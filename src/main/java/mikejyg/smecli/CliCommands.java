package mikejyg.smecli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * This class holds a set of commands, and executes them as requested.
 * 
 * @author jgu
 *
 */
public class CliCommands implements CommandExecutorIntf {
	static class CommandStruct {
		String commandName;
		String [] shorthands;
		String helpString;

		Function<CmdCallType, CmdReturnType> cmdFunc;
		
		@Override
		public String toString() {
			String str = commandName;
			if (shorthands!=null) {
				for (String s : shorthands) {
					str += ", " + s;
				}
			}
			str += "\t" + helpString;
			
			return str;
		}
	};
	
	// command storage & indexes
	
	private List<CommandStruct> commands = new ArrayList<>();
	
	private Map<String, CommandStruct> cmdMap = new TreeMap<>();

	public void addCommand(String commandName, String shorthands[], String helpString, Function<CmdCallType, CmdReturnType> cmdFunc) {
		CommandStruct commandStruct=new CommandStruct();
		
		commandStruct.commandName = commandName;
		commandStruct.shorthands = shorthands;
		commandStruct.helpString = helpString;
		commandStruct.cmdFunc = cmdFunc;
		
		CommandStruct existingCs = cmdMap.get(commandStruct.commandName);
		if (existingCs!=null) {
			commands.remove(existingCs);
		}
		
		commands.add(commandStruct);
		cmdMap.put(commandStruct.commandName, commandStruct);
		
		if (shorthands!=null) {
			for (String s : shorthands) {
				cmdMap.put(s, commandStruct);
			}
		}
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
