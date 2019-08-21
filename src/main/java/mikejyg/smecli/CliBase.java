package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Stack;

import mikejyg.smecli.CliCommands.CommandStruct;
import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;

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
		
	///////////////////////////////////////////////////////////////////

	/**
	 * for built-in and flow control commands
	 */
	private CliCommands cliCommands;
	
	private CommandExecutorIntf commandExecutor;
	
	// behavior options:
	
	private String prompt = "";
	
	private boolean localEcho;

	/**
	 * For the root (1st) session, whether to continue (not exit) on error.
	 * For all sub sessions, always exit on error.
	 */
	private boolean continueOnError=true;

	private boolean endFlag;	// exit all (nested) sessions.
	
	// for nested sessions
	private Stack<CliSession> sessionStack = new Stack<>();
	
	/**
	 * where to print out things, including prompt, results, errors, status...
	 */
	private PrintStream printStream = System.out;
	
	// working variables
	
	/**
	 * last result of a non-flow control command.
	 */
	private CmdReturnType lastCmdReturn;

	///////////////////////////////////////////////////////////
	
	public CliBase() {
		cliCommands = new CliCommands();
	}
	
	public CliBase(CommandExecutorIntf commandExecutor) {
		this();
		this.commandExecutor = commandExecutor;
	}
	
	/**
	 * @throws InvokeCommandFailed
	 */
	public CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed {
		// do built-in command first...
		CommandStruct cmdStruct = cliCommands.getCommand(cmdCall.getCommandName());
		
		if (cmdStruct!=null) {
			CmdReturnType cmdReturn = cmdStruct.cmdFunc.apply(cmdCall);

			if ( cmdReturn.getReturnCode()!=ReturnCode.NOP && cmdReturn.getReturnCode()!=ReturnCode.SCRIPT_ERROR_EXIT ) {
				lastCmdReturn = cmdReturn;
			}
			
			return cmdReturn;
			
		} else {
			lastCmdReturn = commandExecutor.execCmd(cmdCall);
			
			if (lastCmdReturn.getReturnCode()==ReturnCode.EXIT) {
				setExitFlag(true);
			} else if (lastCmdReturn.getReturnCode()==ReturnCode.END) {
				setExitFlag(true);
				setEndFlag(true);
			}
			
			return lastCmdReturn;
		}
	}
	
	/** 
	 * @param cmdLine
	 * @throws InvokeCommandFailed 
	 */
	public CmdReturnType execCmd(String cmdLine) throws InvokeCommandFailed {
		CmdCallType cmdCall = CmdCallType.toCmdCall(cmdLine);
		if (cmdCall.isEmpty())
			return new CmdReturnType(ReturnCode.NOP);			// no command was executed
			
		return execCmd(cmdCall);
	}

	/** 
	 * @param
	 * @throws InvokeCommandFailed 
	 */
	public CmdReturnType execCmd(String args[]) throws InvokeCommandFailed {
		CmdCallType cmdCall = CmdCallType.toCmdCall(args);
		if (cmdCall.isEmpty())
			return new CmdReturnType(ReturnCode.NOP);	// no command was executed
		
		return execCmd(cmdCall);
	}

	/**
	 * override this method to get the command returns.
	 * 
	 * @param cmdReturn
	 */
	protected void processResults(CmdReturnType cmdReturn) {
		printStream.println(cmdReturn.getReturnCode().name());
		
		if ( ! cmdReturn.getResult().isEmpty() )
			printStream.println(cmdReturn.getResult());
	}
	
	public CmdReturnType execAll(BufferedReader reader) throws IOException, IllegalInputCharException, UnexpectedEofException {
		CliSession session = new CliSession(reader, prompt, isLocalEcho());
		sessionStack.add(session);
		
		CmdReturnType cmdReturn = execAll(getCurrentSession());
		
		sessionStack.remove(sessionStack.size()-1);
		
		return cmdReturn;
	}
	
	/**
	 * execute all commands from the reader.
	 * @throws IOException 
	 * @throws UnexpectedEofException 
	 * @throws IllegalInputCharException 
	 * @throws ExitAllSessions 
	 * 
	 * Conversions on using command returns and lastCmdReturn:
	 *   Each command returns a CmdReturnType.
	 *   Additionally, if the result is a solid result, it is persisted in the variable lastCmdReturn.
	 *   A solid result is,
	 *     error executing the command,
	 *     or, success executing a non-flow control command (e.g. source).
	 *   
	 *   The rationale is that when flow control commands succeed, they are transparent, or invisible.
	 *   
	 * possible return values:
	 *   SCRIPT_ERROR_EXIT: to exit cascadingly, due to an error while executing a script.
	 *   others: solid results
	 * 
	 */
	protected CmdReturnType execAll(CliSession session) throws IOException, UnexpectedEofException, IllegalInputCharException {
		while ( !isEndFlag() && !session.isExitFlag() ) {
			
			if (!session.getPrompt().isEmpty()) {
				printStream.print(session.getPrompt());
				printStream.flush();
			}
			
			String cmdLine;
			try {
				cmdLine=getCurrentSession().getCliLineReader().readCliLine();
				
			} catch (EofException e) {
				printStream.println("EOF - exiting...");
				break;
			}
			
			if (session.isLocalEcho()) {
				printStream.println(cmdLine);
				printStream.flush();
			}
			
			if ( !cmdLine.isEmpty() && cmdLine.charAt(0)=='#') {
//				getCurrentSession().getPrintWriter().println(cmdLine);
				continue;
			}
			
			CmdReturnType cmdReturn;
			try {
				cmdReturn = execCmd(cmdLine);
			} catch (InvokeCommandFailed e) {
				throw new RuntimeException("failed to invoke: " + cmdLine);
			}

			if ( cmdReturn.getReturnCode()==ReturnCode.NOP )	// no command is executed.
				continue;
			
			if (cmdReturn.getReturnCode()==ReturnCode.SCRIPT_ERROR_EXIT) {	// cascade exit
				if ( ! isRootSession(session) || ! continueOnError ) {
					return cmdReturn;	
				}
			}
			
			// has a solid return
			processResults(cmdReturn);
			
			if ( ! cmdReturn.getReturnCode().isOk() ) {	// error
				if ( ! isRootSession(session) || ! continueOnError ) {
					return new CmdReturnType(ReturnCode.SCRIPT_ERROR_EXIT);	// initiate cascade exit
				}
			}
			
		}
		
		return new CmdReturnType(ReturnCode.NOP);
	}
	
	/**
	 * the option of whether to continue in case of error.
	 * @param continueOnError
	 */
	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	public boolean isContinueOnError() {
		return continueOnError;
	}
	
	/**
	 * whether to echo the command that is read. This is useful in script execution mode.
	 * @param localEcho
	 */
	public void setLocalEcho(boolean localEcho) {
		if (sessionStack.isEmpty())
			this.localEcho = localEcho;
		else
			getCurrentSession().setLocalEcho(localEcho);
	}

	public boolean isLocalEcho() {
		if (sessionStack.isEmpty())
			return localEcho;
		else
			return getCurrentSession().isLocalEcho();
	}
	
	/**
	 * Set the prompt. If it is set to en empty string, then prompt is disabled. 
	 * @param prompt
	 */
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	protected CliSession getCurrentSession() {
		return sessionStack.lastElement();
	}
	
	public void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}

	public void setExitFlag(boolean exitFlag) {
		if ( sessionStack.isEmpty() ) {
			;	// not applicable, ignored
		} else {
			getCurrentSession().setExitFlag(exitFlag);
		}
	}
	
	public boolean isEndFlag() {
		return endFlag;
	}
	
	public void setEndFlag(boolean endFlag) {
		this.endFlag = endFlag;
	}
	
	public boolean isRootSession(CliSession cliSession) {
		return cliSession == sessionStack.elementAt(0);
	}

	public CliCommands getCliCommands() {
		return cliCommands;
	}

	public CmdReturnType getLastCmdReturn() {
		return lastCmdReturn;
	}

	public void setCommandExecutor(CommandExecutorIntf commandExecutor) {
		this.commandExecutor = commandExecutor;
	}

	public CommandExecutorIntf getCommandExecutor() {
		return commandExecutor;
	}

	/**
	 * run an interactive session (fron stdin).
	 * 
	 * @throws IOException
	 * @throws IllegalInputCharException
	 * @throws UnexpectedEofException
	 */
	public void runInteractive() throws IOException, IllegalInputCharException, UnexpectedEofException {
		setPrompt("> ");
		execAll(new BufferedReader(new InputStreamReader(System.in)));
	}
	

}	
