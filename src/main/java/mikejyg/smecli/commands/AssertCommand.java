package mikejyg.smecli.commands;

import java.util.function.Supplier;

import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.cmdexecutor.CommandsCommandExecutor;

/**
 * This class provides the assert command.
 * @author mikejyg
 *
 */
public class AssertCommand {

	/**
	 * @param getLastCmdResultFunc This command need a way to get the last command return.
	 */
	public static void addToCliCommands(CommandsCommandExecutor cliCommands, Supplier<CmdReturnType> getLastCmdResultFunc) {
		cliCommands.addCommand("assert", null, "assert the value of the last result."
			+ " 1st argument the return code string, 2nd argument(optional) is is the result string."
			, (CmdCallType cmdCall)->{
				String [] args = CliUtils.toArgs(cmdCall);
				if (args.length<1)
					return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument.");
				
				if ( ! args[0].equals(getLastCmdResultFunc.get().getReturnCode().name()) ) {
					return new CmdReturnType(ReturnCode.FAILURE, "return code mismatch: " + getLastCmdResultFunc.get().getReturnCode().name()
							+ " vs " + args[0]);
				}
				
				if ( args.length >=2) {
					if ( ! args[1].equals(getLastCmdResultFunc.get().getResult()) ) {
						return new CmdReturnType(ReturnCode.FAILURE, "result mismatch: " + getLastCmdResultFunc.get().getResult()
								+ " vs " + args[1]);
					}
				}
		
				return new CmdReturnType(ReturnCode.OK);
			});
	}
	
}
