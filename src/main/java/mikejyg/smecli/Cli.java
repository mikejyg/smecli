package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * A text console does the following,
 * 
 * 1. read an input item one at a time, and
 * 2. parse the input itme into a command name, and a arguments string, and
 * 3. call the corresponding command executor to execute the command.
 * 4. output command result.
 * 5. based on the return code, take the next action. 
 * 
 * CLI has its own set of built-in commands.
 * 
 * modularity: command executors are modules, and can be added or removed at run-time.
 * 
 * extendability: CLI can be extended to change or expand its functionalities.
 * 
 * @author jgu
 *
 */
public class Cli {
	/**
	 * a commandName function is of the form: CmdReturnType commandName(String argumentsStr)
	 *  
	 * @param argumentsStr	a string contains device specific arguments... 
	 * @return
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface CliCommand {
		String commandName() default "";	// when default, use the function name.
		String [] shorthands() default {};
		String helpString() default "";
	}
	
	static public class EofException extends Exception {
		private static final long serialVersionUID = 1L;
	};
	
	static class CommandStruct {
		String commandName;
		String [] shorthands;
		String helpString;
		
		Object cmdObj;
		Method method;
		
		@Override
		public String toString() {
			String str = commandName;
			for (String s : shorthands) {
				str += ", " + s;
			}
			str += "\t" + helpString;
			
			return str;
		}
	};
	
	///////////////////////////////////////////////////////////////////

	// behavior options:
	
	private String initialPrompt = "";
	
	private boolean initialLocalEcho;

	private boolean continueOnError;
	
	private Vector<CommandStruct> commands = new Vector<>();
	
	private Map<String, CommandStruct> cmdMap = new TreeMap<>();
	
	private Vector<CliSession> sessionStack = new Vector<>();
	
	///////////////////////////////////////////////////////////

	protected String processEscapeSequences(String strIn) {
		String str="";
		for (int i=0; i<strIn.length(); i++) {
			char c = strIn.charAt(i);
			
			if (c=='\\') {
				if ( i+1 < strIn.length() ) {
					char c1 = strIn.charAt(i+1);
					if (c1=='\\') {
						i++;		// skip the next \
					}
				}
			}
			
			str += c;
		}
		
		return str;
	}
	
	/**
	 * read a command, that includes a command name and a arguments string.
	 * 
	 * a command name consists of alphanumeric chars, and is separated from the arguments string by a white space. 
	 *
	 * the following is to support taking multiple lines as a single arguments string.
	 * 
	 * white spaces at both ends are trimmed.
	 * \ at line end joins 2 lines, and turns into a space.
	 * escape sequences \\ is supported. Any other \ sequences are ignored.
	 *
	 * results are in commandName and argumentsStr
	 * 
	 * @return true if a command is read successfully, false otherwise.
	 * 
	 * @throws IOException 
	 * @throws EofException 
	 * 
	 */
	protected CmdCallType readCmd() throws IOException, EofException {
		String cmdLine=new String();
		
		boolean readNext = true;
		
		while (readNext) {
			String line = getCurrentSession().getBufferedReader().readLine();
			if (line==null)
				throw new EofException();
			
			line.trim();
			
			if (line.isEmpty())
				continue;
			
			line = processEscapeSequences(line);
			
			if (line.endsWith("\\")) {
				// remove the ending \
				line = line.substring(0, line.length()-1);
			} else {
				readNext = false;
			}
			
			if (cmdLine.isEmpty()) {
				cmdLine = line;
			} else {
				cmdLine = cmdLine + " " + line;
			}
			
		}
		
		if (cmdLine.charAt(0)=='#') {
			getCurrentSession().getPrintWriter().println(cmdLine);
			return null;
		}
		
		CmdCallType cmdCall;
		
		int k = cmdLine.indexOf(' ');
		if (k==-1) {
			cmdCall = new CmdCallType(cmdLine, "");
		} else {
			cmdCall = new CmdCallType( cmdLine.substring(0, k).trim(), cmdLine.substring(k+1).trim() );
		}
		
		return cmdCall;
	}
	
	/**
	 *
	 * @return true if continue, otherwise exit.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	protected CmdReturnType execCmd(CmdCallType cmdCall) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		CommandStruct cmdStruct = cmdMap.get(cmdCall.commandName);
		
		if (cmdStruct==null) {
			return new CmdReturnType(ReturnCode.INVALID_COMMAND);
		}
		
		return (CmdReturnType) cmdStruct.method.invoke(cmdStruct.cmdObj, cmdCall.argumentsStr);
	}
	
	protected void addMethods(Object cmdObj) {
		Method [] methods = cmdObj.getClass().getMethods();
		
		for (Method method : methods) {
			CliCommand cliCmd = method.getAnnotation(CliCommand.class);
			if (cliCmd==null)
				continue;
			
			CommandStruct commandStruct=new CommandStruct();
			
			if (cliCmd.commandName().isEmpty())
				commandStruct.commandName = method.getName();
			else
				commandStruct.commandName = cliCmd.commandName();
			
			commandStruct.method = method;
			commandStruct.shorthands = cliCmd.shorthands();
			commandStruct.helpString = cliCmd.helpString();
			commandStruct.cmdObj = cmdObj;
			
			commands.add(commandStruct);
			cmdMap.put(commandStruct.commandName, commandStruct);
			
			for (String s : cliCmd.shorthands()) {
				cmdMap.put(s, commandStruct);
			}
		}

	}
	
	/**
	 * @return true to continue or not.
	 */
	protected boolean processResults(CmdReturnType cmdReturn) {
		if (cmdReturn.result!=null)
			getCurrentSession().getPrintWriter().println(cmdReturn.result);
		
		if (cmdReturn.returnCode != ReturnCode.SUCCESS) {
			getCurrentSession().getPrintWriter().println(cmdReturn.returnCode.name());
			
			if (continueOnError)
				return true;
			else
				return false;
			
		} else {
			getCurrentSession().getPrintWriter().println("OK.");
			return true;
		}
	}
	
	///////////////////////////////////////////////////////////
	
	public Cli() {
		// init built-in commands
		addMethods(this);
	}
	
	public void execAll(Reader reader, Writer writer) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		CliSession session = new CliSession(new BufferedReader(reader), new PrintWriter(writer), initialPrompt, initialLocalEcho);
		sessionStack.add(session);
		
		execAll(getCurrentSession());
		
		getCurrentSession().close();
		sessionStack.remove(sessionStack.size()-1);
	}
	
	/**
	 * execute all commands from the reader.
	 * @throws IOException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public void execAll(CliSession session) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		while (!session.isExitFlag()) {
			
			if (session.getPrompt()!=null)
				session.getPrintWriter().print(session.getPrompt());
			
			CmdCallType cmdCall;
			try {
				cmdCall = readCmd();
			} catch (EofException e) {
				session.getPrintWriter().println("EOF - exiting...");
				break;
			}
			
			if (cmdCall==null)
				continue;
				
			if (session.isLocalEcho())
				session.getPrintWriter().println(cmdCall.toString());
			
			CmdReturnType cmdReturn = execCmd(cmdCall);
			
			if ( ! processResults(cmdReturn) )
				break;
		}
	}
	
	@CliCommand(shorthands = {"?"}, helpString = "print help.")
	public CmdReturnType help(String argumentsStr) {
		for (CommandStruct cmd : commands) {
			getCurrentSession().getPrintWriter().println(cmd.toString());
		}
		return new CmdReturnType(ReturnCode.SUCCESS);
	}
	
	@CliCommand(helpString = "echo arguments.")
	public CmdReturnType echo(String argumentsStr) {
		getCurrentSession().getPrintWriter().println(argumentsStr);
		return new CmdReturnType(ReturnCode.SUCCESS);
	}
	
	@CliCommand(commandName="exit", helpString = "exit current stream.")
	public CmdReturnType exitSession(String argumentsStr) {
		getCurrentSession().getPrintWriter().println("exit()...");
		
		getCurrentSession().setExitFlag(true);
		
		return new CmdReturnType(ReturnCode.SUCCESS);
	}
	
	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	public void setLocalEcho(boolean localEcho) {
		this.initialLocalEcho = localEcho;
	}

	public void setPrompt(String prompt) {
		this.initialPrompt = prompt;
	}

	public CliSession getCurrentSession() {
		return sessionStack.lastElement();
	}
	
	
}
