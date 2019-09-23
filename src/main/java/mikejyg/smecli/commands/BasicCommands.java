package mikejyg.smecli.commands;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.cmdexecutor.CommandExecutorIntf;

public class BasicCommands {
	
	private CommandExecutorIntf commandExecutorIntfRef;
	
	/////////////////////////////////////////////////////////////////////
	
	public BasicCommands(CommandExecutorIntf commandExecutorIntf) {
		this.commandExecutorIntfRef = commandExecutorIntf;
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
		return new CmdReturnType(ReturnCode.OK, commandExecutorIntfRef.toHelpString());
	}
	
	
}
