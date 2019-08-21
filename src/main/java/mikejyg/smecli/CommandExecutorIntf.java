package mikejyg.smecli;

import mikejyg.smecli.CliCommands.InvokeCommandFailed;

public interface CommandExecutorIntf {
	
	/**
	 * execute a command call, and return the result.
	 * @param cmdCall
	 * @return
	 * @throws InvokeCommandFailed
	 */
	public CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed;
	
	/**
	 * @return a string of help.
	 */
	public String toHelpString();
	
}
