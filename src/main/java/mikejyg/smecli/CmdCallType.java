package mikejyg.smecli;

import java.util.Arrays;

/**
 * to package a command name and a arguments string
 * 
 * Since a command can originate from 2 sources: an input stream, or as command line arguments (args[]),
 *   the argument can be either a single String, or String[].
 *   
 * For efficiency reasons, this structure accommodates both.
 *  
 * @author jgu
 *
 */
public class CmdCallType {
	private String commandName;
	
	/**
	 * contains the already parsed command-line args[].
	 * If it is null, then use argumentsStr.
	 */
	private String args[];
	
	/**
	 * if args[] is null, then this string containing all arguments.
	 * otherwise, it references args[0].
	 */
	private String argumentsStr;

	/**
	 * constructor with a single parsed argument string.
	 * @param commandName
	 * @param argumentsStr
	 */
	public CmdCallType(String commandName, String argumentsStr) {
		this.commandName = commandName;
		this.argumentsStr = argumentsStr;
	}
	
	/**
	 * constructor with already parsed command-line args[].
	 * @param commandName
	 * @param args
	 */
	public CmdCallType(String commandName, String args[]) {
		this.commandName = commandName;
		if (args.length!=0) {
			this.args = args;
			argumentsStr = args[0];
			
		} else {
			argumentsStr="";
		}
	}
	
	/**
	 * @return a string contains the entire arguments, not null.
	 */
	public String toArgumentsString() {
		if (args==null)
			return argumentsStr;
		String str="";
		for (String s : args) {
			if (str.isEmpty())
				str = s;
			else
				str = str + " " + s;
		}
		return str;
	}
	
	@Override
	public String toString() {
		String str = toArgumentsString();
		if ( ! str.isEmpty() )
			return commandName + " " + str;
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
	
	public static CmdCallType toCmdCall(String args[]) {
		if (args==null || args.length==0)
			return null;
		
		return new CmdCallType(args[0], Arrays.copyOfRange(args, 1, args.length));
	}

	public String getCommandName() {
		return commandName;
	}

	public String[] getArgs() {
		return args;
	}

	public String getArgumentsStr() {
		return argumentsStr;
	}
	
	
}
