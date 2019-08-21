package mikejyg.socket;

import java.io.IOException;
import java.net.Socket;

import mikejyg.socket.LvPacket;

/**
 * a stream socket that can send/receive TLV packets.
 * @author mikejyg
 *
 */
public class PacketSocket {
	Socket socket;
	
	public PacketSocket(Socket socket) {
		this.socket = socket;
	}
	
	public LvPacket receive() throws IOException, LvPacket.ReadException {
		return LvPacket.read(socket.getInputStream());
	}
	
	public void send(LvPacket lvPacket) throws IOException {
		socket.getOutputStream().write(lvPacket.toBytes());
	}
	
	
}
