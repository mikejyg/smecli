package mikejyg.smecli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CliPacketSerdes.DesException;
import mikejyg.smecli.CliPacketSerdes.Id;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.socket.ByteBufferAccumulator;
import mikejyg.socket.PacketSocket;
import mikejyg.socket.LvPacket;

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
	
	private void serve(Socket socket) throws IOException, LvPacket.ReadException, InvokeCommandFailed
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
				cmdReturn = commandExecutor.execCmd(cmdCall);
			}

			ByteBufferAccumulator bba = new ByteBufferAccumulator();
			CliPacketSerdes.serialize(bba, cmdReturn);
			packetSocket.send(LvPacket.wrap(bba.toBytes()));
		}
	}
	
	public void accept() throws IOException {
		try (ServerSocket serverSocket = new ServerSocket()) {
			serverSocket.bind(new InetSocketAddress(port));

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

	/**
	 * If stop is true, then the server will exit, upon client disconnect.
	 * @param stop
	 */
	public void setStop(boolean stop) {
		this.stop = stop;
	}


}
