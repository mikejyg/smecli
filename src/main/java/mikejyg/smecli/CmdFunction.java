package mikejyg.smecli;

/**
 * This class is different than the Java Function, that it throws Exception. 
 * 
 * @author mikejyg
 *
 */
public interface CmdFunction {
	public CmdReturnType apply(CmdCallType cmdCall) throws Exception;
}
