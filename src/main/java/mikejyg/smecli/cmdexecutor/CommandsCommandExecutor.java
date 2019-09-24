package mikejyg.smecli.cmdexecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.CommandStruct;
import mikejyg.smecli.Environment;

/**
 * A command executor that holds a list of commands.
 * 
 * @author jgu
 *
 */
public class CommandsCommandExecutor implements CommandExecutorIntf {
	private Environment environment;
	
	// command storage & indexes
	
	private List<CommandStruct> commands = new ArrayList<>();
	
	private Map<String, CommandStruct> cmdMap = new TreeMap<>();

	////////////////////////////////////////////////////////////////
	
	public CommandsCommandExecutor() {
		environment = new Environment();
	}
	
	public CommandsCommandExecutor(Environment environment) {
		this.environment = environment;
	}
	
	@Override
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
	
	private CommandStruct getCommand(String commandName) {
		return cmdMap.get(commandName);
	}
	
	public boolean hasCommand(CmdCallType cmdCall) {
		return getCommand(cmdCall.getCommandName()) != null ? true : false;
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
			environment.setLastCmdReturn(cmdReturn);
		
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
	
	public boolean isEmpty() {
		return commands.isEmpty();
	}

	@Override
	public Environment getEnvironment() {
		return environment;
	}

	
}
