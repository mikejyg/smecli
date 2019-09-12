package mikejyg.smecli;

public interface CommandExecutorIntf {

	/**
	 * execute a command call, and return the result.
	 * @param cmdCall
	 * @return
	 * @throws Exception
	 */
	public CmdReturnType execCmd(CmdCallType cmdCall) throws Exception;
	
	/**
	 * @return a string of help.
	 */
	public String toHelpString();
	
}
