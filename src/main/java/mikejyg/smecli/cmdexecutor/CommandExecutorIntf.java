package mikejyg.smecli.cmdexecutor;

import java.util.Collection;

import mikejyg.smecli.CliAnnotation;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdFunction;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CommandStruct;
import mikejyg.smecli.Environment;
import mikejyg.smecli.CmdReturnType.ReturnCode;

public interface CommandExecutorIntf {

	public Environment getEnvironment();
	
	public void addCommand(CommandStruct commandStruct);
	
	/**
	 * execute a command call, and return the result.
	 * @param cmdCall
	 * @return	not null.
	 * @throws Exception
	 */
	public CmdReturnType execCmd(CmdCallType cmdCall) throws Exception;
	
	/**
	 * @return a string of help.
	 */
	public String toHelpString();
	
	//////////////////////////////////////////////////
	
	default public void addCommand(String commandName, String shorthands[], String helpString, CmdFunction cmdFunc) {
		addCommand( new CommandStruct(commandName, shorthands, helpString, cmdFunc) );
	}
	
	default public void addCommands(Collection<CommandStruct> commandStructs) {
		for (CommandStruct commandStruct : commandStructs) {
			addCommand(commandStruct);
		}
	}
	
	default public void addMethods(Object cmdObj) {
		addCommands(CliAnnotation.getCliCommands(cmdObj));
	}
	
	default public CmdReturnType execCmd(String args[]) throws Exception {
		CmdCallType cmdCall = CmdCallType.toCmdCall(args);
		if (cmdCall.isEmpty())
			return new CmdReturnType(ReturnCode.NOP);	// no command was executed
		
		return execCmd(cmdCall);
	}
	

}
