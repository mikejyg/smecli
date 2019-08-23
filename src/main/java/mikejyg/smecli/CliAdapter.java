package mikejyg.smecli;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CliCommands.CommandStruct;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * It is named CliAdapter for lack of a better name.
 * 
 * It provides some basic session specific commands and functionalities to the CliSession class.
 * 
 * It uses annotation to add commands.
 * 
 * @author jgu
 *
 */
public class CliAdapter extends CliSession {
	
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
	
	public CliAdapter(CliBase cliBase) {
		super(cliBase);
		initCliCommands();
	}
	
	public CliAdapter(CliSession parentSession) {
		super(parentSession);
		initCliCommands();
	}
	
	private void initCliCommands() {
		assert(getCliCommands().isEmpty());
		CliAnnotation.addMethods(getCliCommands(), this);
		addCommands();
	}
	
	private void addCommands() {
		getCliCommands().addCommand("continueOnError", new String[]{"coe"}, "set whether to continue on command execution error."
				+ " if no argument is given, prints out current state, otherwise use argument on or off."
				, (CmdCallType cmdCall)->{
					String arg = CliUtils.getArg0(cmdCall);
					if ( arg.isEmpty() )
						return new CmdReturnType(ReturnCode.OK, isContinueOnError() ? "on" : "off");

					if ( arg.contentEquals("on") ) {
						setContinueOnError(true);

					} else if ( arg.contentEquals("off") ) {
						setContinueOnError(false);

					} else {
						return new CmdReturnType(ReturnCode.INVALID_ARGUMENT);
					}

					return new CmdReturnType(ReturnCode.OK, isContinueOnError() ? "on" : "off");
				});
		
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
	
	@CliCommand(shorthands = {"?"}, helpString = "print help.")
	public CmdReturnType help(CmdCallType cmdCall) {
		String helpStr="";
		for (CommandStruct cmd : getCliCommands().getCommands()) {
			if (helpStr.isEmpty())
				helpStr = cmd.toString();
			else
				helpStr = helpStr + '\n' + cmd.toString();
		}

		if ( ! helpStr.isEmpty() )
			helpStr = helpStr + '\n';
		
		helpStr = helpStr + "from command executor: \n" + getCommandExecutorRef().toHelpString();

		return new CmdReturnType(ReturnCode.OK, helpStr);
	}
	
	/**
	 * source a sub-script file from the classpath.
	 * @param cmdCall
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws IllegalInputCharException
	 * @throws UnexpectedEofException
	 * @throws ExitAllSessions
	 */
	@CliCommand(shorthands= {"."}, helpString = "parameter: script_filename\texecute the script file in a new session.")
	public CmdReturnType source(CmdCallType cmdCall) throws FileNotFoundException, IOException, IllegalInputCharException, UnexpectedEofException {
		String args[] = CliUtils.toArgs(cmdCall);
		
		if (args.length < 1) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument");
		} else if (args.length>1) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "excessive arguments after " + args[0]);
		}
		
		String filename = args[0];
		
//		getPrintStream().println("executing " + filename + "...");
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			return new CmdReturnType(ReturnCode.FAILURE, "failed to open file: " + filename);
		}
		
		CmdReturnType cmdReturn;
		try ( InputStreamReader reader = new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) )  {
			CliAdapter newSession = new CliAdapter(this);
			
			// change settings for a sub-session
			newSession.setLocalEcho(true);
			newSession.setContinueOnError(false);
			
			newSession.setReader(reader);
			cmdReturn = newSession.execAll();
		}
		
//		getPrintStream().println(filename + " execution done.");
		
		if (cmdReturn.getReturnCode()==ReturnCode.SCRIPT_ERROR_EXIT)
			return cmdReturn;
		else
			return new CmdReturnType(ReturnCode.NOP);
		
	}
	
	@CliCommand(helpString = "With an argument, set local echo to on or off, or without argument, show current local echo state.")
	public CmdReturnType localEcho(CmdCallType cmdCall) {
		String arg = CliUtils.getArg0(cmdCall);
		if ( arg.isEmpty())
			return new CmdReturnType(ReturnCode.OK, isLocalEcho() ? "on" : "off");
		
		if ( arg.contentEquals("on") ) {
			setLocalEcho(true);
			
		} else if ( arg.contentEquals("off") ) {
			setLocalEcho(false);
			
		} else {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT);
		}
		
		return new CmdReturnType(ReturnCode.OK, isLocalEcho() ? "on" : "off");
			
	}
	
	@CliCommand(commandName="exit", helpString = "exit current session with an optional argument.")
	public CmdReturnType exitSession(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.EXIT, CliUtils.getArg0(cmdCall));
	}

	@CliCommand(helpString = "exit current session and all parent sessions with an optional argument.")
	public CmdReturnType end(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.END, CliUtils.getArg0(cmdCall));
	}


}

