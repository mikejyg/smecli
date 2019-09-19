package mikejyg.smecli;

import java.util.function.Supplier;

import mikejyg.smecli.session.SessionIntf;
import mikejyg.smecli.session.SourceCommand;

/**
 * A command executor with source command.
 * 
 * @author mikejyg
 *
 */
public class CommandExecutorWithSource extends CommandExecutor {
	
	private SourceCommand sourceCommand;
	
	public void setNewCliSessionFunc(Supplier<SessionIntf> newCliSessionFunc) {
		sourceCommand = new SourceCommand(newCliSessionFunc);
		addObjectMethods( sourceCommand );
	}
	
}
