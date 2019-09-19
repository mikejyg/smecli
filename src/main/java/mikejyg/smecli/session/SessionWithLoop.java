package mikejyg.smecli.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.CommandExecutorIntf;

/**
 * A session with basic and loop commands.
 * 
 * This session includes the basic session commands, provided by SessionCommands class, 
 *   and its own loop commands.
 * 
 * @author mikejyg
 *
 */
public class SessionWithLoop extends SessionBase {
	
	@SuppressWarnings("unused")
	private SessionCommands sessionCommands;
	
	// for the source command
//	private SourceCommandExecutor sourceCommandExecutor;
	
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
	
	public SessionWithLoop(CommandExecutorIntf commandExecutor) {
		super(commandExecutor);
		initCommands();
	}
	
	public SessionWithLoop(SessionCommon sessionCommonRef) {
		super(sessionCommonRef);
		initCommands();
	}
	
	/**
	 * copy settings from a parent session.
	 * @param parentSession
	 */
	public SessionWithLoop(SessionBase parentSession) {
		super(parentSession);
		initCommands();
	}
	
	private void initCommands() {
		// add basic commands
		sessionCommands = new SessionCommands(this);
		
		// add the source command
//		sourceCommandExecutor = new SourceCommandExecutor( ()->{
//			return new SessionWithLoop(this);
//		});
//		CliAnnotation.addMethods(this, sourceCommandExecutor);
		
		addCommands();
	}
	
	@Override
	public SessionWithLoop newSession() {
		return new SessionWithLoop(this);
	}

	/////////////////////////////////////////////
	
	protected void addCommands() {
		addCommand("repeat", null, "repeat the following commands, until done, for argument times"
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
		
		addCommand("done", null, "close of a loop."
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
				
				// TODO
//				getPrintWriter().print("fetchCmdLine() warning: missing matching done for loop.\n");
				
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
