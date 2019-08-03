package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		String goldenOutput[]= { "> help" 
				, "help, ?	print help."
				, "exit	exit current stream."
				, "echo	echo arguments."
				, "OK."
				, "> ?"
				, "help, ?	print help."
				, "exit	exit current stream."
				, "echo	echo arguments."
				, "OK."
				, "> badCmd"
				, "INVALID_COMMAND"
				, "> echo abc def and \\n"
				, "abc def and \\n"
				, "OK."
				, "> exit"
				, "exit()..."
				, "OK." };
		
//		PrintWriter golden = new PrintWriter(new File("golden.out"));
//		Arrays.stream(goldenOutput).forEach(x->golden.println(x));
//		golden.close();
//		
//		PrintWriter test = new PrintWriter(new File("test.out"));
//		test.println(output);
//		test.close();
		
		Set<String> goldenSet = new HashSet<>(Arrays.asList(goldenOutput));
		
		Set<String> outputSet = new HashSet<>();
		BufferedReader outputReader = new BufferedReader( new StringReader(output) );
		while (true) {
			String str = outputReader.readLine();
			if (str!=null)
				outputSet.add(str);
			else
				break;
		}
		
		assert( goldenSet.equals(outputSet) );
		
	}
	
	
}
