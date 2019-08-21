package mikejyg.smecli;

import java.nio.ByteBuffer;

import org.junit.Test;

import mikejyg.smecli.CliPacketSerdes.DesException;
import mikejyg.smecli.CliPacketSerdes.Id;
import mikejyg.smecli.CmdReturnType.ReturnCode.IllegalValueException;
import mikejyg.socket.ByteBufferAccumulator;

public class CliSerdesTest {

	@Test
	public void test() throws DesException, IllegalValueException {
		String msg="halt";
		
		ByteBufferAccumulator bba = new ByteBufferAccumulator();
		
		CliPacketSerdes.serialize(bba, msg.getBytes(CmdCallType.charset));
		
		byte[] bytes = bba.toBytes();
		
		CliPacketSerdes cliSerdes = new CliPacketSerdes();
		
		Object obj = cliSerdes.deserialize(ByteBuffer.wrap(bytes));
		
		assert( cliSerdes.getLastId() == Id.OOB );
		assert( obj instanceof byte[] );
		
		String desMsg = new String( (byte[])obj, CmdCallType.charset);
		
		assert(msg.contentEquals(desMsg));
		
	}
	
	
}
