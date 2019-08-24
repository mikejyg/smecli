package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Test;

import mikejyg.cloep.ArgsParser;
import mikejyg.cloep.ArgsParser.ParseException;
import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;

public class CliTest {

	// run options
	
	// run an interactive session
	private boolean interactiveFlag;
	
	// execute the args as a command
	private String [] commandStrs;
	
	private String transcriptFilename;
	
	//////////////////////////////////////////////////////////////////////////
	
	static public String printToString(Consumer<PrintStream> printer) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);

		printer.accept(ps);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);			
	}


	//////////////////////////////////////////////////////////////////////////

	@Test
	public void test() throws IOException, IllegalInputCharException, UnexpectedEofException {
		CommandExecutorIntf commandExecutor = new CommandExecutor();
		final String outputFilename="cliTest.out";
		
		// execute commands from cliTestCommands.txt and write to test.out
		
		try ( InputStreamReader reader = new InputStreamReader( 
				this.getClass().getResourceAsStream("/cliTestCommands.txt"), StandardCharsets.UTF_8 ) ) {
			try ( PrintStream printStream = new PrintStream( new FileOutputStream(outputFilename) ) ) {
				CliBase cliBase = new CliBase(commandExecutor);
				cliBase.setPrintStream(printStream);

				CliLoop cli = new CliLoop(cliBase);
				cli.setReader(reader);
				cli.setLocalEcho(true);
				
				cli.setContinueOnError(true);
				assert( cli.execAll().getReturnCode() == ReturnCode.NOP );
			}
		}
		
		// compare output against golden results
		
		Set<String> goldenSet;
		try ( BufferedReader goldenReader = new BufferedReader( 
				new InputStreamReader( this.getClass().getResourceAsStream("/cliTestGolden.out"), StandardCharsets.UTF_8 ) ) ) {
			goldenSet = goldenReader.lines().collect(Collectors.toSet());
		}
		
		Set<String> outputSet;
		try ( BufferedReader outputReader = new BufferedReader( 
				new InputStreamReader( new FileInputStream(outputFilename), StandardCharsets.UTF_8 ) ) ) {
			outputSet = outputReader.lines().collect(Collectors.toSet());
		}
		
		assert( goldenSet.equals(outputSet) );
		
	}

	public void parseArgs(String[] args) throws ParseException {
		ArgsParser argsParser = new ArgsParser();
		argsParser.addOptionWithoutArg('h', null, "help", arg->{argsParser.printHelp();});
		
		argsParser.addOptionWithoutArg('c', null, "execute the remaining args as a command", arg->{
			commandStrs = argsParser.getRemainingArgs();
			argsParser.terminate();
		});
		
		argsParser.addOptionWithoutArg('i', null, "run an interactive session", arg->{ interactiveFlag=true; });
		
		argsParser.addOptionWithArg('t', null, "write out a transcript", "filename to write to", arg->{ transcriptFilename=arg; });
		
		argsParser.parse(args);
	}
	
	public void execute() throws IOException, UnexpectedEofException, IllegalInputCharException, InvokeCommandFailed {
		CommandExecutor commandExecutor = new CommandExecutor();

		Writer writer=null;
		if ( transcriptFilename != null ) {
			writer = new FileWriter(transcriptFilename);
			new CliSessionTranscriptor(writer, commandExecutor.getCliBase());
		}
		
		// customize commandExector, so that it uses CliLoop for new sessions.
		commandExecutor.setNewClisessionFunc( (sessionSettings, reader)->{
			CliLoop cliAdapter = new CliLoop(sessionSettings);
			cliAdapter.setReader(reader);
			return cliAdapter;
		});
		
		if (commandStrs!=null) {
			System.out.println( commandExecutor.execCmd(commandStrs) );
			
		} else if (interactiveFlag) {

			try (Reader reader = new InputStreamReader(System.in)) {
				CliSession cli = commandExecutor.newCliSession(reader);
				
				cli.setLocalEcho(false);
				cli.setContinueOnError(true);
				cli.setInteractiveFlag(true);
				cli.execAll();
			}
			
		} else {
			System.out.println("no action selected or performed.");
		}

		if (writer!=null)
			writer.close();	
		
	}
	
	//////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) throws IOException, IllegalInputCharException, UnexpectedEofException, InvokeCommandFailed, ParseException {
		CliTest cliTest = new CliTest();
		cliTest.parseArgs(args);
		cliTest.execute();
		
	}
	
}
