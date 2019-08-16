package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * It is named CliAdapter for lack of a better name.
 * 
 * It provides some basic commands and functionalities to the CliBase class.
 * 
 * It uses annotation to add commands.
 * 
 * @author jgu
 *
 * uses the following to add commands to CLI.
 *		addMethods(cliAdapter);
 */
public class CliAdapter extends CliAnnotation {
	
	public CliAdapter() {
		addMethods(this);
	}
	
	/**
	 * transform to args[] from cmdCall, if needed.
	 * Here, args are separated by white spaces, and before the comment symbol #.
	 * 
	 * @param cmdCall
	 * @return
	 */
	public static String[] toArgs(CmdCallType cmdCall) {
		String args[] = cmdCall.getArgs();
		
		if (args==null) {
			args = CliUtils.toArgs(cmdCall.getArgumentsStr());
		}
		args=CliUtils.removeEndComments(args);

		return args;
	}
	
	/**
	 * Get the first argument. It uses the same mechanism as toArgs().
	 * 
	 * @param cmdCall
	 * @return null if no arg.
	 */
	public static String getArg0(CmdCallType cmdCall) {
		String args[]=toArgs(cmdCall);
		if (args==null || args.length==0)
			return null;
		else
			return args[0];
	}
	
	@CliCommand(shorthands = {"?"}, helpString = "print help.")
	public CmdReturnType help(CmdCallType cmdCall) {
		String helpStr="";
		for (CommandStruct cmd : getCommands()) {
			if (helpStr.isEmpty())
				helpStr = cmd.toString();
			else
				helpStr = helpStr + '\n' + cmd.toString();
		}
		return new CmdReturnType(ReturnCode.SUCCESS, helpStr);
	}
	
	@CliCommand(helpString = "echo arguments.")
	public CmdReturnType echo(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.SUCCESS, cmdCall.toArgumentsString());
	}
	
	@CliCommand(commandName="exit", helpString = "exit current session with an optional argument.")
	public CmdReturnType exitSession(CmdCallType cmdCall) {
		setExitFlag(true);
		return new CmdReturnType(ReturnCode.SUCCESS, getArg0(cmdCall));
	}

	@CliCommand(helpString = "exit current session and all parent sessions with an optional argument.")
	public CmdReturnType end(CmdCallType cmdCall) {
		setExitFlag(true);
		setEndFlag(true);
		return new CmdReturnType(ReturnCode.SUCCESS, getArg0(cmdCall));
	}

	@CliCommand(helpString = "sleep for specified time (seconds in double).")
	public CmdReturnType sleep(CmdCallType cmdCall) {
		String arg = getArg0(cmdCall);
		if (arg==null || arg.isEmpty()) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument.");
		}
		
		try {
			double t = Double.parseDouble(arg);
			Thread.sleep((long)(t * 1000));
			
		} catch (NumberFormatException e) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "NumberFormatException: " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			return new CmdReturnType(ReturnCode.FAILURE_RECOVERABLE, "InterruptedException: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			return new CmdReturnType(ReturnCode.FAILURE_RECOVERABLE, "IllegalArgumentException: " + e.getMessage());
		}
		
		return new CmdReturnType(ReturnCode.SUCCESS);
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
		String args[] = toArgs(cmdCall);
		
		if (args==null || args.length < 1) {
			CmdReturnType cmdReturn = new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument");
			return cmdReturn;
		} else if (args.length>1) {
			CmdReturnType cmdReturn = new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "excessive arguments after " + args[0]);
			return cmdReturn;
		}
		
		String filename = args[0];
		
//		getPrintStream().println("executing " + filename + "...");
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			return new CmdReturnType(ReturnCode.FAILURE_RECOVERABLE, "failed to open file: " + filename);
		}
		
		try ( BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ) ) {
			execAll(reader);
		}
		
//		getPrintStream().println(filename + " execution done.");
		return null;
		
	}
	
	@CliCommand(helpString = "With an argument, set local echo to on or off, or without argument, show current local echo state.")
	public CmdReturnType localEcho(CmdCallType cmdCall) {
		String arg = getArg0(cmdCall);
		if ( arg==null || arg.isEmpty())
			return new CmdReturnType(ReturnCode.SUCCESS, isLocalEcho() ? "on" : "off");
		
		if ( arg.contentEquals("on") ) {
			setLocalEcho(true);
			
		} else if ( arg.contentEquals("off") ) {
			setLocalEcho(false);
			
		} else {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT);
		}
		
		return new CmdReturnType(ReturnCode.SUCCESS, isLocalEcho() ? "on" : "off");
			
	}
	
	
}

