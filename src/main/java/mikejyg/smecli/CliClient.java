package mikejyg.smecli;

import java.io.IOException;

import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.session.ConsoleSession;
import mikejyg.smecli.session.Session;
import mikejyg.smecli.session.SessionWithLoop;
import mikejyg.smecli.socket.RemoteCommandExecutor;

/**
 * a CliAdapter class that connects to a remote command executor server, using a RemoteCommandExecutor. 
 * 
 * @author mikejyg
 *
 */
public class CliClient {
	private RemoteCommandExecutor rce;
	private Session sessionBase;
	private ConsoleSession consoleSession;
	
	/////////////////////////////////////////////////////////
	
	public CliClient() {
		rce = new RemoteCommandExecutor();
		sessionBase = new Session(rce);
		consoleSession = new ConsoleSession(sessionBase);
		SessionWithLoop sessionWithLoop = new SessionWithLoop(sessionBase);
		rce.addCommands(sessionWithLoop.getCommandStructs());
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
