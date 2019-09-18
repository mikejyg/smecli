package mikejyg.smecli;

/**
 * 
 * @author mikejyg
 *
 */
public class CommandStruct {
	
	/**
	 * not null.
	 */
	public String commandName;
	
	/**
	 * optional, can be null.
	 */
	public 	String [] shorthands;
	
	/**
	 * not null.
	 */
	public String helpString;

	/**
	 * not null.
	 */
	public CmdFunction cmdFunc;
	
	///////////////////////////////////////
	
	public CommandStruct(String commandName, String [] shorthands, String helpString, CmdFunction cmdFunc) {
		this.commandName = commandName;
		this.shorthands = shorthands;
		this.helpString = helpString;
		this.cmdFunc = cmdFunc;
	}
	
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
	
}
