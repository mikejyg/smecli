package mikejyg.smecli;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * to package a command name and a arguments string
 * 
 * Since a command can originate from 2 sources: an input stream, or as command line arguments (args[]),
 *   the argument can be either a single String, or String[].
 *   
 * This structure accommodates both.
 *  
 * @author jgu
 *
 */
public class CmdCallType {
	public static final Charset charset=StandardCharsets.UTF_8;
	
	private String commandName = new String();
	
	private boolean argumentsInArgsFlag=false;
	
	/**
	 * contains the already parsed command-line args[].
	 */
	private String[] args = new String[0];
	
	/**
	 * This is a string of the entire arguments, except,
	 *   when the arguments are in args[], it points to args[0] 
	 */
	private String argumentsStr=new String();

	////////////////////////////////////////////////////////////
	
	/**
	 * construct an empty cmdCall.
	 */
	public CmdCallType() {}
	
	/**
	 * constructor with a single parsed argument string.
	 * @param commandName	not null
	 * @param argumentsStr	not null
	 */
	public CmdCallType(String commandName, String argumentsStr) {
		this.commandName = commandName;
		this.argumentsStr = argumentsStr;
	}
	
	public CmdCallType(String commandName) {
		this(commandName, "");
	}
	
	/**
	 * constructor with already parsed command-line args[].
	 * @param commandName
	 * @param args
	 */
	public CmdCallType(String commandName, String args[]) {
		this.commandName = commandName;
		
		if (args.length!=0) {
			argumentsInArgsFlag=true;
			this.args=args;
			argumentsStr = args[0];			
		}
	}
	
	/**
	 * serialize.
	 * @return
	 */
	public byte[] toBytes() {
		ByteBuffer bb;
		
		byte [] cmdBytes = commandName.getBytes(charset);
		
		int totalSize = 4 + cmdBytes.length;	// length, string
		totalSize++;							// byte, argumentsInArgsFlag
		
		if (argumentsInArgsFlag) {
			
			int totalArgsBytesSize=0;
			
			ArrayList<byte[]> argsByteList = new ArrayList<>();
			
			for (String arg : args) {
				byte[] argsBytes = arg.getBytes(charset);
				argsByteList.add(argsBytes);
				totalArgsBytesSize += argsBytes.length;
			}
			
			totalSize += 4;							// args.length
			totalSize += args.length * 4;			// length for every arg
			totalSize += totalArgsBytesSize;		// args
			
			bb = ByteBuffer.allocate(totalSize);
			
			bb.putInt(cmdBytes.length);
			bb.put(cmdBytes);
			bb.put( (byte)(argumentsInArgsFlag ? 1 : 0) );
			
			bb.putInt(args.length);
			
			for (byte[] argBytes : argsByteList) {
				bb.putInt(argBytes.length);
				bb.put(argBytes);
			}
			
		} else {
			byte[] argumentsStrBytes = argumentsStr.getBytes(charset);
			totalSize += 4 + argumentsStrBytes.length;
			
			bb = ByteBuffer.allocate(totalSize);
			
			bb.putInt(cmdBytes.length);
			bb.put(cmdBytes);
			bb.put( (byte)(argumentsInArgsFlag ? 1 : 0) );
		
			bb.putInt(argumentsStrBytes.length);
			bb.put(argumentsStrBytes);
			
		}
		
		return bb.array();		
	}
	
	/**
	 * de-serialize.
	 * @param bytes
	 */
	public CmdCallType(byte[] bytes) {
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		byte[] buf = new byte[bb.getInt()];
		bb.get(buf);
		commandName = new String(buf, charset);
		byte b = bb.get();
		if ( b == (byte)1 )
			argumentsInArgsFlag = true;
		
		if (argumentsInArgsFlag) {
			int argsCnt = bb.getInt();
			
			args = new String[argsCnt];
			for (int i=0; i<argsCnt; i++) {
				int length = bb.getInt();
				buf = new byte[length];
				bb.get(buf);
				args[i] = new String(buf, charset);
			}
			
		} else {
			int length = bb.getInt();
			buf = new byte[length];
			bb.get(buf);
			argumentsStr = new String(buf, charset);
		}

	}
	
	@Override
	public boolean equals(Object obj) {
		CmdCallType c2 = (CmdCallType) obj;
		if ( ! commandName.contentEquals(c2.commandName ) )
				return false;
		
		if ( argumentsInArgsFlag != c2.argumentsInArgsFlag )
			return false;
		
		if (argumentsInArgsFlag) {
			if ( args.length != c2.args.length )
				return false;

			for ( int i=0; i<args.length; i++) {
				if ( ! args[i].contentEquals(c2.args[i]))
					return false;
			}
			
			return true;
			
		} else {
			return argumentsStr.contentEquals(c2.argumentsStr);
		}
		
	}
	
	public boolean isEmpty() {
		return commandName.isEmpty();
	}
	
	/**
	 * @return a string contains the entire arguments.
	 */
	public String toArgumentsString() {
		if ( ! argumentsInArgsFlag )
			return argumentsStr;
		
		String str="";
		for (String s : args) {
			if (str.isEmpty())
				str = s;
			else
				str = str + " " + s;
		}
		return str;
	}
	
	@Override
	public String toString() {
		String str = toArgumentsString();
		if ( ! str.isEmpty() )
			return commandName + " " + str;
		else
			return commandName;
	}
	
	/**
	 * parse a string command line to the CmdCallType structure.
	 * 
	 * @param cmdString
	 * @return
	 */
	public static CmdCallType toCmdCall(String cmdLine) {
		if (cmdLine.isEmpty())
			return new CmdCallType();
		
		CmdCallType cmdCall;
		
		int k = cmdLine.indexOf(' ');
		if (k==-1) {
			cmdCall = new CmdCallType(cmdLine, "");
		} else {
			cmdCall = new CmdCallType( cmdLine.substring(0, k).trim(), cmdLine.substring(k+1).trim() );
		}
		
		return cmdCall;

	}
	
	public static CmdCallType toCmdCall(String args[]) {
		if (args.length==0)
			return new CmdCallType();
		
		return new CmdCallType(args[0], Arrays.copyOfRange(args, 1, args.length));
	}

	public String getCommandName() {
		return commandName;
	}

	public String[] getArgs() {
		return args;
	}

	public String getArgumentsStr() {
		return argumentsStr;
	}
	
	public boolean isArgumentsInArgsFlag() {
		return argumentsInArgsFlag;
	}


}
