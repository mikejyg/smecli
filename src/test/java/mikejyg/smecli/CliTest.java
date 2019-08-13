package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Test;

import mikejyg.smecli.CliBase.ExitAllSessions;
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
	public void test() throws IOException, IllegalInputCharException, UnexpectedEofException, ExitAllSessions {
		
		// build a CLI with a base module.
		
		CliBase cli = new CliBase();
		CliAdapter cliBaseModule = new CliAdapter(cli);
		CliAnnotation.addMethods(cli, cliBaseModule);
		
		cli.setPrompt("> ");
		cli.setContinueOnError(true);
		cli.setLocalEcho(true);

		final String outputFilename="cliTest.out";
		
		// execute commands from cliTestCommands.txt and write to test.out
		
		try ( Reader reader = new InputStreamReader( this.getClass().getResourceAsStream("/cliTestCommands.txt"), StandardCharsets.UTF_8 ) ) {
			try ( Writer outputWriter = new OutputStreamWriter( new FileOutputStream(outputFilename), StandardCharsets.UTF_8) ) { 
				cli.execAll(reader, outputWriter);
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
	
	public static void main(String[] args) throws IOException, IllegalInputCharException, UnexpectedEofException, ExitAllSessions {
		CliBase cli = new CliBase();
		CliAdapter cliBaseModule = new CliAdapter(cli);
		CliAnnotation.addMethods(cli, cliBaseModule);
		
		cli.setPrompt("> ");
		cli.setContinueOnError(true);
		cli.execAll(new InputStreamReader(System.in), new PrintWriter(System.out));
		
	}
	
}
