package mikejyg.smecli.cmdexecutor;

import mikejyg.smecli.commands.AssertCommand;
import mikejyg.smecli.commands.BasicCommands;

public class CommandExecutorFactory {
	
	static public CommandsCommandExecutor makeLoadedCommandExecutor() {
		CommandsCommandExecutor commandExecutor = new CommandsCommandExecutor();
		commandExecutor.addMethods(new BasicCommands(commandExecutor));		// help, echo, sleep
		commandExecutor.addCommand(AssertCommand.getCommandStruct(
				() -> { return commandExecutor.getEnvironment().getLastCmdReturn();}) );	// assert

		return commandExecutor;
	}
	
}
