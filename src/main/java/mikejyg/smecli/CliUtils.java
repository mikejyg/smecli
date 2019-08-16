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
	 * @param argumentsStr
	 * @return
	 */
	static public String[] toArgs(String argumentsStr) {
		if (argumentsStr==null)
			return null;
		return argumentsStr.split("\\s+");
	}
	
	/**
	 * remove everything after the comment symbol #. 
	 * @param args
	 * @return
	 */
	static public String[] removeEndComments(String args[]) {
		if (args==null)
			return null;
		
		int i=0;
		for (String s : args) {
			if ( s!=null && !s.isEmpty() && s.charAt(0)=='#' )
				break;
			i++;
		}
		return Arrays.copyOfRange(args, 0, i);
	}
	
	
}
