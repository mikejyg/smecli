package mikejyg.smecli;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * a CommandExecutorBase with some built-in commands
 * @author jgu
 *
 */
public class CommandExecutor extends CommandExecutorBase {

	/**
	 * last result of a non-flow control command.
	 */
	private CmdReturnType lastCmdExecResult;
	
	//////////////////////////////////////////////////////
	
	public CommandExecutor() {
		addMethods(this);
		addCommands();
		lastCmdExecResult = new CmdReturnType(ReturnCode.OK);
	}
	
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed  {
		CmdReturnType cmdReturn = super.execCmd(cmdCall);
		
		if ( cmdReturn.getReturnCode().isCmdExecResult() )
			lastCmdExecResult = cmdReturn;
		
		return cmdReturn;
	}
	
	private void addCommands() {
		AssertCommand.addToCliCommands(this, ()->{ return lastCmdExecResult; });
	}
	
	@CliCommand(helpString = "sleep for specified time (seconds in double).")
	public CmdReturnType sleep(CmdCallType cmdCall) {
		String arg = CliUtils.getArg0(cmdCall);
		if (arg.isEmpty()) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument.");
		}
		
		try {
			double t = Double.parseDouble(arg);
			Thread.sleep((long)(t * 1000));
			
		} catch (NumberFormatException e) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "NumberFormatException: " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			return new CmdReturnType(ReturnCode.FAILURE, "InterruptedException: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			return new CmdReturnType(ReturnCode.FAILURE, "IllegalArgumentException: " + e.getMessage());
		}
		
		return new CmdReturnType(ReturnCode.OK);
	}

	@CliCommand(helpString = "echo arguments.")
	public CmdReturnType echo(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.OK, cmdCall.toArgumentsString());
	}
	
	@CliCommand(shorthands = {"?"}, helpString = "print help.")
	public CmdReturnType help(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.OK, toHelpString());
	}
	

}

