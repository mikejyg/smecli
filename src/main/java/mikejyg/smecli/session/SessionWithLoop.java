package mikejyg.smecli.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.CommandStruct;
import mikejyg.smecli.cmdexecutor.CommandExecutorIntf;
import mikejyg.smecli.cmdexecutor.CommandsCommandExecutor;

/**
 * A session with basic and loop commands.
 * 
 * This session includes the basic session commands, provided by SessionCommands class, 
 *   and its own loop commands.
 * 
 * @author mikejyg
 *
 */
public class SessionWithLoop extends Session {

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
	
	// session instance specific commands
	private CommandsCommandExecutor sessionCommandExecutor;
	
	/////////////////////////////////////////////
	
	public SessionWithLoop(CommandExecutorIntf commandExecutor) {
		super(commandExecutor);
		
		// really just to override the help command
		commandExecutor.addMethods(this);

		// instance specific commands
		sessionCommandExecutor = new CommandsCommandExecutor(commandExecutor.getEnvironment());
		sessionCommandExecutor.addCommands(getCommandStructs());
	}
	
	/**
	 * copy settings from a parent session.
	 * @param parentSession
	 */
	public SessionWithLoop(Session parentSession) {
		super(parentSession);
		
		// instance specific commands
		sessionCommandExecutor = new CommandsCommandExecutor(parentSession.getCommandExecutorRef().getEnvironment());
		sessionCommandExecutor.addCommands(getCommandStructs());
	}
	
	@Override
	public SessionWithLoop newSubSession() {
		return new SessionWithLoop(this);
	}
	
	@Override
	public CmdReturnType execCmd(CmdCallType cmdCall) throws Exception {
		// do session command first...
		if ( sessionCommandExecutor.hasCommand(cmdCall) ) {
			CmdReturnType cmdReturn = sessionCommandExecutor.execCmd(cmdCall);

			if ( cmdReturn.getReturnCode().isCmdExecResult() )
				setLastCmdReturn(cmdReturn);
				
			return cmdReturn;
			
		} else {
			return super.execCmd(cmdCall);
		}
	}

	/////////////////////////////////////////////
	
	public List<CommandStruct> getCommandStructs() {
		ArrayList<CommandStruct> cmds = new ArrayList<>();
		
		cmds.add( new CommandStruct("repeat", null, "repeat the following commands, until done, for argument times"
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
		}));
		
		cmds.add( new CommandStruct("done", null, "close of a loop."
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
		}));

		return cmds;
	}

	/**
	 * override the help command
	 * @param cmdCall
	 * @return
	 */
	@CliCommand(shorthands = {"?"}, helpString = "print help.")
	public CmdReturnType help(CmdCallType cmdCall) {
		String helpStr = "SessionWithLoop instance specific commands:\n" + sessionCommandExecutor.toHelpString();
		
		helpStr += "\n\ncommand executor commands:\n" + getCommandExecutorRef().toHelpString();
		
		return new CmdReturnType(ReturnCode.OK, helpStr);
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
