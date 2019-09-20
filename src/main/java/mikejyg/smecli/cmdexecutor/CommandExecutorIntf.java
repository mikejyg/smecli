package mikejyg.smecli.cmdexecutor;

import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;

public interface CommandExecutorIntf {

	/**
	 * @return a string of help.
	 */
	public String toHelpString();
	
	/**
	 * execute a command call, and return the result.
	 * @param cmdCall
	 * @return	not null.
	 * @throws Exception
	 */
	public CmdReturnType execCmd(CmdCallType cmdCall) throws Exception;
	
	default public CmdReturnType execCmd(String args[]) throws Exception {
		CmdCallType cmdCall = CmdCallType.toCmdCall(args);
		if (cmdCall.isEmpty())
			return new CmdReturnType(ReturnCode.NOP);	// no command was executed
		
		return execCmd(cmdCall);
	}
	

}
