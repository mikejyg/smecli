package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.function.Function;

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
 * modularity: command executors are modules, and can be added or removed at run-time.
 * 
 * extendability: CLI can be extended to change or expand its functionalities.
 * 
 * @author jgu
 *
 */
public class Cli {
	static public class EofException extends Exception {
		private static final long serialVersionUID = 1L;
	};
	
	static public class ExitAllSessions extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	static public class InvokeCommandFailed extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	static class CommandStruct {
		String commandName;
		String [] shorthands;
		String helpString;

		Function<CmdCallType, CmdReturnType> cmdFunc;
		
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
	 * @throws InvokeCommandFailed
	 * @Return not null.
	 */
	public CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed  {
		CommandStruct cmdStruct = cmdMap.get(cmdCall.commandName);
		
		if (cmdStruct==null) {
			return new CmdReturnType(ReturnCode.INVALID_COMMAND);
		}
		
		CmdReturnType cmdReturn = cmdStruct.cmdFunc.apply(cmdCall);
		
		if (cmdReturn==null)
			throw new InvokeCommandFailed();
		
		return cmdReturn;
	}
	
	/** 
	 * @param cmdLine
	 * @return null, if cmdLins is null, or no command is found in the cmdLine.
	 * @throws InvokeCommandFailed 
	 */
	public CmdReturnType execCmd(String cmdLine) throws InvokeCommandFailed {
		CmdCallType cmdCall = CmdCallType.toCmdCall(cmdLine);
		if (cmdCall==null)
			return null;
		return execCmd(cmdCall);
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
	
	public Cli() {}
	
	public void addCommand(String commandName, String shorthands[], String helpString, Function<CmdCallType, CmdReturnType> cmdFunc) {
		CommandStruct commandStruct=new CommandStruct();
		
		commandStruct.commandName = commandName;
		commandStruct.shorthands = shorthands;
		commandStruct.helpString = helpString;
		commandStruct.cmdFunc = cmdFunc;
		
		commands.add(commandStruct);
		cmdMap.put(commandStruct.commandName, commandStruct);
		
		for (String s : shorthands) {
			cmdMap.put(s, commandStruct);
		}

	}
	
	public CmdReturnType execAll(Reader reader, PrintWriter printWriter) throws IOException, IllegalInputCharException, UnexpectedEofException, ExitAllSessions {
		CliSession session = new CliSession(new BufferedReader(reader), printWriter, initialPrompt, initialLocalEcho);
		sessionStack.add(session);
		
		CmdReturnType cmdReturnType = execAll(getCurrentSession());
		
		sessionStack.remove(sessionStack.size()-1);

		return cmdReturnType;
	}
	
	public CmdReturnType execAll(Reader reader, Writer writer) throws IOException, IllegalInputCharException, UnexpectedEofException, ExitAllSessions {
		return execAll(reader, new PrintWriter(writer));
	}
	
	/**
	 * execute all commands from the reader.
	 * @throws IOException 
	 * @throws UnexpectedEofException 
	 * @throws IllegalInputCharException 
	 * @throws ExitAllSessions 
	 * 
	 * @return the return of the last command
	 */
	protected CmdReturnType execAll(CliSession session) throws IOException, UnexpectedEofException, IllegalInputCharException, ExitAllSessions {
		CmdReturnType cmdReturn=null;
		
		while (!session.isExitFlag()) {
			
			if (session.getPrompt()!=null) {
				session.getPrintWriter().print(session.getPrompt());
				session.getPrintWriter().flush();
			}
			
			String cmdLine;
			try {
				cmdLine=getCurrentSession().getCliLineReader().readCliLine();
				
			} catch (EofException e) {
				session.getPrintWriter().println("EOF - exiting...");
				break;
			}
			
			if (session.isLocalEcho())
				session.getPrintWriter().println(cmdLine);
			
			if (cmdLine.charAt(0)=='#') {
				getCurrentSession().getPrintWriter().println(cmdLine);
				continue;
			}
			
			try {
				cmdReturn = execCmd(cmdLine);
			} catch (InvokeCommandFailed e) {
				throw new RuntimeException("failed to invoke: " + cmdLine);
			}

			if (cmdReturn==null)	// no command is executed.
				continue;
			
			if (session.isEndFlag())
				throw new ExitAllSessions();
			
			if ( ! processResults(cmdReturn) )
				break;
		}
		
		return cmdReturn;
	}
	
	/**
	 * the option of whether to continue in case of error.
	 * @param continueOnError
	 */
	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	/**
	 * whether to echo the command that is read. This is useful in script execution mode.
	 * @param localEcho
	 */
	public void setLocalEcho(boolean localEcho) {
		this.initialLocalEcho = localEcho;
	}

	/**
	 * Set the prompt. If it is set to null, then prompt is disabled. 
	 * @param prompt
	 */
	public void setPrompt(String prompt) {
		this.initialPrompt = prompt;
	}

	protected CliSession getCurrentSession() {
		return sessionStack.lastElement();
	}
	
	/**
	 * return the list of available commands.
	 * @return
	 */
	public Vector<CommandStruct> getCommands() {
		return commands;
	}

	public PrintWriter getPrintWriter() {
		return getCurrentSession().getPrintWriter();
	}
	
	public void setExitFlag(boolean exitFlag) {
		getCurrentSession().setExitFlag(exitFlag);
	}
	
	public void setEndFlag(boolean endFlag) {
		getCurrentSession().setEndFlag(endFlag);
	}
	
}	
