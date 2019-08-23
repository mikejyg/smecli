package mikejyg.smecli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * this holds commands.
 * 
 * @author jgu
 *
 */
public class CliCommands {
	static public class InvokeCommandFailed extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
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
