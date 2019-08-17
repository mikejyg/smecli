package mikejyg.smecli;

import mikejyg.smecli.CliCommands.CommandStruct;
import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * A CommandExecutor manages and executes commands.
 * 
 * @author jgu
 *
 */
public class CommandExecutorBase {
	
	private CliCommands cliCommands;
	
	// working variables
	
	/**
	 * last result of a non-flow control command.
	 */
	private CmdReturnType lastCmdReturn;
	
	////////////////////////////////////////////////////////////////
	
	public CommandExecutorBase() {
		cliCommands = new CliCommands();
	}
	
	/**
	 * @throws InvokeCommandFailed
	 */
	public CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed  {
		CommandStruct cmdStruct = cliCommands.getCommand(cmdCall.getCommandName());
		
		if (cmdStruct==null) {
			lastCmdReturn = new CmdReturnType(ReturnCode.INVALID_COMMAND);
			return lastCmdReturn;
		}
		
		lastCmdReturn = cmdStruct.cmdFunc.apply(cmdCall);
		
		return lastCmdReturn;
	}
	
	/** 
	 * @param
	 * @return null, if args is null, or no command is found in args.
	 * @throws InvokeCommandFailed 
	 */
	public CmdReturnType execCmd(String args[]) throws InvokeCommandFailed {
		CmdCallType cmdCall = CmdCallType.toCmdCall(args);
		if (cmdCall==null)
			return null;	// no command was executed
		
		return execCmd(cmdCall);
	}
	
	public CmdReturnType getLastCmdReturn() {
		return lastCmdReturn;
	}

	public CliCommands getCliCommands() {
		return cliCommands;
	}


}
