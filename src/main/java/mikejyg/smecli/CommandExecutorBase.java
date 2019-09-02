package mikejyg.smecli;

import java.util.ArrayList;
import java.util.List;

import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * A CommandExecutor manages and executes commands.
 * 
 * @author jgu
 *
 */
public class CommandExecutorBase extends CliCommands {
	
	/**
	 * a list of command executors to consult, before cliCommands.
	 */
	private List<CommandExecutorIntf> commandExecutorList = new ArrayList<>();
	
	////////////////////////////////////////////////////////////////
	
	public CommandExecutorBase() {
	}
	
	/**
	 * @throws InvokeCommandFailed
	 */
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed  {
		for (CommandExecutorIntf ce : commandExecutorList) {
			try {
				CmdReturnType cmdReturn = ce.execCmd(cmdCall);
				if (cmdReturn.getReturnCode()==ReturnCode.INVALID_COMMAND)
					continue;
				return cmdReturn;
				
			} catch (InvokeCommandFailed e) {
				;
			}
		}
		
		return super.execCmd(cmdCall);
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
		
		if (helpStr.isEmpty())
			helpStr = super.toHelpString();
		else
			helpStr = helpStr + '\n' + super.toHelpString();
		
		return helpStr;
	}
	
	/** 
	 * @param
	 * @throws InvokeCommandFailed 
	 */
	public CmdReturnType execCmd(String args[]) throws InvokeCommandFailed {
		CmdCallType cmdCall = CmdCallType.toCmdCall(args);
		if (cmdCall.isEmpty())
			return new CmdReturnType(ReturnCode.NOP);	// no command was executed
		
		return execCmd(cmdCall);
	}
	
	/**
	 * this single access method allows user to insert executors at desired list positions. 
	 */
	public List<CommandExecutorIntf> getCommandExecutorList() {
		return commandExecutorList;
	}

	public void addObjectMethods(Object cmdObj) {
		CliAnnotation.addMethods(this, cmdObj);
	}
	
	
}

