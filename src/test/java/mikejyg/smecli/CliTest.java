package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Test;

import mikejyg.smecli.CliCommands.InvokeCommandFailed;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;

public class CliTest {

	static public String printToString(Consumer<PrintStream> printer) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);

		printer.accept(ps);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);			
	}

	@Test
	public void test() throws IOException, IllegalInputCharException, UnexpectedEofException {
		CommandExecutorBase commandExecutor = new CommandExecutor();
		CliAdapter cli = new CliAdapter(commandExecutor);
		
		cli.setPrompt("> ");
		cli.setLocalEcho(true);

		final String outputFilename="cliTest.out";
		
		// execute commands from cliTestCommands.txt and write to test.out
		
		try ( BufferedReader reader = new BufferedReader(new InputStreamReader( this.getClass().getResourceAsStream("/cliTestCommands.txt"), StandardCharsets.UTF_8 ) ) ) {
			try ( PrintStream printStream = new PrintStream( new FileOutputStream(outputFilename) ) ) {
				cli.setPrintStream(printStream);
				assert( cli.execAll(reader) == null );
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
		CommandExecutorBase commandExecutor = new CommandExecutor();
		CliAdapter cli = new CliAdapter(commandExecutor);
		
		cli.setPrompt("> ");
		
		if (args.length<1) {	// no argument, run in interactive mode
			cli.execAll(new BufferedReader(new InputStreamReader(System.in)));
			
		} else {	// execute args as a command
			
			// we do not want continue on error in non-interactive mode
			cli.setContinueOnError(false);
			
			CmdReturnType cmdReturn = cli.execCmd(args);
			if (cmdReturn!=null)
				System.out.println( cmdReturn );
		}
		
	}
	
}
