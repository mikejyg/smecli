package mikejyg.smecli;

import java.io.PrintStream;

import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * this class holds the settings common to all nested CLI sessions.
 * 
 * @author mikejyg
 *
 */
public class CliBase {
	private CommandExecutorIntf commandExecutorRef;
	
	private String prompt = "> ";
	
	private boolean endFlag=false;	// exit all (nested) sessions.
	
	private CmdReturnType lastCmdReturn = new CmdReturnType(ReturnCode.OK);

	/**
	 * where to print out things, including prompt, results, errors, status...
	 */
	private PrintStream printStream = System.out;

	/////////////////////////////////////////////////
	
	public CliBase() {}
	
	public CliBase(CommandExecutorIntf commandExecutor) {
		this.commandExecutorRef = commandExecutor;
	}
	
	public CommandExecutorIntf getCommandExecutorRef() {
		return commandExecutorRef;
	}

	public void setCommandExecutorRef(CommandExecutorIntf commandExecutorRef) {
		this.commandExecutorRef = commandExecutorRef;
	}

	public CmdReturnType getLastCmdReturn() {
		return lastCmdReturn;
	}

	public void setLastCmdReturn(CmdReturnType lastCmdReturn) {
		this.lastCmdReturn = lastCmdReturn;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public PrintStream getPrintStream() {
		return printStream;
	}

	public void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}

	public boolean isEndFlag() {
		return endFlag;
	}
	
	public void setEndFlag(boolean endFlag) {
		this.endFlag = endFlag;
	}


}	
