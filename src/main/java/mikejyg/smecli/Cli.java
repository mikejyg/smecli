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

import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;

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

	// command storage & indexes
	
	private Vector<CommandStruct> commands = new Vector<>();
	
	private Map<String, CommandStruct> cmdMap = new TreeMap<>();
	
	// for nested sessions
	private Vector<CliSession> sessionStack = new Vector<>();
	
	///////////////////////////////////////////////////////////

	/**
	 * read a command, that includes a command name and a arguments string.
	 * 
	 * a command name consists of alphanumeric chars, and is separated from the arguments string by a white space. 
	 *
	 * results are in commandName and argumentsStr
	 * 
	 * @return true if a command is read successfully, false otherwise.
	 * 
	 * @throws IOException 
	 * @throws EofException 
	 * @throws UnexpectedEofException 
	 * @throws IllegalInputCharException 
	 * 
	 */
	protected CmdCallType readCmd() throws IOException, EofException, IllegalInputCharException, UnexpectedEofException {
		String cmdLine=getCurrentSession().getCliLineReader().readCliLine();
		
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
			
			String commandName;
			if (cliCmd.commandName().isEmpty())
				commandName = method.getName();
			else
				commandName = cliCmd.commandName();
			
			addCommand(commandName, cliCmd.shorthands(), cliCmd.helpString(), cmdObj, method);
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
	
	public void addCommand(String commandName, String shorthands[], String helpString, Object cmdObj, Method method) {
		CommandStruct commandStruct=new CommandStruct();
		
		commandStruct.commandName = commandName;
		commandStruct.shorthands = shorthands;
		commandStruct.helpString = helpString;
		commandStruct.cmdObj = cmdObj;
		commandStruct.method = method;
		
		commands.add(commandStruct);
		cmdMap.put(commandStruct.commandName, commandStruct);
		
		for (String s : shorthands) {
			cmdMap.put(s, commandStruct);
		}

	}
	
	public void execAll(Reader reader, Writer writer) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, IllegalInputCharException, UnexpectedEofException {
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
	 * @throws UnexpectedEofException 
	 * @throws IllegalInputCharException 
	 */
	public void execAll(CliSession session) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IllegalInputCharException, UnexpectedEofException {
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
