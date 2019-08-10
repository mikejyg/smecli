package mikejyg.smecli;

/**
 * the enumeration of return code is used by the calling party to
 * 	decide the next action.
 *   
 * @author jgu
 *
 */
public enum ReturnCode {
	SUCCESS,
	INVALID_COMMAND,
	INVALID_ARGUMENT,
	FAILURE_RECOVERABLE,	// can continue
	FAILURE_UNRECOVERABLE	// cannot continue

};
