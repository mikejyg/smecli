package mikejyg.smecli.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.CommandExecutorIntf;
import mikejyg.smecli.socket.CliPacketSerdes.DesException;
import mikejyg.smecli.socket.CliPacketSerdes.Id;
import mikejyg.socket.ByteBufferAccumulator;
import mikejyg.socket.LvPacket;
import mikejyg.socket.PacketSocket;

/**
 * This class presents a command executor interface via a stream socket server,
 *   to let a remote command executor to make a connection and execute commands.
 * 
 * @author jgu
 *
 */
public class SocketCli {
	private CommandExecutorIntf commandExecutor;
	
	private int port;
	
	private boolean stop;
	
	/////////////////////////////////////////////////////////////
	
	public SocketCli(CommandExecutorIntf commandExecutor) {
		this.commandExecutor = commandExecutor;
	}
	
	public SocketCli(CommandExecutorIntf commandExecutor, int port) {
		this.commandExecutor = commandExecutor;
		this.port=port;
	}
	
	private void serve(Socket socket) throws IOException, LvPacket.ReadException
		, DesException, ReturnCode.IllegalValueException {
		PacketSocket packetSocket = new PacketSocket(socket);
		CliPacketSerdes cliPacketSerdes = new CliPacketSerdes();
		while (true) {

			LvPacket lvPacket = packetSocket.receive();
			if (lvPacket==null)
				break;

			Object obj = cliPacketSerdes.deserialize(ByteBuffer.wrap(lvPacket.getData()));
			assert(cliPacketSerdes.getLastId()==Id.CMD_CALL);
			
			CmdCallType cmdCall = (CmdCallType)obj;
//			System.out.println("received cmdCall: " + cmdCall.toString());
			
			CmdReturnType cmdReturn;
			
			// TODO: change to OOB messaging for this special case.
			// help is a special case
			if (cmdCall.getCommandName().equals("help")) {
				cmdReturn = new CmdReturnType(ReturnCode.OK, commandExecutor.toHelpString());
				
			} else {
				try {
					cmdReturn = commandExecutor.execCmd(cmdCall);
				} catch (Exception e) {
					e.printStackTrace();
					cmdReturn = new CmdReturnType(ReturnCode.FAILURE, e.getMessage());
				}
			}

			ByteBufferAccumulator bba = new ByteBufferAccumulator();
			CliPacketSerdes.serialize(bba, cmdReturn);
			packetSocket.send(LvPacket.wrap(bba.toBytes()));
		}
	}

	/**
	 * @param serverPortListener a call back function to get the listening port.
	 * @throws IOException
	 */
	public void accept(Consumer<Integer> serverPortListener) throws IOException {
		try (ServerSocket serverSocket = new ServerSocket()) {
			serverSocket.bind(new InetSocketAddress(port));

			port = serverSocket.getLocalPort();
			serverPortListener.accept(port);

			while (!stop) {
				try (Socket clientSocket = serverSocket.accept()) {
	
					serve(clientSocket);	
	
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				System.out.print("client session ended.\n");
			}

			System.out.print("exiting server...\n");
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void accept() throws IOException {
		accept( port->{ System.out.print("server port: " + port + '\n'); } );
	}
	
	public int getPort() {
		return port;
	}

	/**
	 * If stop is true, then the server will exit, upon client disconnect.
	 * @param stop
	 */
	public void setStop(boolean stop) {
		this.stop = stop;
	}


}
