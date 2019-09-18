package mikejyg.smecli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * This class holds a set of commands, and executes them as requested.
 * 
 * @author jgu
 *
 */
public class CliCommands implements CommandExecutorIntf {
	// command storage & indexes
	
	private List<CommandStruct> commands = new ArrayList<>();
	
	private Map<String, CommandStruct> cmdMap = new TreeMap<>();

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
			return new CmdReturnType(ReturnCode.INVALID_COMMAND);
		}
		
		cmdReturn = cmdStruct.cmdFunc.apply(cmdCall);
		return cmdReturn;
	}
	
	@Override
	public String toHelpString() {
		String helpStr="";
		for (CommandStruct cmd : getCommands()) {
			if (helpStr.isEmpty())
				helpStr = cmd.toString();
			else
				helpStr = helpStr + '\n' + cmd.toString();
		}
		return helpStr;
	}
	
	public void addMethods(Object cmdObj) {
		CliAnnotation.addMethods(this, cmdObj);
	}
	
	/**
	 * return the list of available commands.
	 * @return
	 */
	public List<CommandStruct> getCommands() {
		return commands;
	}

	public boolean isEmpty() {
		return commands.isEmpty();
	}

	
}
