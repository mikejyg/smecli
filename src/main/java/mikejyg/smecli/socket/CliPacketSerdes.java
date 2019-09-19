package mikejyg.smecli.socket;

import java.nio.ByteBuffer;
import java.util.Arrays;

import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.socket.ByteBufferAccumulator;

/**
 * serializer/de-serializer for CLI packets.
 * 
 * a CLI packet is:
 * 4 bytes ID
 * ... CmdCallType or CmdReturnType, or ...
 * 
 * @author mikejyg
 *
 */
public class CliPacketSerdes {
	public static class DesException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public enum Id {
		OOB(0)	// out of band
		, CMD_CALL(1)
		,CMD_RETURN(2)
		, INVALID (-1)		// not a packet ID, but for flagging.
		;
		
		private int id;
		
		private Id(int id) {
			this.id=id;
		}
		
		public int intValue() {
			return id;
		}
	}
	
	////////////////////////////////////////////////////
	
	private Id lastId = Id.INVALID;
	
	////////////////////////////////////////////////////
	
	public static void serialize(ByteBufferAccumulator bba, CmdCallType cmdCall) {
		bba.putInt(Id.CMD_CALL.intValue());
		cmdCall.serialize(bba);
	}
	
	public static void serialize(ByteBufferAccumulator bba, CmdReturnType cmdReturn) {
		bba.putInt(Id.CMD_RETURN.intValue());
		cmdReturn.serialize(bba);
	}
	
	public static void serialize(ByteBufferAccumulator bba, byte[] oobData) {
		bba.putInt(Id.OOB.intValue());
		bba.put(oobData);
	}
	
	public Object deserialize(ByteBuffer bb) throws DesException, ReturnCode.IllegalValueException {
		int id = bb.getInt();
		
		if (id==Id.CMD_CALL.intValue()) {
			lastId = Id.CMD_CALL;
			return new CmdCallType(bb);
			
		} else if (id==Id.CMD_RETURN.intValue()) {
			lastId = Id.CMD_RETURN;
			return new CmdReturnType(bb);
			
		} else if (id==Id.OOB.intValue()) {
			lastId = Id.OOB;
			return Arrays.copyOfRange(bb.array(), bb.position(), bb.capacity());
					
		} else {
			lastId = Id.INVALID;
			throw new DesException();
		}
		
	}
	
	public Id getLastId() {
		return lastId;
	}

	
}
