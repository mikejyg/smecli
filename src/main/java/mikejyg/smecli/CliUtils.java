package mikejyg.smecli;

import java.util.Arrays;

/**
 * Some optional, but useful utilities.
 * 
 * @author jgu
 *
 */
public class CliUtils {

	/**
	 * tokenize an arguments string, using white space as separators.
	 * 
	 * for now, no quoting or escape sequences are recognized.
	 * 
	 * @return
	 */
	public static String[] toArgs(String argumentsStr) {
		return argumentsStr.split("\\s+");
	}
	
	/**
	 * This is the way to get args[] from this structure.
	 * 
	 * If args[] is not already populated, it parses fromargumentStr, using white spaces as separators.
	 * 
	 */
	public static String[] toArgs(CmdCallType cmdCall) {
		if ( cmdCall.getArgs()!=null )
			return cmdCall.getArgs();
		else
			return CliUtils.toArgs(cmdCall.getArgumentsStr());
	}
	
	/**
	 * Get the first argument. It uses the same mechanism as toArgs().
	 * 
	 * @param cmdCall
	 */
	public static String getArg0(CmdCallType cmdCall) {
		String args[]=toArgs(cmdCall);
		if (args.length==0)
			return "";
		else
			return args[0];
	}
	
	/**
	 * remove everything after the comment symbol #. 
	 * @return
	 */
	static public String[] removeEndComments(String args[]) {
		int i=0;
		for (String s : args) {
			if ( !s.isEmpty() && s.charAt(0)=='#' )
				break;
			i++;
		}
		return Arrays.copyOfRange(args, 0, i);
	}
	
	
}
