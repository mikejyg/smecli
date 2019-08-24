package mikejyg.smecli;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import mikejyg.smecli.CliCommands.CommandStruct;
import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CliLineReader.EofException;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * A CLI session fetches commands from a reader and executes them.
 *  
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
 * @author mikejyg
 *
 */
public class CliSession {
	/**
	 * a reference to a CliBase.
	 */
	private CliBase cliBaseRef;
	
	/**
	 * default value for an interactive session: false
	 */
	private boolean localEcho=false;

	/**
	 * default value for an interactive session: true
	 */
	private boolean continueOnError = true;

	private CliLineReader cliLineReader;
	
	/**
	 * session specific commands.
	 */
	private CliCommands cliCommands = new CliCommands();;
	
	/**
	 * interactive means, reading user input from console.
	 */
	private boolean interactiveFlag;
	
	/////////////////////////////////////////////////////
	
	public CliSession(CliBase cliBase) {
		this.cliBaseRef = cliBase;
	}

	/**
	 * copy constructor
	 * @param parentSession
	 */
	public CliSession(CliSession parentSession) {
		// copy settings
		
		this.cliBaseRef = parentSession.getCliBaseRef();
		this.localEcho = parentSession.localEcho;
		this.continueOnError = parentSession.continueOnError;
	}
	
	/**
	 * generate a new session, after the current session.
	 * This method is meant to be polymorphic.
	 * @return
	 */
	public CliSession newSession() {
		return new CliSession(this);
	}
	
	public void setReader(Reader reader) {
		cliLineReader = new CliLineReader(reader);
	}
	
	/**
	 * @throws InvokeCommandFailed
	 */
	private CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed {
		// do built-in command first...
		CommandStruct cmdStruct = getCliCommands().getCommand(cmdCall.getCommandName());
		
		CmdReturnType cmdReturn;
		
		if (cmdStruct!=null)
			cmdReturn = cmdStruct.cmdFunc.apply(cmdCall);
		else
			cmdReturn = getCommandExecutorRef().execCmd(cmdCall);
			
		if ( cmdReturn.getReturnCode().isCmdExecResult() )
			setLastCmdExecResult(cmdReturn);
			
		return cmdReturn;
	}
	
	/** 
	 * @param cmdLine
	 * @throws InvokeCommandFailed 
	 */
	private CmdReturnType execCmd(String cmdLine) throws InvokeCommandFailed {
		if (getCliBaseRef().getCmdExecListener()!=null)
			getCliBaseRef().getCmdExecListener().accept(cmdLine);
		
		CmdCallType cmdCall = CmdCallType.toCmdCall(cmdLine);
		if (cmdCall.isEmpty())
			return new CmdReturnType(ReturnCode.NOP);			// no command was executed
			
		return execCmd(cmdCall);
	}

	/**
	 * @return null if EOF
	 * @throws IOException
	 * @throws IllegalInputCharException
	 * @throws UnexpectedEofException
	 */
	protected String fetchCmdLine() throws IOException, IllegalInputCharException, UnexpectedEofException {
		String cmdLine;
		try {
			cmdLine=getCliLineReader().readCliLine();
			
		} catch (EofException e) {
			return null;
		}
		
		return cmdLine;
	}
	
	/**
	 * execute all commands from the reader.
	 * @throws IOException 
	 * @throws UnexpectedEofException 
	 * @throws IllegalInputCharException 
	 * @throws ExitAllSessions 
	 * 
	 * Conversions on using command returns and lastCmdExecResult:
	 *   Each command returns a CmdReturnType.
	 *   Additionally, if the result is a solid result, it is persisted in the variable lastCmdExecResult.
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
	public CmdReturnType execAll() throws IOException, UnexpectedEofException, IllegalInputCharException {
		while ( !isEndFlag() ) {
			
			if ( ! cliBaseRef.isPrompted() && ! getPrompt().isEmpty() ) {
				getPrintStream().print(getPrompt());
				getPrintStream().flush();
				cliBaseRef.setPrompted(true);
			}
			
			String cmdLine = fetchCmdLine();
			
			if (cmdLine==null) {
//				getPrintStream().println("EOF - exiting...");
				break;
			}
			
			// interactive - input from console, the prompt is consumed by user input.
			if (interactiveFlag)
				cliBaseRef.setPrompted(false);
			
			if (isLocalEcho()) {
				getPrintStream().println(cmdLine);
				getPrintStream().flush();
				cliBaseRef.setPrompted(false);
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

			if ( cmdReturn.getReturnCode()==ReturnCode.NOP ) {	// no command is executed.
				continue;
			}
			
			if (cmdReturn.getReturnCode()==ReturnCode.SCRIPT_ERROR_EXIT) {	// cascade exit
				return cmdReturn;				// return SCRIPT_ERROR_EXIT
				
			} else if (cmdReturn.getReturnCode()==ReturnCode.EXIT) {
				cliBaseRef.getCmdReturnListener().accept(cmdReturn);
				break;
				
			} else if (cmdReturn.getReturnCode()==ReturnCode.END) {
				cliBaseRef.getCmdReturnListener().accept(cmdReturn);
				setEndFlag(true);
				break;
				
			} else if ( ! cmdReturn.getReturnCode().isOk() ) {	// error
				if ( ! isContinueOnError() ) {
					cliBaseRef.getCmdReturnListener().accept(cmdReturn);
					return new CmdReturnType(ReturnCode.SCRIPT_ERROR_EXIT);	// initiate cascade exit
				}
			}

			cliBaseRef.getCmdReturnListener().accept(cmdReturn);
			
		}
		
		return new CmdReturnType(ReturnCode.NOP);
	}
	
	protected CliLineReader getCliLineReader() {
		return cliLineReader;
	}

	protected CmdReturnType getLastCmdExecResult() {
		return cliBaseRef.getLastCmdExecResult();
	}

	public void setLastCmdExecResult(CmdReturnType lastCmdExecResult) {
		cliBaseRef.setLastCmdExecResult(lastCmdExecResult);
	}

	public boolean isLocalEcho() {
		return localEcho;
	}

	public void setLocalEcho(boolean localEcho) {
		this.localEcho = localEcho;
	}

	public boolean isContinueOnError() {
		return continueOnError;
	}

	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	protected CliBase getCliBaseRef() {
		return cliBaseRef;
	}

	protected PrintStream getPrintStream() {
		return getCliBaseRef().getPrintStream();
	}

	public boolean isEndFlag() {
		return getCliBaseRef().isEndFlag();
	}

	public void setEndFlag(boolean endFlag) {
		getCliBaseRef().setEndFlag(endFlag);
	}
	
	public String getPrompt() {
		return getCliBaseRef().getPrompt();
	}
	
	public CommandExecutorIntf getCommandExecutorRef() {
		return cliBaseRef.getCommandExecutorRef();
	}

	public CliCommands getCliCommands() {
		return cliCommands;
	}

	public void setInteractiveFlag(boolean interactiveFlag) {
		this.interactiveFlag = interactiveFlag;
	}

	/**
	 * run an interactive session (fron stdin).
	 * @throws IOException
	 * @throws UnexpectedEofException
	 * @throws IllegalInputCharException
	 */
	public static void runInteractive(CliSession cli) throws IOException, UnexpectedEofException, IllegalInputCharException {
		try (InputStreamReader reader = new InputStreamReader(System.in) ) {
			cli.setReader( reader );
			cli.setInteractiveFlag(true);
			cli.execAll();
		}
	}

	
}
