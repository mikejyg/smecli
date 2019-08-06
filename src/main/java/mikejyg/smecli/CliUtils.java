package mikejyg.smecli;

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
		return argumentsStr.split("\\s+");
	}
	
	
}
