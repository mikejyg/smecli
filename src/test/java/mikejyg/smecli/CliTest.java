package mikejyg.smecli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
//import java.io.File;
//import java.io.PrintWriter;

import org.junit.Test;

public class CliTest {

	static public String printToString(Consumer<PrintStream> printer) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);

		printer.accept(ps);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);			
	}

	@Test
	public void test() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		Cli cli = new Cli();

		cli.setPrompt("> ");
		cli.setContinueOnError(true);
		cli.setLocalEcho(true);

		String commands = "help" + System.lineSeparator();
		commands += "?" + System.lineSeparator();
		commands += "badCmd" + System.lineSeparator();
		commands += "echo abc\\" + System.lineSeparator();
		commands += "def and \\" + "n" + System.lineSeparator();
		commands += "exit";

		Reader reader = new StringReader(commands);
		
		String output = printToString( (ps)->{
			try {
				cli.execAll(reader, new OutputStreamWriter(ps));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		String goldenOutput="> help" + System.lineSeparator() + 
				"help, ?	print help." + System.lineSeparator() + 
				"echo	echo arguments." + System.lineSeparator() + 
				"exit	exit current stream." + System.lineSeparator() + 
				"OK." + System.lineSeparator() + 
				"> ?" + System.lineSeparator() + 
				"help, ?	print help." + System.lineSeparator() + 
				"echo	echo arguments." + System.lineSeparator() + 
				"exit	exit current stream." + System.lineSeparator() + 
				"OK." + System.lineSeparator() + 
				"> badCmd" + System.lineSeparator() + 
				"INVALID_COMMAND" + System.lineSeparator() + 
				"> echo abc def and \\n" + System.lineSeparator() + 
				"abc def and \\n" + System.lineSeparator() + 
				"OK." + System.lineSeparator() + 
				"> exit" + System.lineSeparator() + 
				"exit()..." + System.lineSeparator() + 
				"OK." + System.lineSeparator();
		
//		PrintWriter golden = new PrintWriter(new File("golden.out"));
//		golden.println(goldenOutput);
//		golden.close();
//		
//		PrintWriter test = new PrintWriter(new File("test.out"));
//		test.println(output);
//		test.close();
		
		assert(goldenOutput.contentEquals(output));
		
	}
	
	
}
