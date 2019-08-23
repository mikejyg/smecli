package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Test;

import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CmdReturnType.ReturnCode;

public class CliTest {

	static public String printToString(Consumer<PrintStream> printer) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);

		printer.accept(ps);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);			
	}

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

				CliAdapter cli = new CliAdapter(cliBase);
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
	
	public static void main(String[] args) throws IOException, IllegalInputCharException, UnexpectedEofException, InvokeCommandFailed {
		CommandExecutor commandExecutor = new CommandExecutor();
		
		if (args.length<1) {	// no argument, run in interactive mode
			try (Reader reader = new InputStreamReader(System.in) ) {
				CliAdapter cli = commandExecutor.newCliAdapter(reader);
				cli.setLocalEcho(false);
				cli.setContinueOnError(true);
				cli.execAll();
			}
			
		} else {	// execute args as a command
			CmdCallType cmdCall = CmdCallType.toCmdCall(args);
			if (! cmdCall.isEmpty() )
				System.out.println( commandExecutor.execCmd(cmdCall) );
			else
				System.out.println( "no command is found: " + Arrays.deepToString(args) );
			
		}
		
	}
	
}
