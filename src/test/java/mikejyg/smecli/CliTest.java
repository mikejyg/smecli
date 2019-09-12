package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Test;

import mikejyg.cloep.ArgsParser;
import mikejyg.cloep.ArgsParser.ParseException;
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
	
	/**
	 * let a function that prints to a print writer, to print a string. 
	 * @param printer
	 * @return
	 */
	static public String printToString(Consumer<PrintWriter> printer) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(baos);

		printer.accept(writer);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);			
	}

	//////////////////////////////////////////////////////////////////////////

	@Test
	public void test() throws IOException, IllegalInputCharException, UnexpectedEofException {
		CommandExecutor commandExecutor = new CommandExecutor();
		
		SessionBase session = new SessionWithLoop(commandExecutor);
		ConsoleSession consoleSession = new ConsoleSession(session);
		
		final String outputFilename="cliTest.out";
		// execute commands from cliTestCommands.txt and write to test.out
		
		try ( InputStreamReader reader = new InputStreamReader( 
				this.getClass().getResourceAsStream("/cliTestCommands.txt"), StandardCharsets.UTF_8 ) ) {
			try ( PrintWriter printWriter = new PrintWriter(outputFilename) ) {
				
				consoleSession.setPrintWriter(printWriter);

				consoleSession.setReader(reader);
				consoleSession.setLocalEcho(true);
				
				assert( consoleSession.execAll().getReturnCode() == ReturnCode.NOP );
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
	
	public void execute() throws Exception {
		CommandExecutorWithSource commandExecutor = new CommandExecutorWithSource();
		SessionCommon sessionCommon = new SessionCommon(commandExecutor);
		ConsoleSession consoleSession = new ConsoleSession(sessionCommon);
	
		commandExecutor.setNewCliSessionFunc(()->{return consoleSession;});
		
		PrintWriter writer=null;
		if ( transcriptFilename != null ) {
			consoleSession.setContinueOnError(false);

			writer = new PrintWriter(transcriptFilename);
			sessionCommon.setSessionTranscriptor( new SessionTranscriptor(writer) );
		}
		
		if (commandStrs!=null) {
			consoleSession.setContinueOnError(false);

			consoleSession.setLocalEcho(true);
			CmdReturnType cmdReturn = commandExecutor.execCmd(commandStrs);
			consoleSession.flushPrintWriter();

			if ( cmdReturn.getReturnCode() != ReturnCode.NOP )
				System.out.print( cmdReturn.toString() + '\n' );
			
			
		} else if (interactiveFlag) {

			try (Reader reader = new InputStreamReader(System.in) ) {
				
				consoleSession.setReader(reader);
				
				consoleSession.setInteractiveFlag(true);
				consoleSession.execAll();
				
				consoleSession.flushPrintWriter();
				
			}
			
		} else {
			System.out.print("no action selected or performed.\n");
		}

		if (writer!=null)
			writer.close();	
		
	}
	
	//////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) throws Exception {
		CliTest cliTest = new CliTest();
		cliTest.parseArgs(args);
		cliTest.execute();
		
	}
	
}
