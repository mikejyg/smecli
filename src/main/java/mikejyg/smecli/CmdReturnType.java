package mikejyg.smecli;

/**
 * this class is to wrap the 2 return values.
 * @author jgu
 *
 */
public class CmdReturnType {
	public ReturnCode returnCode; 
	
	public String result;	// device specific results/messages
	
	public CmdReturnType(ReturnCode returnCode) {
		this.returnCode = returnCode;
	}
	
	public CmdReturnType(ReturnCode returnCode, String result) {
		this.returnCode = returnCode;
		this.result = result;
	}
	
}
