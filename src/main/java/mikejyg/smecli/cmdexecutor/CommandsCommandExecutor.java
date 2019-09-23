package mikejyg.smecli.cmdexecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mikejyg.smecli.CliAnnotation;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdFunction;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CommandStruct;
import mikejyg.smecli.Environment;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * A command executor that holds a list of commands.
 * 
 * @author jgu
 *
 */
public class CommandsCommandExecutor implements CommandExecutorIntf {
	private Environment environmentRef = new Environment();
	
	// command storage & indexes
	
	private List<CommandStruct> commands = new ArrayList<>();
	
	private Map<String, CommandStruct> cmdMap = new TreeMap<>();

	////////////////////////////////////////////////////////////////
	
	public void addCommand(CommandStruct commandStruct) {
		CommandStruct existingCs = cmdMap.get(commandStruct.commandName);
		if (existingCs!=null) {
			commands.remove(existingCs);
		}
		
		commands.add(commandStruct);
		cmdMap.put(commandStruct.commandName, commandStruct);
		
		if (commandStruct.shorthands!=null) {
			for (String s : commandStruct.shorthands) {
				cmdMap.put(s, commandStruct);
			}
		}
	}
	
	public void addCommand(String commandName, String shorthands[], String helpString, CmdFunction cmdFunc) {
		addCommand( new CommandStruct(commandName, shorthands, helpString, cmdFunc) );
	}
	
	public CommandStruct getCommand(String commandName) {
		return cmdMap.get(commandName);
	}
	
	/**
	 * @throws InvokeCommandFailed
	 */
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws Exception  {
		CmdReturnType cmdReturn;
		
		CommandStruct cmdStruct = getCommand(cmdCall.getCommandName());
		
		if (cmdStruct==null) {
			cmdReturn = new CmdReturnType(ReturnCode.INVALID_COMMAND);	
		} else {
			cmdReturn = cmdStruct.cmdFunc.apply(cmdCall);
		}
		
		if ( cmdReturn.getReturnCode().isCmdExecResult() )
			environmentRef.setLastCmdReturn(cmdReturn);
		
		return cmdReturn;
	}
	
	@Override
	public String toHelpString() {
		String helpStr="";
		for (CommandStruct cmd : commands) {
			if (helpStr.isEmpty())
				helpStr = cmd.toString();
			else
				helpStr = helpStr + '\n' + cmd.toString();
		}
		return helpStr;
	}
	
	public void addCommands(Collection<CommandStruct> commandStructs) {
		for (CommandStruct commandStruct : commandStructs) {
			addCommand(commandStruct);
		}
	}
	
	public void addMethods(Object cmdObj) {
		addCommands(CliAnnotation.getCliCommands(cmdObj));
	}
	
	public boolean isEmpty() {
		return commands.isEmpty();
	}

	public Environment getEnvironmentRef() {
		return environmentRef;
	}

	
}
