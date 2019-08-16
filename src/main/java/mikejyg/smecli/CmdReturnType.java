package mikejyg.smecli;

/**
 * this class is to wrap the 2 return values.
 * @author jgu
 *
 */
public class CmdReturnType {
	
	/**
	 * the enumeration of return code is used by the calling party to
	 *   decide the next action.
	 * @author jgu
	 *
	 */
	static public enum ReturnCode {
		SUCCESS,
		INVALID_COMMAND,
		INVALID_ARGUMENT,
		FAILURE_RECOVERABLE,	// can continue
		FAILURE_UNRECOVERABLE	// cannot continue

	};

	private ReturnCode returnCode; 
	
	/**
	 * the result of a command, if any.
	 */
	private String result;	// device specific results/messages
	
	////////////////////////////////////////////////////////////
	
	public CmdReturnType(ReturnCode returnCode) {
		this.returnCode = returnCode;
	}
	
	public CmdReturnType(ReturnCode returnCode, String result) {
		this.returnCode = returnCode;
		this.result = result;
	}
	
	@Override
	public String toString() {
		String str = returnCode.name();
		if ( result!=null && !result.isEmpty() ) {
			str = str + ' ' + result; 
		}
		return str;
	}
	
	public ReturnCode getReturnCode() {
		return returnCode;
	}

	public String getResult() {
		return result;
	}

}
