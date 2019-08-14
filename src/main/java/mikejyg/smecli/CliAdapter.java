package mikejyg.smecli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
	
	public CliAdapter() {}
	
	@CliCommand(shorthands = {"?"}, helpString = "print help.")
	public CmdReturnType help(CmdCallType cmdCall) {
		for (CommandStruct cmd : getCommands()) {
			getPrintWriter().println(cmd.toString());
		}
		return new CmdReturnType(ReturnCode.SUCCESS);
	}
	
	@CliCommand(helpString = "echo arguments.")
	public CmdReturnType echo(CmdCallType cmdCall) {
		getPrintWriter().println(cmdCall.argumentsStr);
		return new CmdReturnType(ReturnCode.SUCCESS);
	}
	
	@CliCommand(commandName="exit", helpString = "exit current session.")
	public CmdReturnType exitSession(CmdCallType cmdCall) {
		getPrintWriter().println("exit()...");
		
		setExitFlag(true);
		
		return new CmdReturnType(ReturnCode.SUCCESS);
	}

	@CliCommand(helpString = "exit current session and all parent sessions.")
	public CmdReturnType end(CmdCallType cmdCall) {
		getPrintWriter().println("exit()...");
		
		setExitFlag(true);
		setEndFlag(true);
		
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
		
		getPrintWriter().println("executing " + filename + "...");
		
		CmdReturnType cmdReturn;
		try ( Reader reader = new InputStreamReader( this.getClass().getResourceAsStream(filename), StandardCharsets.UTF_8 ) ) {
			cmdReturn = execAll(reader, getPrintWriter());
		}
		
		getPrintWriter().println(filename + " execution done.");
		return cmdReturn;
		
	}
	
	
}

