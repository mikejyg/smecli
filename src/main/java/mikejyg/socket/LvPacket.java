package mikejyg.socket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * length value packet, for packetizing a stream.
 * 
 * @author mikejyg
 *
 */
public class LvPacket {
	public static class ReadException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public static final int HEADER_LENGTH=4;
	
	//////////////////////////////////////////////////////////////////
	
	private byte [] data;
	
	//////////////////////////////////////////////////////////////////
	
	/**
	 * create a packet object from a given byte array.
	 * 
	 * @param data
	 * @return
	 */
	public static LvPacket wrap(byte[] data) {
		LvPacket lvPacket = new LvPacket();
		lvPacket.data = data;
		return lvPacket;
	}
	
	/**
	 * read a packet from an input stream, de-serialize
	 * @param inputStream
	 * @return null to indicate socket closed.
	 * @throws IOException 
	 * @throws ReadException 
	 */
	public static LvPacket read(InputStream inputStream) throws IOException, ReadException {
		LvPacket lvPacket = new LvPacket();
		
		byte[] headerBytes = new byte[HEADER_LENGTH];
		int k = inputStream.read(headerBytes);
		if (k!=HEADER_LENGTH)
			return null;
		
		ByteBuffer bb = ByteBuffer.wrap(headerBytes);
		
		int length = bb.getInt();
		
		lvPacket.data = new byte[length];
		
		k = inputStream.read(lvPacket.data);
		if (k!=length)
			throw new ReadException();	// failure to read all data
		
		return lvPacket;
		
	}
	
	/**
	 * serialize.
	 * @return
	 */
	public byte[] toBytes() {
		byte[] bytes = new byte[ data.length + HEADER_LENGTH];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.putInt(data.length);
		bb.put(data);
		
		return bytes;
	}
	
	public byte[] getData() {
		return data;
	}


}
