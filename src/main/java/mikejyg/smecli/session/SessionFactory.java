package mikejyg.smecli.session;

import mikejyg.smecli.cmdexecutor.CommandExecutorFactory;
import mikejyg.smecli.cmdexecutor.CommandExecutorIntf;
import mikejyg.smecli.commands.SourceCommand;
import mikejyg.smecli.commands.SystemCommand;

public class SessionFactory {

	public static ConsoleSession buildLoadedConsoleSession(CommandExecutorIntf commandExecutor) {
		Session session = new SessionWithLoop(commandExecutor);
		commandExecutor.addCommands(new SessionCommands(session).getCommands());	// end, exit, continueOnError
		
		ConsoleSession consoleSession = new ConsoleSession(session);
	
		commandExecutor.addMethods( new SourceCommand(()->{return consoleSession.newSubSession();}) );	// source
	
		commandExecutor.addMethods(
				new SystemCommand(consoleSession.getConsoleSessionCommonRef().getPrintWriter()) );	// system

		return consoleSession;
		
	}
	
	public static ConsoleSession buildLoadedConsoleSession() {
		CommandExecutorIntf commandExecutor = CommandExecutorFactory.makeLoadedCommandExecutor();
		
		return buildLoadedConsoleSession(commandExecutor);
	}
	
	
}
