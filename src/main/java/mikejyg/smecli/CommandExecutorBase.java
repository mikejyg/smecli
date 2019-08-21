package mikejyg.smecli;

import java.util.List;
import java.util.function.Function;

import mikejyg.smecli.CliCommands.CommandStruct;
import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * A CommandExecutor manages and executes commands.
 * 
 * @author jgu
 *
 */
public class CommandExecutorBase implements CommandExecutorIntf {
	
	private CliCommands cliCommands;
	
	// working variables
	
	/**
	 * last result of a non-flow control command.
	 */
	private CmdReturnType lastCmdReturn;
	
	////////////////////////////////////////////////////////////////
	
	public CommandExecutorBase() {
		cliCommands = new CliCommands();
		lastCmdReturn = new CmdReturnType(ReturnCode.OK);
	}
	
	/**
	 * @throws InvokeCommandFailed
	 */
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed  {
		CommandStruct cmdStruct = cliCommands.getCommand(cmdCall.getCommandName());
		
		if (cmdStruct==null) {
			lastCmdReturn = new CmdReturnType(ReturnCode.INVALID_COMMAND);
			return lastCmdReturn;
		}
		
		lastCmdReturn = cmdStruct.cmdFunc.apply(cmdCall);
		
		return lastCmdReturn;
	}
	
	@Override
	public String toHelpString() {
		String helpStr="";
		for (CommandStruct cmd : getCommandList()) {
			if (helpStr.isEmpty())
				helpStr = cmd.toString();
			else
				helpStr = helpStr + '\n' + cmd.toString();
		}
		return helpStr;
	}
	
	/** 
	 * @param
	 * @throws InvokeCommandFailed 
	 */
//	public CmdReturnType execCmd(String args[]) throws InvokeCommandFailed {
//		CmdCallType cmdCall = CmdCallType.toCmdCall(args);
//		if (cmdCall.isEmpty())
//			return new CmdReturnType(ReturnCode.NOP);	// no command was executed
//		
//		return execCmd(cmdCall);
//	}
	
	public CmdReturnType getLastCmdReturn() {
		return lastCmdReturn;
	}

	public List<CommandStruct> getCommandList() {
		return cliCommands.getCommands();
	}

	public void addMethods(Object cmdObj) {
		CliAnnotation.addMethods(cliCommands, cmdObj);
	}
	
	public void addCommand(String commandName, String shorthands[], String helpString, Function<CmdCallType, CmdReturnType> cmdFunc) {
		cliCommands.addCommand(commandName, shorthands, helpString, cmdFunc);
	}
	
	
}
