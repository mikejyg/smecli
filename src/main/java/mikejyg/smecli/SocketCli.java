package mikejyg.smecli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.socket.PacketSocket;
import mikejyg.socket.TlvPacket;
import mikejyg.socket.TlvPacket.ReadException;
import mikejyg.socket.TlvPacketType.IllegalValueException;

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
	
	private void serve(Socket socket) throws IOException, ReadException, IllegalValueException, InvokeCommandFailed {
		PacketSocket packetSocket = new PacketSocket(socket);
		while (true) {

			TlvPacket tlvPacket = packetSocket.receive();
			if (tlvPacket==null)
				break;

			CmdCallType cmdCall = new CmdCallType(tlvPacket.getData());
//			System.out.println("received cmdCall: " + cmdCall.toString());
			
			CmdReturnType cmdReturn;
			
			// help is a special case
			if (cmdCall.getCommandName().equals("help")) {
				cmdReturn = new CmdReturnType(ReturnCode.OK, commandExecutor.toHelpString());
				
			} else {
				cmdReturn = commandExecutor.execCmd(cmdCall);
			}

			packetSocket.send(TlvPacket.wrap(cmdReturn.toBytes()));
		}
	}
	
	public void accept() throws IOException {
		try (ServerSocket serverSocket = new ServerSocket()) {
			serverSocket.bind(new InetSocketAddress(0));

			port = serverSocket.getLocalPort();
			System.out.println("server port: " + port);

			while (!stop) {
				try (Socket clientSocket = serverSocket.accept()) {
	
					serve(clientSocket);	
	
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				System.out.println("client session ended.");
			}

			System.out.println("exiting server...");
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public int getPort() {
		return port;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}


}
