package mikejyg.smecli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CliPacketSerdes;
import mikejyg.socket.ByteBufferAccumulator;
import mikejyg.socket.LvPacket;
import mikejyg.socket.PacketSocket;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * sends commands to a remote for execution, and receives returns.
 * 
 * @author jgu
 *
 */
public class RemoteCommandExecutor implements CommandExecutorIntf {

	private Socket socket;
	private PacketSocket packetSocket;
	
	private CliPacketSerdes cliPacketSerdes = new CliPacketSerdes();
	
	//////////////////////////////////////////////////////////
	
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws InvokeCommandFailed {
		try {
			ByteBufferAccumulator bba = new ByteBufferAccumulator();
			CliPacketSerdes.serialize(bba, cmdCall);
			packetSocket.send(LvPacket.wrap(bba.toBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}

		LvPacket lvPacket;
		try {
			lvPacket = packetSocket.receive();
		} catch (IOException | LvPacket.ReadException e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}
		
		CmdReturnType cmdReturn;
		try {
			Object obj = cliPacketSerdes.deserialize( ByteBuffer.wrap(lvPacket.getData()) );
			cmdReturn = (CmdReturnType) obj;
			
		} catch (ReturnCode.IllegalValueException | CliPacketSerdes.DesException e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}

//		System.out.println("received return: " + cmdReturn.toString());
		
		return cmdReturn;
	}

	@Override
	public String toHelpString() {
		// TODO: change to OOB messaging for this special case.
		
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
