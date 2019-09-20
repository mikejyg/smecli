package mikejyg.smecli.session;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Consumer;

import mikejyg.smecli.CliLineReader;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CliLineReader.EofException;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.cmdexecutor.CommandsCommandExecutor;
import mikejyg.smecli.cmdexecutor.CommandExecutorIntf;

/**
 * This class provides the basic mechanisms for a CLI session.
 * 
 * A session fetches commands from a reader and executes them.
 *  
 * The main loop does the following,
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
public class SessionBase extends CommandsCommandExecutor implements SessionIntf {
	/**
	 * a reference to a SessionCommon.
	 */
	private SessionCommon sessionCommonRef;
	
	private boolean continueOnError = false;

	private CliLineReader cliLineReader;
	
	private Consumer<String> cmdLineListener=null;
	
	/////////////////////////////////////////////////////
	
	public SessionBase(CommandExecutorIntf commandExecutor) {
		sessionCommonRef = new SessionCommon(commandExecutor);
	}
	
	public SessionBase(SessionCommon sessionCommonRef) {
		this.sessionCommonRef = sessionCommonRef;
	}
	
	/**
	 * copy constructor
	 * @param parentSession
	 */
	public SessionBase(SessionBase parentSession) {
		// copy settings
		
		this.sessionCommonRef = parentSession.getSessionCommonRef();
		this.continueOnError = parentSession.continueOnError;
	}
	
	/**
	 * generate a new session, after the current session.
	 * This method is meant to be polymorphic.
	 * @return
	 */
	public SessionBase newSession() {
		return new SessionBase(this);
	}
	
	@Override
	public void setReader(Reader reader) {
		cliLineReader = new CliLineReader(reader);
	}
	
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws Exception {
		// do built-in command first...
		CmdReturnType cmdReturn = super.execCmd(cmdCall);
		
		if (cmdReturn.getReturnCode()==ReturnCode.INVALID_COMMAND) {
			cmdReturn = getCommandExecutorRef().execCmd(cmdCall);
		}
		
		if ( cmdReturn.getReturnCode().isCmdExecResult() )
			setLastCmdReturn(cmdReturn);
			
		return cmdReturn;
	}
	
	@Override
	public String toHelpString() {
		String helpStr=super.toHelpString();
		
		if ( ! helpStr.isEmpty() )
			helpStr = helpStr + '\n';
		
		helpStr = helpStr + "from command executor: \n" + getCommandExecutorRef().toHelpString();

		return helpStr;
	}
	
	/** 
	 * @param cmdLine
	 */
	protected CmdReturnType execCmd(String cmdLine) throws Exception {
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
	@Override
	public CmdReturnType execAll() throws IOException, UnexpectedEofException, IllegalInputCharException {
		while ( !isEndFlag() ) {
			
			if (sessionCommonRef.getPromptFunc()!=null) {
				sessionCommonRef.getPromptFunc().run();
			}
			
			String cmdLine = fetchCmdLine();
			
			if (cmdLine==null) {
//				getPrintWriter().println("EOF - exiting...");
				break;
			}
			
			if (cmdLine.isEmpty())
				continue;
			
			if (sessionCommonRef.getSessionTranscriptor()!=null) {
				sessionCommonRef.getSessionTranscriptor().onCmdLine(cmdLine);
			}
			
			if (getCmdLineListener()!=null)
				getCmdLineListener().accept(cmdLine);
			
			if ( !cmdLine.isEmpty() && cmdLine.charAt(0)=='#') {
//				getCurrentSession().getPrintWriter().println(cmdLine);
				continue;
			}
			
			CmdReturnType cmdReturn;
			try {
				cmdReturn = execCmd(cmdLine);
				
			} catch (Exception e) {
				cmdReturn=new CmdReturnType(ReturnCode.FAILURE, e.getMessage());
			}

			if (sessionCommonRef.getSessionTranscriptor()!=null) {
				sessionCommonRef.getSessionTranscriptor().onCmdReturn(cmdReturn);
			}
			
			if (sessionCommonRef.getCmdReturnListener()!=null)
				sessionCommonRef.getCmdReturnListener().accept(cmdReturn);
			
			if ( cmdReturn.getReturnCode()==ReturnCode.NOP ) {	// no command is executed.
				continue;
			}
			
			if (cmdReturn.getReturnCode()==ReturnCode.SCRIPT_ERROR_EXIT) {	// cascade exit
				if (!continueOnError)
					return cmdReturn;				// return SCRIPT_ERROR_EXIT
				// otherwise, do nothing.
				
			} else if (cmdReturn.getReturnCode()==ReturnCode.EXIT) {
				break;
				
			} else if (cmdReturn.getReturnCode()==ReturnCode.END) {
				setEndFlag(true);
				break;
				
			} else if ( ! cmdReturn.getReturnCode().isOk() ) {	// error
				if ( ! isContinueOnError() ) {
					return new CmdReturnType(ReturnCode.SCRIPT_ERROR_EXIT);	// initiate cascade exit
				}
			}

		}
		
		return new CmdReturnType(ReturnCode.NOP);
	}
	
	protected CliLineReader getCliLineReader() {
		return cliLineReader;
	}

	protected CmdReturnType getLastCmdReturn() {
		return sessionCommonRef.getEnvironment().getLastCmdReturn();
	}

	public void setLastCmdReturn(CmdReturnType lastCmdReturn) {
		sessionCommonRef.getEnvironment().setLastCmdReturn(lastCmdReturn);
	}

	public boolean isContinueOnError() {
		return continueOnError;
	}

	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	protected SessionCommon getSessionCommonRef() {
		return sessionCommonRef;
	}

	public boolean isEndFlag() {
		return getSessionCommonRef().isEndFlag();
	}

	public void setEndFlag(boolean endFlag) {
		getSessionCommonRef().setEndFlag(endFlag);
	}
	
	public CommandExecutorIntf getCommandExecutorRef() {
		return sessionCommonRef.getCommandExecutorRef();
	}

	public void setPromptFunc(Runnable promptFunc) {
		sessionCommonRef.setPromptFunc(promptFunc);
	}
	
	public Consumer<String> getCmdLineListener() {
		return cmdLineListener;
	}

	public void setCmdLineListener(Consumer<String> cmdLineListener) {
		this.cmdLineListener = cmdLineListener;
	}


}
