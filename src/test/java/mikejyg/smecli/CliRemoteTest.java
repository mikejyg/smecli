package mikejyg.smecli;

import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import mikejyg.cloep.ArgsParser;
import mikejyg.cloep.ArgsParser.ParseException;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.session.ConsoleSession;
import mikejyg.smecli.session.SessionBase;
import mikejyg.smecli.session.SessionCommon;
import mikejyg.smecli.session.SessionWithLoop;
import mikejyg.smecli.socket.RemoteCommandExecutor;
import mikejyg.smecli.socket.SocketCli;

/**
 * @author mikejyg
 *
 */
public class CliRemoteTest {
	
	private class SocketCliThread {
		public SocketCli socketCli;
		public Thread serverThread;
	}
	
	// run options
	ArgsParser argsParser = new ArgsParser();
	
	/**
	 * run an interactive session, with a built-in a server and a client.
	 */
	private boolean interactiveFlag;	// run a interactive test
	
	/**
	 * run the built-in-tests.
	 */
	private boolean bitFlag;
	
	/**
	 * run an interactive session with client only, connecting to a specified port.
	 */
	private boolean clientFlag;		// run a client	
	private int clientRemotePort;
	
	/**
	 * run a built-in server only, listening on a specified port. Port can be 0, for wild card.
	 */
	private boolean serverFlag;
	private int serverPort;
	
	//////////////////////////////////////
	
	public void parseCli(String [] args) throws ParseException {
		argsParser.addOptionWithoutArg('i', null, "run an interactive session", arg->{
			interactiveFlag = true;
		});

		argsParser.addOptionWithoutArg('b', null, "run built-in-tests.", arg->{
			bitFlag=true;
		});
		
		argsParser.addOptionWithArg('c', "", "run a client only, connecting to a server", "port to connect to", arg->{
			clientFlag = true;
			clientRemotePort = Integer.parseInt(arg);
		});
		
		argsParser.addOptionWithArg('s', null, "run a server, using specified port", "listening port", arg->{
			serverFlag=true;
			serverPort = Integer.parseInt(arg);
		});
		
		argsParser.addOptionWithoutArg('h', null, "help", arg->{
			argsParser.printHelp();
		});
		
		argsParser.parse(args);
	}
	
	private SocketCliThread startServer(CommandExecutorIntf commandExecutor, int port) throws InterruptedException {
		SocketCliThread socketCliThread = new SocketCliThread();
		
		socketCliThread.socketCli = new SocketCli(commandExecutor, port);
		
		socketCliThread.serverThread = new Thread(()->{
			try {
				socketCliThread.socketCli.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		});
		socketCliThread.serverThread.start();
		
		// wait for server to be ready
		Thread.sleep(1000);
		
		return socketCliThread;
	}
	
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
		SocketCliThread socketCliThread = startServer(new CommandExecutor(), 0);

		// create a client
		
		RemoteCommandExecutor rce = new RemoteCommandExecutor();
		rce.connect("localhost", socketCliThread.socketCli.getPort());
		
		SessionBase session = new SessionWithLoop(new SessionCommon(rce));
		
		try (InputStreamReader reader = new InputStreamReader(System.in) ) {
			ConsoleSession consoleSession = new ConsoleSession(session);
			consoleSession.setReader(new InputStreamReader(System.in));
			consoleSession.setInteractiveFlag(true);
			consoleSession.execAll();
		}
		
		// shutting down
		socketCliThread.socketCli.setStop(true);
		rce.close();
		socketCliThread.serverThread.join();
		
		System.out.print("runInteractive() done.\n");
	}
	
	@Test
	public void test() throws Exception {
		int[] cmdCnt= {0};
		
		// create a command execution server
		SocketCliThread socketCliThread = startServer(new CommandExecutorIntf() {
			
			@Override
			public String toHelpString() {
				return null;
			}
			
			@Override
			public CmdReturnType execCmd(CmdCallType cmdCall) {
				System.out.print("received cmdCall: " + cmdCall.toString() + '\n');
				cmdCnt[0]++;
				
				if (cmdCall.getCommandName().equals("exit")) {
					assert(cmdCnt[0]==5);
					System.out.print("execCmd() upon exit, all commands received.\n");
				}
				
				return new CmdReturnType(CmdReturnType.ReturnCode.OK, "1");
			}
			
		}, 0);
		
		RemoteCommandExecutor rce = new RemoteCommandExecutor();
		rce.connect("localhost", socketCliThread.socketCli.getPort());

		rce.execCmd(new CmdCallType("abc", "defg"));
		rce.execCmd(new CmdCallType("123", ""));
		rce.execCmd(new CmdCallType("123", new String[]{""}));
		rce.execCmd(new CmdCallType("123", new String[]{"345","678"}));
		rce.execCmd(new CmdCallType("exit"));

		// shutting down
		socketCliThread.socketCli.setStop(true);
		rce.close();
		socketCliThread.serverThread.join();
		
		System.out.print("all done.\n");
	}

	/**
	 * execute according to the test options.
	 * @throws Exception 
	 */
	public void execute() throws Exception {
		if (interactiveFlag) {
			runInteractive();
			
		} else if (bitFlag) {
			test();
			
		} else if (serverFlag) {	// run a server only
			SocketCli socketCli = new SocketCli(new CommandExecutor(), serverPort);
			socketCli.accept();
			
		} else if (clientFlag) {	// run a client only
			CliClient cliClient = new CliClient();
			cliClient.connect("localhost", clientRemotePort);
			cliClient.runInteractive();
			cliClient.close();
			
		} else {
			System.out.print("no action specified.\n");
			System.out.print("usages:\n");
			argsParser.printHelp();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) throws Exception {
		CliRemoteTest cliRemoteTest = new CliRemoteTest();
		cliRemoteTest.parseCli(args);
		cliRemoteTest.execute();
	}
	
	
}
