package mikejyg.smecli;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
	
	public CliAdapter(CliBase cliBase) {
		super(cliBase);
		initCliCommands();
	}
	
	/**
	 * copy settings from a parent session.
	 * @param parentSession
	 */
	public CliAdapter(CliSession parentSession) {
		super(parentSession);
		initCliCommands();
	}
	
	private void initCliCommands() {
		assert(getCliCommands().isEmpty());
		CliAnnotation.addMethods(getCliCommands(), this);
		addCommands();
	}
	
	@Override
	public CliAdapter newSession() {
		return new CliAdapter(this);
	}
	
	protected void addCommands() {
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
			CliAdapter newSession = newSession();
			
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

