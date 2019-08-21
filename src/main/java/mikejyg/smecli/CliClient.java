package mikejyg.smecli;

import java.io.IOException;

/**
 * a CliAdapter class that connects to a remote command executor server, using a RemoteCommandExecutor. 
 * 
 * @author mikejyg
 *
 */
public class CliClient extends CliAdapter {
	private RemoteCommandExecutor rce;

	/////////////////////////////////////////////////////////
	
	public void connect(String hostname, int port) throws IOException {
		rce = new RemoteCommandExecutor();
		setCommandExecutor(rce);
		
		rce.connect(hostname, port);
	}
	
	public void close() throws IOException {
		rce.close();
	}
	
	
}
