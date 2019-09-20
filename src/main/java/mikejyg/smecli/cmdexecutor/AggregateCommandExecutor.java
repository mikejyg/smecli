package mikejyg.smecli.cmdexecutor;

import java.util.ArrayList;
import java.util.List;

import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * A CommandExecutor that uses a list of command executors.
 * 
 * NOTE: all command executors should share a same environment.
 * 
 * @author jgu
 *
 */
public class AggregateCommandExecutor implements CommandExecutorIntf {
	
	/**
	 * a list of command executors to consult, before cliCommands.
	 */
	private List<CommandExecutorIntf> commandExecutorList = new ArrayList<>();
	
	////////////////////////////////////////////////////////////////
	
	public AggregateCommandExecutor() {
	}
	
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws Exception  {
		for (CommandExecutorIntf ce : commandExecutorList) {
			CmdReturnType cmdReturn = ce.execCmd(cmdCall);
			if (cmdReturn.getReturnCode()==ReturnCode.INVALID_COMMAND)
				continue;
			return cmdReturn;
		}
		
		return new CmdReturnType(ReturnCode.INVALID_COMMAND);
	}
	
	@Override
	public String toHelpString() {
		String helpStr="";
		for (CommandExecutorIntf ce : commandExecutorList) {
			if (helpStr.isEmpty())
				helpStr = ce.toHelpString();
			else
				helpStr = helpStr + '\n' + ce.toHelpString();
		}
		
		return helpStr;
	}
	
	/**
	 * this single access method allows user to insert executors at desired list positions. 
	 */
	public List<CommandExecutorIntf> getCommandExecutorList() {
		return commandExecutorList;
	}

	
}

