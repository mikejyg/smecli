package mikejyg.smecli;

import java.util.Arrays;

/**
 * to package a command name and a arguments string
 * 
 * Since a command can originate from 2 sources: an input stream, or as command line arguments (args[]),
 *   the argument can be either a single String, or String[].
 *   
 * This structure accommodates both.
 *  
 * @author jgu
 *
 */
public class CmdCallType {
	private String commandName = new String();
	
	private boolean argumentsInArgsFlag=false;
	
	/**
	 * contains the already parsed command-line args[].
	 */
	private String[] args = new String[0];
	
	/**
	 * This is a string of the entire arguments, except,
	 *   when the arguments are in args[], it points to args[0] 
	 */
	private String argumentsStr=new String();

	////////////////////////////////////////////////////////////
	
	/**
	 * construct an empty cmdCall.
	 */
	public CmdCallType() {}
	
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
			argumentsInArgsFlag=true;
			this.args=args;
			argumentsStr = args[0];			
		}
	}
	
	public boolean isEmpty() {
		return commandName.isEmpty();
	}
	
	/**
	 * @return a string contains the entire arguments.
	 */
	public String toArgumentsString() {
		if ( ! argumentsInArgsFlag )
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
	 * @return
	 */
	public static CmdCallType toCmdCall(String cmdLine) {
		if (cmdLine.isEmpty())
			return new CmdCallType();
		
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
		if (args.length==0)
			return new CmdCallType();
		
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
	
	public boolean isArgumentsInArgsFlag() {
		return argumentsInArgsFlag;
	}


}
