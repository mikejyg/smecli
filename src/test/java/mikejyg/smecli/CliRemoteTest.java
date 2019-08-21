package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;

public class CliRemoteTest {

	/**
	 * test remote command execution over stream socket.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws IllegalInputCharException
	 * @throws UnexpectedEofException
	 */
	public void runInteractive() throws InterruptedException, IOException, IllegalInputCharException, UnexpectedEofException {
		
		// create a command execution server
		
		CommandExecutorIntf commandExecutor = new CommandExecutor();
		SocketCli socketCli = new SocketCli(commandExecutor);
		
		Thread serverThread = new Thread(()->{
			try {
				socketCli.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		});
		serverThread.start();
		
		// wait for server to be ready
		Thread.sleep(1000);
		
		// create a client
		
		RemoteCommandExecutor rce = new RemoteCommandExecutor();
		rce.connect("localhost", socketCli.getPort());
		
		// start a interactive session
		
		CliAdapter cli = new CliAdapter(rce);
		
		cli.setPrompt("> ");
		
		cli.execAll(new BufferedReader(new InputStreamReader(System.in)));	
		
		socketCli.setStop(true);
		
		rce.close();
		
		serverThread.join();
		
		System.out.println("runInteractive() done.");
		
	}
	
	public static void main(String[] args) throws InterruptedException, IOException, IllegalInputCharException, UnexpectedEofException {
		new CliRemoteTest().runInteractive();
	}
	
	
}
