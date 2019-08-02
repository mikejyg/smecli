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
	
}
