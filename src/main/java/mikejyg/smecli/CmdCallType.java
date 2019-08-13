package mikejyg.smecli;

/**
 * to package a command name and a arguments string 
 * @author jgu
 *
 */
public class CmdCallType {
	public String commandName;
	
	public String argumentsStr;		// a string containing all arguments, not null.

	public CmdCallType(String commandName, String argumentsStr) {
		this.commandName = commandName;
		this.argumentsStr = argumentsStr;
	}
	
	@Override
	public String toString() {
		if ( ! argumentsStr.isEmpty() )
			return commandName + " " + argumentsStr;
		else
			return commandName;
	}
	
	/**
	 * parse a string command line to the CmdCallType structure.
	 * 
	 * @param cmdString
	 * @return null, if cmdLins is empty.
	 */
	public static CmdCallType toCmdCall(String cmdLine) {
		if (cmdLine==null || cmdLine.isEmpty())
			return null;
		
		CmdCallType cmdCall;
		
		int k = cmdLine.indexOf(' ');
		if (k==-1) {
			cmdCall = new CmdCallType(cmdLine, "");
		} else {
			cmdCall = new CmdCallType( cmdLine.substring(0, k).trim(), cmdLine.substring(k+1).trim() );
		}
		
		return cmdCall;

	}
	
}
