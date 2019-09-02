package mikejyg.smecli;

public interface CommandExecutorIntf {

	/**
	 * Command not found, or the associated method can not be invoked.
	 * @author jgu
	 *
	 */
	static public class InvokeCommandFailed extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
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
