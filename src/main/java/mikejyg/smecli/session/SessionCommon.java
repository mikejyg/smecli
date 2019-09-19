package mikejyg.smecli.session;

import java.util.function.Consumer;

import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CommandExecutorIntf;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * this class holds the settings and variables common to all sessions.
 * 
 * @author mikejyg
 *
 */
public class SessionCommon {
	private CommandExecutorIntf commandExecutorRef;
	
	private boolean endFlag=false;	// exit all (nested) sessions.
	
	private CmdReturnType lastCmdExecResult = new CmdReturnType(ReturnCode.OK);

	private Runnable promptFunc=null;
	
	private Consumer<CmdReturnType> cmdReturnListener=null;
	
	private SessionTranscriptor sessionTranscriptor=null;
	
	/////////////////////////////////////////////////
	
	public SessionCommon(CommandExecutorIntf commandExecutor) {
		this.commandExecutorRef = commandExecutor;
	}
	
	/////////////////////////////////////////////////

	public CommandExecutorIntf getCommandExecutorRef() {
		return commandExecutorRef;
	}

	public void setCommandExecutorRef(CommandExecutorIntf commandExecutorRef) {
		this.commandExecutorRef = commandExecutorRef;
	}

	public CmdReturnType getLastCmdExecResult() {
		return lastCmdExecResult;
	}

	public void setLastCmdExecResult(CmdReturnType lastCmdExecResult) {
		this.lastCmdExecResult = lastCmdExecResult;
	}

	public boolean isEndFlag() {
		return endFlag;
	}
	
	public void setEndFlag(boolean endFlag) {
		this.endFlag = endFlag;
	}

	public Runnable getPromptFunc() {
		return promptFunc;
	}

	public void setPromptFunc(Runnable promptFunc) {
		this.promptFunc = promptFunc;
	}

	public SessionTranscriptor getSessionTranscriptor() {
		return sessionTranscriptor;
	}

	public void setSessionTranscriptor(SessionTranscriptor sessionTranscriptor) {
		this.sessionTranscriptor = sessionTranscriptor;
	}

	public Consumer<CmdReturnType> getCmdReturnListener() {
		return cmdReturnListener;
	}

	public void setCmdReturnListener(Consumer<CmdReturnType> cmdReturnListener) {
		this.cmdReturnListener = cmdReturnListener;
	}


}	
