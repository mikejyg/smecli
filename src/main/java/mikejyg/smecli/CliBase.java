package mikejyg.smecli;

import java.io.PrintStream;
import java.util.function.Consumer;

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
	
	private CmdReturnType lastCmdExecResult = new CmdReturnType(ReturnCode.OK);

	/**
	 * where to print out things, including prompt, results, errors, status...
	 */
	private PrintStream printStream = System.out;

	/**
	 * whether there is an prompt outstanding.
	 */
	private boolean prompted;
	
	/**
	 * an optional listener for capturing all command executions.
	 */
	private Consumer<String> cmdExecListener;
	
	private Consumer<CmdReturnType> cmdReturnListener;
	
	/////////////////////////////////////////////////
	
	public CliBase() {}
	
	public CliBase(CommandExecutorIntf commandExecutor) {
		this.commandExecutorRef = commandExecutor;
		cmdReturnListener = (r)->{ processResults(r); };
	}
	
	/**
	 * override this method to get the command returns.
	 * 
	 * @param cmdReturn
	 */
	protected void processResults(CmdReturnType cmdReturn) {
		getPrintStream().println(cmdReturn.getReturnCode().name());
		
		if ( ! cmdReturn.getResult().isEmpty() ) {
			getPrintStream().println(cmdReturn.getResult());
			setPrompted(false);
		}
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

	public boolean isPrompted() {
		return prompted;
	}

	public void setPrompted(boolean prompted) {
		this.prompted = prompted;
	}

	public Consumer<String> getCmdExecListener() {
		return cmdExecListener;
	}

	public void setCmdExecListener(Consumer<String> cmdExecListener) {
		this.cmdExecListener = cmdExecListener;
	}

	public Consumer<CmdReturnType> getCmdReturnListener() {
		return cmdReturnListener;
	}

	public void setCmdReturnListener(Consumer<CmdReturnType> cmdReturnListener) {
		this.cmdReturnListener = cmdReturnListener;
	}


}	
