package mikejyg.smecli.commands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.session.SessionIntf;

/**
 * The source command.
 * 
 * @author mikejyg
 *
 */
public class SourceCommand {
	
	// the function to get a new session.
	private Supplier<SessionIntf> newSessionFunc;
	
	//////////////////////////////////////////
	
	public SourceCommand(Supplier<SessionIntf> newSessionFunc) {
		this.newSessionFunc = newSessionFunc;
	}
	
	/**
	 * source a sub-script file from the classpath.
	 * @param cmdCall
	 * @return
	 * @throws IOException
	 */
	@CliCommand(shorthands= {"."}, helpString = "parameter: script_filename\texecute the script file in a new session.")
	public CmdReturnType source(CmdCallType cmdCall) throws IOException {
		String args[] = CliUtils.toArgs(cmdCall);
		
		if (args.length < 1) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "missing argument");
		} else if (args.length>1) {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT, "excessive arguments after " + args[0]);
		}
		
		String filename = args[0];
		
//		getPrintWriter().println("executing " + filename + "...");
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			return new CmdReturnType(ReturnCode.FAILURE, "failed to open file: " + filename);
		}
		
		CmdReturnType cmdReturn;
		try ( InputStreamReader reader = new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) )  {
			SessionIntf newSession  = newSessionFunc.get();
			newSession.setReader(reader);
			cmdReturn = newSession.execAll();
		}
		
//		getPrintWriter().println(filename + " execution done.");
		
		if (cmdReturn.getReturnCode()==ReturnCode.SCRIPT_ERROR_EXIT)
			return cmdReturn;
		else
			return new CmdReturnType(ReturnCode.NOP);
		
	}


}
