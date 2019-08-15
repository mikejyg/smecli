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
	
	@CliCommand(commandName="exit", helpString = "exit current session.")
	public CmdReturnType exitSession(CmdCallType cmdCall) {
		setExitFlag(true);
		return new CmdReturnType(ReturnCode.SUCCESS);
	}

	@CliCommand(helpString = "exit current session and all parent sessions.")
	public CmdReturnType end(CmdCallType cmdCall) {
		setExitFlag(true);
		setEndFlag(true);
		return new CmdReturnType(ReturnCode.SUCCESS);
	}

	@CliCommand(helpString = "sleep for specified time (seconds in double).")
	public CmdReturnType sleep(CmdCallType cmdCall) {
		if (cmdCall.argumentsStr.isEmpty()) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument.");
		}
		
		try {
			double t = Double.parseDouble(cmdCall.argumentsStr);
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
	public CmdReturnType source(CmdCallType cmdCall) throws FileNotFoundException, IOException, IllegalInputCharException, UnexpectedEofException, ExitAllSessions {
		String args[] = CliUtils.toArgs(cmdCall.argumentsStr);
		
		if (args.length < 1) {
			CmdReturnType cmdReturn = new CmdReturnType(ReturnCode.INVALID_ARGUMENT);
			cmdReturn.result = "missing argument";
			return cmdReturn;
		} else if (args.length>1) {
			CmdReturnType cmdReturn = new CmdReturnType(ReturnCode.INVALID_ARGUMENT);
			cmdReturn.result = "excessive arguments after " + args[0];
			return cmdReturn;
		}
		
		String filename = args[0];
		
		getPrintStream().println("executing " + filename + "...");
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			return new CmdReturnType(ReturnCode.FAILURE_RECOVERABLE, "failed to open file: " + filename);
		}
		
		try ( BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ) ) {
			execAll(reader);
		}
		
		getPrintStream().println(filename + " execution done.");
		return getLastCmdReturn();
		
	}
	
	
}

