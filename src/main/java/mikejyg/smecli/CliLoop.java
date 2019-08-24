package mikejyg.smecli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;

public class CliLoop extends CliAdapter {
	// for loops
	
	private List<String> cmdLineBuffer = new ArrayList<>();
	private int clbCounter = 0;
	
	private class LoopStruct {
		public int iterations;		// remaining iterations to do
		
		// indexes into cmdLineBuffer
		public int beginIdx;
		
		/**
		 * called when a loop start instruction is encountered.
		 * @param repeats
		 */
		public LoopStruct(int repeats) {
			iterations = repeats;
			beginIdx = clbCounter;	// points to the one AFTER the loop start instruction.
		}
		
	}
	
	private Stack<LoopStruct> loopStack = new Stack<>();
	private LoopStruct currentLoopStruct = null; 
	
	/////////////////////////////////////////////
	
	public CliLoop(CliBase cliBase) {
		super(cliBase);
	}
	
	/**
	 * copy settings from a parent session.
	 * @param parentSession
	 */
	public CliLoop(CliSession parentSession) {
		super(parentSession);
	}
	
	@Override
	public CliLoop newSession() {
		return new CliLoop(this);
	}

	/////////////////////////////////////////////
	
	@Override
	protected void addCommands() {
		super.addCommands();
		
		getCliCommands().addCommand("repeat", null, "repeat the following commands, until done, for argument times"
				, (CmdCallType cmdCall)->{
			String arg = CliUtils.getArg0(cmdCall);
			if ( arg.isEmpty() )
				return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument.");
			int iterations = Integer.parseInt(arg);
			
			if (iterations<1)
				return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "iterations less than 1.");
			
			loopStack.add(new LoopStruct(iterations));
			currentLoopStruct = loopStack.lastElement();
			return new CmdReturnType(ReturnCode.NOP);
		});
		
		getCliCommands().addCommand("done", null, "close of a loop."
				, (CmdCallType cmdCall)->{
			if (currentLoopStruct==null)
				return new CmdReturnType(ReturnCode.INVALID_COMMAND, "no matching loop start.");

			currentLoopStruct.iterations--;
			if ( currentLoopStruct.iterations==0 
					|| clbCounter == currentLoopStruct.beginIdx		// empty loop 
			) {
				loopStack.pop();
				if (loopStack.isEmpty()) {
					currentLoopStruct=null;
					cmdLineBuffer.clear();
					clbCounter=0;
				} else
					currentLoopStruct = loopStack.lastElement();
				
			} else {
				clbCounter = currentLoopStruct.beginIdx;
			}
			return new CmdReturnType(ReturnCode.NOP);
		});

	}
	
	@Override
	protected String fetchCmdLine() throws IOException, IllegalInputCharException, UnexpectedEofException {
		String cmdLine  = null;
		
		if ( clbCounter < cmdLineBuffer.size() ) {	// cmd line is in the buffer
			return cmdLineBuffer.get(clbCounter++);
		}
		
		cmdLine = super.fetchCmdLine();
		
		if ( ! loopStack.isEmpty() ) {	// accumulating
			if (cmdLine!=null) {
				cmdLineBuffer.add(cmdLine);
				clbCounter++;
				
			} else {	// EOF while looping
				getPrintStream().println("fetchCmdLine() warning: missing matching done for loop.");
				
//				// cancel all loops
//				
//				loopStack.clear();
//				currentLoopStruct = null;
//				cmdLineBuffer.clear();
//				clbCounter=0;
//				return null;
			}
		}
		return cmdLine;
	}
	

}
