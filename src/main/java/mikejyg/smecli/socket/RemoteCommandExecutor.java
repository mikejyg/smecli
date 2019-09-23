package mikejyg.smecli.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.cmdexecutor.CommandsCommandExecutor;
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
public class RemoteCommandExecutor extends CommandsCommandExecutor {

	private Socket socket;
	private PacketSocket packetSocket;
	
	private CliPacketSerdes cliPacketSerdes = new CliPacketSerdes();
	
	//////////////////////////////////////////////////////////
	
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws Exception {
		CmdReturnType cmdReturn;
		
		cmdReturn = super.execCmd(cmdCall);
		if (cmdReturn.getReturnCode()!=ReturnCode.INVALID_COMMAND)
			return cmdReturn;
		
		ByteBufferAccumulator bba = new ByteBufferAccumulator();
		CliPacketSerdes.serialize(bba, cmdCall);
		packetSocket.send(LvPacket.wrap(bba.toBytes()));

		LvPacket lvPacket;
		lvPacket = packetSocket.receive();
				
		Object obj = cliPacketSerdes.deserialize( ByteBuffer.wrap(lvPacket.getData()) );
		cmdReturn = (CmdReturnType) obj;
			
//		System.out.println("received return: " + cmdReturn.toString());
		
		return cmdReturn;
	}

	@Override
	public String toHelpString() {
		String helpStr = toHelpString();
		
		// TODO: change to OOB messaging for this special case.
		CmdReturnType cmdReturn;
		try {
			cmdReturn = execCmd(new CmdCallType("help"));
		} catch (Exception e) {
			e.printStackTrace();
			return helpStr;
		}
		if ( ! cmdReturn.getReturnCode().isOk() ) {
			helpStr += "\nremote help failed: " + cmdReturn.toString();
		} else {
			helpStr += '\n' + cmdReturn.getResult(); 
		}
		
		return helpStr;
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
