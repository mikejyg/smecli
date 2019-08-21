package mikejyg.smecli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.socket.PacketSocket;
import mikejyg.socket.TlvPacket;
import mikejyg.socket.TlvPacket.ReadException;
import mikejyg.socket.TlvPacketType.IllegalValueException;

/**
 * sends commands to a remote for execution, and receives returns.
 * 
 * @author jgu
 *
 */
public class RemoteCommandExecutor implements CommandExecutorIntf {

	private Socket socket;
	private PacketSocket packetSocket;
	
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed {
		try {
			packetSocket.send(TlvPacket.wrap(cmdCall.toBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}

		TlvPacket tlvPacket;
		try {
			tlvPacket = packetSocket.receive();
		} catch (IOException | ReadException | IllegalValueException e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}
		
		CmdReturnType cmdReturn;
		try {
			cmdReturn = new CmdReturnType(tlvPacket.getData());
		} catch (mikejyg.smecli.CmdReturnType.ReturnCode.IllegalValueException e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}

//		System.out.println("received return: " + cmdReturn.toString());
		
		return cmdReturn;
	}

	@Override
	public String toHelpString() {
		CmdReturnType cmdReturn;
		try {
			cmdReturn = execCmd(new CmdCallType("help"));
		} catch (InvokeCommandFailed e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}
		if ( ! cmdReturn.getReturnCode().isOk() )
			return "remote help failed: " + cmdReturn.toString();
		
		return cmdReturn.getResult();
	}

	public void connect(String hostname, int port) throws IOException {
		socket = new Socket();
		
		socket.connect(new InetSocketAddress(hostname, port));
		
		packetSocket = new PacketSocket(socket);
	}
	
	public void close() throws IOException {
		socket.close();
	}
	
	
}
