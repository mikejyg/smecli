package mikejyg.smecli;

import java.util.HashMap;
import java.util.Map;

import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * The command execution environment.
 * 
 * @author mikejyg
 *
 */
public class Environment {
	
	/**
	 * Non-string values can be used as well
	 */
	Map<String, Object> env = new HashMap<>();
	
	/**
	 * last result of a non-flow control command.
	 */
	private CmdReturnType lastCmdReturn = new CmdReturnType(ReturnCode.OK);

	////////////////////////////////////////////////////////////
	
	public CmdReturnType getLastCmdReturn() {
		return lastCmdReturn;
	}

	public void setLastCmdReturn(CmdReturnType lastCmdReturn) {
		this.lastCmdReturn = lastCmdReturn;
	}
	
	public Object get(String key) {
		return env.get(key);
	}
	
	public Object put(String key, Object val) {
		return env.put(key, val);
	}
	
}
