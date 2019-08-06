package mikejyg.smecli;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;

import mikejyg.smecli.Cli.EofException;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;

public class CliLineReaderTest {
	
	@Test
	public void test() throws IOException, EofException, IllegalInputCharException, UnexpectedEofException {
		String golden[]= {
				"command 1st line",
				"c2 2nd line  continued 2nd done.",
				"quoted abc def",
				"quoted multi\nline",
				"unspecial chars \\ \\? \\",
				"regular quotes: \"excellent!\"",
				"all done."
		};
		
		try ( Reader inputReader = new InputStreamReader( this.getClass().getResourceAsStream("/linereaderTestInputs.txt")) ) {
			CliLineReader cliLineReader = new CliLineReader( inputReader );
		
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
	
	
}
