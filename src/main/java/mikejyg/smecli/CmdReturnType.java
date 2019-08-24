package mikejyg.smecli;

import java.nio.ByteBuffer;

import mikejyg.socket.ByteBufferAccumulator;

/**
 * this class is to wrap the 2 return values.
 * @author jgu
 *
 */
public class CmdReturnType {
	
	/**
	 * the enumeration of return code is used by the calling party to
	 *   decide the next action.
	 * @author jgu
	 *
	 */
	static public enum ReturnCode {
		NOP(0),	// no command, comment only, a successful flow control execution...
		OK(1),
		EXIT(2),
		END(3),
		INVALID_COMMAND(4),
		INVALID_ARGUMENT(5),
		FAILURE(6),	// can continue
		FAILURE_UNRECOVERABLE(7)	// cannot continue
		, SCRIPT_ERROR_EXIT(8);			// used by sub-script to indicate script exit due to error 
		
		private int idValue;
		
		private ReturnCode(int idValue) {
			this.idValue = idValue;
		}
		
		public boolean isOk() {
			return ( this==NOP || this==OK || this==EXIT || this==END );
		}
		
		/**
		 * Whether the return is a result of a command execution.
		 * NOP, or other flow control returns are not considered results of command executions.  
		 * @return
		 */
		public boolean isCmdExecResult() {
			return this!=NOP && this!=SCRIPT_ERROR_EXIT; 
		}
		
		public int intValue() {
			return idValue;
		}

		public static class IllegalValueException extends Exception {
			private static final long serialVersionUID = 1L;
		}
		
		public static ReturnCode getReturnCode(int intValue) throws IllegalValueException {
			for ( ReturnCode rc : values() ) {
				if (rc.intValue() == intValue)
					return rc;
			}
			throw new IllegalValueException();
		}
		
	};

	private ReturnCode returnCode; 
	
	/**
	 * the result of a command, if any.
	 */
	private String result;	// device specific results/messages
	
	////////////////////////////////////////////////////////////
	
	public CmdReturnType(ReturnCode returnCode) {
		this.returnCode = returnCode;
		result="";
	}
	
	/**
	 * @param returnCode NOTE: not null.
	 * @param result
	 */
	public CmdReturnType(ReturnCode returnCode, String result) {
		this.returnCode = returnCode;
		this.result = result;
	}

	/**
	 * 4 bytes return code
	 * 4 bytes result length
	 * ... result
	 * 
	 * @param bba
	 */
	public void serialize(ByteBufferAccumulator bba) {
		byte [] resultBytes = result.getBytes(CmdCallType.charset);
		
		ByteBuffer bb = ByteBuffer.allocate( 4 + 4 + resultBytes.length );
		bb.putInt(returnCode.intValue());
		bb.putInt(resultBytes.length);
		bb.put(resultBytes);
		
		bba.put(bb.array());
	}
	
	/**
	 * de-serialize
	 * @param bytes
	 * @throws ReturnCode.IllegalValueException
	 */
	public CmdReturnType(ByteBuffer bb) throws ReturnCode.IllegalValueException {
		returnCode = ReturnCode.getReturnCode(bb.getInt());
		int length = bb.getInt();
		byte [] buf =new byte[length];
		bb.get(buf);
		result = new String(buf, CmdCallType.charset);
	}
	
	@Override
	public String toString() {
		String str = returnCode.name();
		if ( !result.isEmpty() ) {
			str = str + ' ' + result; 
		}
		return str;
	}
	
	public ReturnCode getReturnCode() {
		return returnCode;
	}

	public String getResult() {
		return result;
	}

}
