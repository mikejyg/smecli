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
	private RemoteCommandExecutor rce;
	private SessionBase sessionBase;
	private ConsoleSession consoleSession;
	
	/////////////////////////////////////////////////////////
	
	public CliClient() {
		rce = new RemoteCommandExecutor();
		sessionBase = new SessionWithLoop(new SessionCommon(rce));
		consoleSession = new ConsoleSession(sessionBase);
	}
	
	public void connect(String hostname, int port) throws IOException {
		rce.connect(hostname, port);
	}
	
	public void runInteractive() throws IOException, UnexpectedEofException, IllegalInputCharException {
		ConsoleSession.runInteractive(consoleSession);
		consoleSession.flushPrintWriter();
	}

	public void close() throws IOException {
		rce.close();
	}

	
}
