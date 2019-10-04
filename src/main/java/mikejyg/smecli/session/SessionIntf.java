package mikejyg.smecli.session;

import java.io.Reader;

import mikejyg.smecli.CmdReturnType;

/**
 * Interface of a session.
 * 
 * @author mikejyg
 *
 */
public interface SessionIntf {
	public void setReader(Reader reader);
	
	public CmdReturnType execAll();
	
	public SessionIntf newSubSession();
	
}
