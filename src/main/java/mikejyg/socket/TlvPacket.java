package mikejyg.socket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * a tag-length-value packet
 * 
 * @author mikejyg
 * 
 * 32-bit packet type
 * 32-bit data length
 * data...
 *
 */
public class TlvPacket {
	public static class ReadException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public static final int HEADER_LENGTH=8;
	
	//////////////////////////////////////////////////////////////////
	
	private TlvPacketType packetType;
	
	private byte []  data;
	
	//////////////////////////////////////////////////////////////////
	
	public TlvPacket() {
		packetType=TlvPacketType.CONTAINER;
	}
	
	public TlvPacket(String str) {
		packetType=TlvPacketType.STRING;
		data = str.getBytes(StandardCharsets.UTF_8);
	}
	
	public static TlvPacket wrap(byte[] data) {
		TlvPacket tlvPacket = new TlvPacket();
		tlvPacket.data = data;
		return tlvPacket;
	}
	
	/**
	 * read a packet from an input stream
	 * @param inputStream
	 * @return null to indicate socket closed.
	 * @throws IOException 
	 * @throws ReadException 
	 * @throws IllegalValueException 
	 */
	public static TlvPacket read(InputStream inputStream) throws IOException, ReadException, TlvPacketType.IllegalValueException {
		TlvPacket tlvPacket = new TlvPacket();
		
		byte[] headerBytes = new byte[HEADER_LENGTH];
		int k = inputStream.read(headerBytes);
		if (k!=HEADER_LENGTH)
			return null;
		
		ByteBuffer bb = ByteBuffer.wrap(headerBytes);
		
		tlvPacket.packetType = TlvPacketType.getTlvPacketType(bb.getInt());
		int length = bb.getInt();
		
		tlvPacket.data = new byte[length];
		
		k = inputStream.read(tlvPacket.data);
		if (k!=length)
			throw new ReadException();	// failure to read all data
		
		return tlvPacket;
		
	}
	
	public byte[] toBytes() {
		byte[] bytes = new byte[ data.length + HEADER_LENGTH];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.putInt(packetType.intValue());
		bb.putInt(data.length);
		bb.put(data);
		
		return bytes;
	}
	
	public byte[] getData() {
		return data;
	}

	
}
