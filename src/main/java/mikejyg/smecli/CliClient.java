package mikejyg.smecli;

import java.io.IOException;

import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;

/**
 * a CliAdapter class that connects to a remote command executor server, using a RemoteCommandExecutor. 
 * 
 * @author mikejyg
 *
 */
public class CliClient {
	private CliBase cliBase = new CliBase();
	private CliSession cli = new CliLoop(cliBase);
	
	private RemoteCommandExecutor rce;

	/////////////////////////////////////////////////////////
	
	public void connect(String hostname, int port) throws IOException {
		rce = new RemoteCommandExecutor();
		cliBase.setCommandExecutorRef(rce);
		
		rce.connect(hostname, port);
	}
	
	public void runInteractive() throws IOException, UnexpectedEofException, IllegalInputCharException {
		CliSession.runInteractive(cli);
	}

	public void close() throws IOException {
		rce.close();
	}

	
}
