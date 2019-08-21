package mikejyg.socket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * to act as a variable size byte buffer.
 * 
 * @author mikejyg
 *
 */
public class ByteBufferAccumulator {
	private List<byte[]> byteArrays = new ArrayList<>();
	private int totalLength=0;
	
	/////////////////////////////////////////////////////////

	public void put(byte a) {
		ByteBuffer bb = ByteBuffer.allocate(1);
		totalLength++;
		bb.put(a);
		byteArrays.add(bb.array());
	}

	public void putInt(int a) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		totalLength+=4;
		bb.putInt(a);
		byteArrays.add(bb.array());
	}
	
	public void put(byte[] bytes) {
		totalLength += bytes.length;
		byteArrays.add(bytes);
	}
	
	public byte[] toBytes() {
		byte[] bytes = new byte[totalLength];
		
		int idx=0;
		for (byte[] bs : byteArrays) {
			for (int i=0; i<bs.length; i++) {
				bytes[idx++] = bs[i];
			}
		}
		
		return bytes;
	}

	public int getTotalLength() {
		return totalLength;
	}

	
}
