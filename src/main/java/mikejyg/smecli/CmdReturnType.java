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
		NOP,	// no command, comment only, a successful flow control execution...
		OK,
		EXIT,
		END,
		INVALID_COMMAND,
		INVALID_ARGUMENT,
		FAILURE,	// can continue
		FAILURE_UNRECOVERABLE	// cannot continue
		, SCRIPT_ERROR_EXIT;			// used by sub-script to indicate script exit due to error 
		
		public boolean isOk() {
			return ( this==NOP || this==OK || this==EXIT || this==END );
		}
		
	};

	private ReturnCode returnCode; 
	
	/**
	 * the result of a command, if any.
	 */
	private String result;	// device specific results/messages
	
	////////////////////////////////////////////////////////////
	
	public CmdReturnType(ReturnCode returnCode) {
		this.returnCode = returnCode;
		result="";
	}
	
	public CmdReturnType(ReturnCode returnCode, String result) {
		this.returnCode = returnCode;
		this.result = result;
	}
	
	@Override
	public String toString() {
		String str = returnCode.name();
		if ( !result.isEmpty() ) {
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
