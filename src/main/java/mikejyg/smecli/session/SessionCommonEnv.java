package mikejyg.smecli.session;

import java.util.function.Consumer;

import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.Environment;
import mikejyg.smecli.cmdexecutor.CommandExecutorIntf;

/**
 * this class holds the settings and variables common to all sessions.
 * 
 * @author mikejyg
 *
 */
public class SessionCommonEnv {
	private CommandExecutorIntf commandExecutor;
	
	private boolean endFlag=false;	// exit all (nested) sessions.
	
	private Runnable promptFunc=null;
	
	private Consumer<CmdReturnType> cmdReturnListener=null;
	
	private SessionTranscriptor sessionTranscriptor=null;
	
	private Consumer<String> cmdLineListener=null;
	
	/////////////////////////////////////////////////
	
	public SessionCommonEnv(CommandExecutorIntf commandExecutor) {
		this.commandExecutor = commandExecutor;
	}
	
	/////////////////////////////////////////////////

	public CommandExecutorIntf getCommandExecutorRef() {
		return commandExecutor;
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

	public Environment getEnvironment() {
		return commandExecutor.getEnvironment();
	}

	public void setCmdLineListener(Consumer<String> cmdLineListener) {
		this.cmdLineListener = cmdLineListener;
	}

	public Consumer<String> getCmdLineListener() {
		return cmdLineListener;
	}


}	
