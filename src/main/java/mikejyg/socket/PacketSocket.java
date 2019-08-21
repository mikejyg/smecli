package mikejyg.socket;

import java.io.IOException;
import java.net.Socket;

import mikejyg.socket.TlvPacket.ReadException;
import mikejyg.socket.TlvPacketType.IllegalValueException;

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
	
	public TlvPacket receive() throws IOException, ReadException, IllegalValueException {
		return TlvPacket.read(socket.getInputStream());
	}
	
	public void send(TlvPacket tlvPacket) throws IOException {
		socket.getOutputStream().write(tlvPacket.toBytes());
	}
	
	
}
