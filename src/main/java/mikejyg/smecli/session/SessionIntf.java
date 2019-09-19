package mikejyg.smecli.session;

import java.io.IOException;
import java.io.Reader;

import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType;

/**
 * Interface of a session.
 * 
 * @author mikejyg
 *
 */
public interface SessionIntf {
	public void setReader(Reader reader);
	
	public CmdReturnType execAll() throws IOException, UnexpectedEofException, IllegalInputCharException;
	
}
