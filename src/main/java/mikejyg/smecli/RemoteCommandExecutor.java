package mikejyg.smecli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import mikejyg.socket.ByteBufferAccumulator;
import mikejyg.socket.LvPacket;
import mikejyg.socket.PacketSocket;

/**
 * A command executor, that executes commands remotely.
 * It sends commands to a remote server for execution, and receives returns.
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
	public CmdReturnType execCmd(CmdCallType cmdCall) throws Exception {
		ByteBufferAccumulator bba = new ByteBufferAccumulator();
		CliPacketSerdes.serialize(bba, cmdCall);
		packetSocket.send(LvPacket.wrap(bba.toBytes()));

		LvPacket lvPacket;
		lvPacket = packetSocket.receive();
				
		CmdReturnType cmdReturn;
		Object obj = cliPacketSerdes.deserialize( ByteBuffer.wrap(lvPacket.getData()) );
		cmdReturn = (CmdReturnType) obj;
			
//		System.out.println("received return: " + cmdReturn.toString());
		
		return cmdReturn;
	}

	@Override
	public String toHelpString() {
		// TODO: change to OOB messaging for this special case.
		
		CmdReturnType cmdReturn;
		try {
			cmdReturn = execCmd(new CmdCallType("help"));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
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
