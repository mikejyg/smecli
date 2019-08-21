package mikejyg.smecli;

import org.junit.Test;

/**
 * unit tests for CmdCallType
 */
public class CmdCallTypeTest {

	private void serdesTest(CmdCallType cmdCallType) {
		byte [] cmdCallBytes = cmdCallType.toBytes();
		
		CmdCallType c2 = new CmdCallType(cmdCallBytes);
		
		if ( ! cmdCallType.equals(c2) ) {
			System.out.println(c2.toString());
			System.out.println("vs expected: " + cmdCallType.toString());
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
