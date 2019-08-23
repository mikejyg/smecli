package mikejyg.smecli;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * a CommandExecutorBase with some built-in commands
 * @author jgu
 *
 */
public class CommandExecutor extends CommandExecutorBase {

	private CliBase cliBase;
	
	/**
	 * hold an inactive session object here, 
	 *   so that the settings of a new session can be customized.
	 */
	private CliSession cliSessionSettings;
	
	//////////////////////////////////////////////////////
	
	public CommandExecutor() {
		addMethods(this);
		addCommands();
		
		cliBase = new CliBase(this);
		cliSessionSettings = new CliSession(cliBase);
		
		// setting default values for a sub-session
		cliSessionSettings.setLocalEcho(true);
		cliSessionSettings.setContinueOnError(false);
	}
	
	private void addCommands() {
		addCommand("assert", null, "assert the value of the last result."
			+ " 1st argument the return code string, 2nd argument(optional) is is the result string."
			, (CmdCallType cmdCall)->{
				String [] args = CliUtils.toArgs(cmdCall);
				if (args.length<1)
					return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument.");
				
				if ( ! args[0].equals(getLastCmdReturn().getReturnCode().name()) ) {
					return new CmdReturnType(ReturnCode.FAILURE, "return code mismatch: " + getLastCmdReturn().getReturnCode().name()
							+ " vs " + args[0]);
				}
				
				if ( args.length >=2) {
					if ( ! args[1].equals(getLastCmdReturn().getResult()) ) {
						return new CmdReturnType(ReturnCode.FAILURE, "result mismatch: " + getLastCmdReturn().getResult()
								+ " vs " + args[1]);
					}
				}
		
				return new CmdReturnType(ReturnCode.OK);
			});
	}
	
	@CliCommand(helpString = "sleep for specified time (seconds in double).")
	public CmdReturnType sleep(CmdCallType cmdCall) {
		String arg = CliUtils.getArg0(cmdCall);
		if (arg.isEmpty()) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument.");
		}
		
		try {
			double t = Double.parseDouble(arg);
			Thread.sleep((long)(t * 1000));
			
		} catch (NumberFormatException e) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "NumberFormatException: " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			return new CmdReturnType(ReturnCode.FAILURE, "InterruptedException: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			return new CmdReturnType(ReturnCode.FAILURE, "IllegalArgumentException: " + e.getMessage());
		}
		
		return new CmdReturnType(ReturnCode.OK);
	}

	@CliCommand(helpString = "echo arguments.")
	public CmdReturnType echo(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.OK, cmdCall.toArgumentsString());
	}
	
	@CliCommand(shorthands = {"?"}, helpString = "print help.")
	public CmdReturnType help(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.OK, toHelpString());
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
			cmdReturn = newCliAdapter(reader).execAll();
		}
		
//		getPrintStream().println(filename + " execution done.");
		
		if (cmdReturn.getReturnCode()==ReturnCode.SCRIPT_ERROR_EXIT)
			return cmdReturn;
		else
			return new CmdReturnType(ReturnCode.NOP);
		
	}

	/**
	 * create a new CLI session.
	 * @param reader
	 * @return
	 */
	public CliAdapter newCliAdapter() {
		CliAdapter cliAdapter = new CliAdapter(cliSessionSettings);
		return cliAdapter;
	}
	
	/**
	 * create a new CLI session with a reader.
	 * @param reader
	 * @return
	 */
	public CliAdapter newCliAdapter(Reader reader) {
		CliAdapter cliAdapter = newCliAdapter();
		cliAdapter.setReader(reader);
		return cliAdapter;
	}
	
	
}

