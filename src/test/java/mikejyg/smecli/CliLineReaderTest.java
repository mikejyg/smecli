package mikejyg.smecli;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import mikejyg.smecli.Cli.EofException;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;

public class CliLineReaderTest {
	
	@Test
	public void test() throws IOException, EofException, IllegalInputCharException, UnexpectedEofException {
		String inputs="";
		inputs += "command 1st line" + System.lineSeparator()
			+ "c2 2nd line \\" + System.lineSeparator()
			+ "continued\\ " + System.lineSeparator()
			+ "   2nd done." + System.lineSeparator();
		
		inputs += "quoted \\\"abc def\\\"" + System.lineSeparator();
		inputs += "quoted \\\"multi" + System.lineSeparator()
			+ "line \\\" " + System.lineSeparator();

		inputs += "unspecial chars \\\\ \\? \\\\ " + System.lineSeparator();
		inputs += "regular quotes: \"excellent!\"" + System.lineSeparator();
		inputs += "all done." + System.lineSeparator();

		System.out.println("inputs:");
		System.out.println(inputs);
		
		CliLineReader cliLineReader = new CliLineReader(new StringReader(inputs));
		
		String golden[]= {
				"command 1st line",
				"c2 2nd line  continued 2nd done.",
				"quoted abc def",
				"quoted multi\nline",
				"unspecial chars \\ \\? \\",
				"regular quotes: \"excellent!\"",
				"all done."
		};
		
		int cnt=0;
		while (true) {
			String cliLine;
			try {
				cliLine = cliLineReader.readCliLine();
			} catch (EofException e) {
				break;
			}
			
			assert( golden[cnt].contentEquals(cliLine) );
			
			System.out.print(cnt++);
			System.out.println( ": " + cliLine );
		}
		
	}
	
	
}
