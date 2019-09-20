package mikejyg.smecli;

import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * The command execution environment.
 * 
 * @author mikejyg
 *
 */
public class Environment {
	/**
	 * last result of a non-flow control command.
	 */
	private CmdReturnType lastCmdReturn = new CmdReturnType(ReturnCode.OK);

	public CmdReturnType getLastCmdReturn() {
		return lastCmdReturn;
	}

	public void setLastCmdReturn(CmdReturnType lastCmdReturn) {
		this.lastCmdReturn = lastCmdReturn;
	}
	
	
}
