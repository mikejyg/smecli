package mikejyg.smecli.session;

import java.io.IOException;
import java.io.Reader;

import mikejyg.smecli.CliLineReader;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CliLineReader.EofException;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;
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
public class Session implements SessionIntf {
	/**
	 * a reference to a SessionCommon.
	 */
	private SessionCommonEnv sessionCommonRef;
	
	/**
	 * this is a session-specific setting.
	 */
	private boolean continueOnError = false;

	private CliLineReader cliLineReader;
	
	/////////////////////////////////////////////////////
	
	public Session(CommandExecutorIntf commandExecutor) {
		sessionCommonRef = new SessionCommonEnv(commandExecutor);
	}
	
	public Session(SessionCommonEnv sessionCommonRef) {
		this.sessionCommonRef = sessionCommonRef;
	}
	
	/**
	 * copy constructor
	 * @param parentSession
	 */
	public Session(Session parentSession) {
		// copy settings
		
		this.sessionCommonRef = parentSession.getSessionCommonRef();
		this.continueOnError = parentSession.continueOnError;
	}
	
	/**
	 * generate a new session, after the current session.
	 * This method is meant to be polymorphic.
	 * @return
	 */
	@Override
	public Session newSubSession() {
		return new Session(this);
	}
	
	@Override
	public void setReader(Reader reader) {
		cliLineReader = new CliLineReader(reader);
	}
	
	protected CmdReturnType execCmd(CmdCallType cmdCall) throws Exception {
		CmdReturnType cmdReturn = getCommandExecutorRef().execCmd(cmdCall);
		
		return cmdReturn;
	}

	/** 
	 * @param cmdLine
	 */
	private CmdReturnType execCmd(String cmdLine) throws Exception {
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
			cmdLine=cliLineReader.readCliLine();
			
		} catch (EofException e) {
			return null;
		}
		
		return cmdLine;
	}
	
	/**
	 * execute all commands from the reader.
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
	public CmdReturnType execAll() {
		while ( !isEndFlag() ) {
			
			if (sessionCommonRef.getPromptFunc()!=null) {
				sessionCommonRef.getPromptFunc().run();
			}
			
			String cmdLine = null;
			CmdReturnType cmdReturn = null;
			
			try {
				cmdLine = fetchCmdLine();
				
				if (cmdLine==null) {
//					getPrintWriter().println("EOF - exiting...");
					break;
				}
				
				if (cmdLine.isEmpty())
					continue;
				
				if (sessionCommonRef.getSessionTranscriptor()!=null) {
					sessionCommonRef.getSessionTranscriptor().onCmdLine(cmdLine);
				}
				
				if (sessionCommonRef.getCmdLineListener()!=null)
					sessionCommonRef.getCmdLineListener().accept(cmdLine);
				
				if ( !cmdLine.isEmpty() && cmdLine.charAt(0)=='#') {
//					getCurrentSession().getPrintWriter().println(cmdLine);
					continue;
				}
				
				try {
					cmdReturn = execCmd(cmdLine);
					
				} catch (Exception e) {
					cmdReturn=new CmdReturnType(ReturnCode.FAILURE, e.getMessage());
				}

			} catch (IOException e) {
				e.printStackTrace();
				cmdReturn = new CmdReturnType(ReturnCode.FAILURE, "IOException: " + e.getMessage());
				
			} catch (IllegalInputCharException e) {
				cmdReturn = new CmdReturnType(ReturnCode.FAILURE, "illegal input character");
				
			} catch (UnexpectedEofException e) {
				e.printStackTrace();
				break;
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

	public SessionCommonEnv getSessionCommonRef() {
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
	

}
