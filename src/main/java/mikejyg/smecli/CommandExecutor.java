package mikejyg.smecli;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * a CommandExecutorBase with some built-in commands
 * @author jgu
 *
 */
public class CommandExecutor extends CommandExecutorBase {

	public CommandExecutor() {
		CliAnnotation.addMethods(getCliCommands(), this);
		addCommands();
	}
	
	private void addCommands() {
		getCliCommands().addCommand("assert", null, "assert the value of the last result."
			+ " 1st argument the return code string, 2nd argument(optional) is is the result string."
			, (CmdCallType cmdCall)->{
				String [] args = CliUtils.toArgs(cmdCall);
				if (args.length<1)
					return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument.");
				
				if ( ! args[0].equals(getLastCmdReturn().getReturnCode().name()) ) {
					return new CmdReturnType(ReturnCode.FAILURE, "return code mismatch: " + getLastCmdReturn().getReturnCode().name()
							+ " vs " + args[0]);
				}
				
				if ( args.length >=2) {
					if ( ! args[1].equals(getLastCmdReturn().getResult()) ) {
						return new CmdReturnType(ReturnCode.FAILURE, "result mismatch: " + getLastCmdReturn().getResult()
								+ " vs " + args[1]);
					}
				}
		
				return new CmdReturnType(ReturnCode.OK);
			});
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
	
	@CliCommand(commandName="exit", helpString = "exit current session with an optional argument.")
	public CmdReturnType exitSession(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.EXIT, CliUtils.getArg0(cmdCall));
	}

	@CliCommand(helpString = "exit current session and all parent sessions with an optional argument.")
	public CmdReturnType end(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.END, CliUtils.getArg0(cmdCall));
	}


}
