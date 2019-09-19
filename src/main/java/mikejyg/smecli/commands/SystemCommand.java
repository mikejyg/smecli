package mikejyg.smecli.commands;

import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CmdReturnType.ReturnCode;

public class SystemCommand {

	private PrintWriter printWriterRef;
	
	public SystemCommand(PrintWriter printWriterRef) {
		this.printWriterRef = printWriterRef;
	}
	
	@CliCommand(shorthands = {"sys"}, helpString = "execute a system command with args")
	public CmdReturnType system(CmdCallType cmdCall) throws Exception {
		String [] args = CliUtils.toArgs(cmdCall);
		
		ArrayList<String> argList = new ArrayList<>();
		
		for (int i=0; i<args.length; i++) {
			argList.add(args[i]);
		}

		ProcessBuilder processBuilder = new ProcessBuilder(argList);
		processBuilder.redirectErrorStream(true);
		
		Process process = processBuilder.start();

		InputStream is = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line = null;
		while ((line = reader.readLine()) != null) {
			printWriterRef.println(line);
		}
		
		int rc = process.waitFor();
		
		return new CmdReturnType(ReturnCode.OK, Integer.toString(rc));
		
	}
	
	
}
