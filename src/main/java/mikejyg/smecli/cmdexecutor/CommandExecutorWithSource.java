package mikejyg.smecli.cmdexecutor;

import java.util.function.Supplier;

import mikejyg.smecli.CliAnnotation;
import mikejyg.smecli.Environment;
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
	
	public CommandExecutorWithSource(Environment environmentRef) {
		super(environmentRef);
	}
	
	public void setNewCliSessionFunc(Supplier<SessionIntf> newCliSessionFunc) {
		sourceCommand = new SourceCommand(newCliSessionFunc);
		CliAnnotation.addMethods(this, sourceCommand);
	}
	
}
