package mikejyg.smecli;

import java.nio.ByteBuffer;

import org.junit.Test;

import mikejyg.socket.ByteBufferAccumulator;

/**
 * unit tests for CmdCallType
 */
public class CmdCallTypeTest {

	private void serdesTest(CmdCallType cmdCallType) {
		ByteBufferAccumulator bba = new ByteBufferAccumulator();
		cmdCallType.serialize(bba);
		byte [] cmdCallBytes = bba.toBytes();
		
		CmdCallType c2 = new CmdCallType(ByteBuffer.wrap(cmdCallBytes));
		
		if ( ! cmdCallType.equals(c2) ) {
			System.out.print(c2.toString()+'\n');
			System.out.print("vs expected: " + cmdCallType.toString()+'\n');
			assert(false);
		}
	}
		
	@Test
	public void test() {
		serdesTest(new CmdCallType("abc","defg"));
		
		serdesTest(new CmdCallType("abc",""));
		
		serdesTest(new CmdCallType( "abc", new String[]{"defg", "hijk"} ) );
		
		serdesTest(new CmdCallType( "abc", new String[0] ) );
		
	}
	
}
