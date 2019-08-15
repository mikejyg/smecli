package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
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
 * 2. parse the input item into a command name, and a arguments string, and
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
public class CliBase {
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
			if (shorthands!=null) {
				for (String s : shorthands) {
					str += ", " + s;
				}
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
	
	private PrintStream printStream = System.out;
	
	private CmdReturnType lastCmdReturn;
	
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
		
		lastCmdReturn = cmdStruct.cmdFunc.apply(cmdCall);
		
		if (lastCmdReturn==null)
			throw new InvokeCommandFailed();
		
		return lastCmdReturn;
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
	 * @param
	 * @return null, if args is null, or no command is found in args.
	 * @throws InvokeCommandFailed 
	 */
	public CmdReturnType execCmd(String args[]) throws InvokeCommandFailed {
		CmdCallType cmdCall = CmdCallType.toCmdCall(args);
		if (cmdCall==null)
			return null;
		return execCmd(cmdCall);
	}
	
	/**
	 * @return true to continue or not.
	 */
	protected boolean processResults(CmdReturnType cmdReturn) {
		if (cmdReturn.result!=null)
			getPrintStream().println(cmdReturn.result);
		
		if (cmdReturn.returnCode != ReturnCode.SUCCESS) {
			getPrintStream().println(cmdReturn.returnCode.name());
			
			if (continueOnError)
				return true;
			else
				return false;
			
		} else {
			getPrintStream().println("OK.");
			return true;
		}
	}
	
	///////////////////////////////////////////////////////////
	
	public CliBase() {}
	
	public void addCommand(String commandName, String shorthands[], String helpString, Function<CmdCallType, CmdReturnType> cmdFunc) {
		CommandStruct commandStruct=new CommandStruct();
		
		commandStruct.commandName = commandName;
		commandStruct.shorthands = shorthands;
		commandStruct.helpString = helpString;
		commandStruct.cmdFunc = cmdFunc;
		
		commands.add(commandStruct);
		cmdMap.put(commandStruct.commandName, commandStruct);
		
		if (shorthands!=null) {
			for (String s : shorthands) {
				cmdMap.put(s, commandStruct);
			}
		}
	}
	
	public CmdReturnType execAll(BufferedReader reader) throws IOException, IllegalInputCharException, UnexpectedEofException, ExitAllSessions {
		CliSession session = new CliSession(reader, initialPrompt, initialLocalEcho);
		sessionStack.add(session);
		
		CmdReturnType cmdReturnType = execAll(getCurrentSession());
		
		sessionStack.remove(sessionStack.size()-1);

		return cmdReturnType;
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
				getPrintStream().print(session.getPrompt());
				getPrintStream().flush();
			}
			
			String cmdLine;
			try {
				cmdLine=getCurrentSession().getCliLineReader().readCliLine();
				
			} catch (EofException e) {
				getPrintStream().println("EOF - exiting...");
				break;
			}
			
			if (session.isLocalEcho()) {
				getPrintStream().println(cmdLine);
				getPrintStream().flush();
			}
			
			if ( !cmdLine.isEmpty() && cmdLine.charAt(0)=='#') {
//				getCurrentSession().getPrintWriter().println(cmdLine);
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
		if (sessionStack.isEmpty())
			return null;
		return sessionStack.lastElement();
	}
	
	/**
	 * return the list of available commands.
	 * @return
	 */
	public Vector<CommandStruct> getCommands() {
		return commands;
	}

	public PrintStream getPrintStream() {
		return printStream;
	}
	
	public void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}

	public void setExitFlag(boolean exitFlag) {
		getCurrentSession().setExitFlag(exitFlag);
	}
	
	public void setEndFlag(boolean endFlag) {
		getCurrentSession().setEndFlag(endFlag);
	}
	
	public CmdReturnType getLastCmdReturn() {
		return lastCmdReturn;
	}

	public void setLastCmdReturn(CmdReturnType lastCmdReturn) {
		this.lastCmdReturn = lastCmdReturn;
	}

}	
