package mikejyg.smecli;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;

import mikejyg.smecli.CliLineReader;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;

public class CliLineReaderTest {
	
	@Test
	public void test() throws IOException, CliLineReader.EofException, IllegalInputCharException, UnexpectedEofException {
		String golden[]= {
				"command 1st line",
				"c2 2nd line\ncontinued \n   2nd done.",
				"quoted abc def end quote",
				"unspecial chars \\\\ \\? \\\\",
				"regular quotes: \"excellent!\" user's \\\" \\\"",
				"all done."
		};
		
		try ( Reader inputReader = new InputStreamReader( this.getClass().getResourceAsStream("/lineReaderTestInputs.txt")) ) {
			CliLineReader cliLineReader = new CliLineReader( inputReader );
		
			int cnt=0;
			while (true) {
				String cliLine;
				try {
					cliLine = cliLineReader.readCliLine();
				} catch (CliLineReader.EofException e) {
					break;
				}

				System.out.print(cnt);
				System.out.println( ": " + cliLine );
				
				assert( golden[cnt].contentEquals(cliLine) );
				
				cnt++;
			}
		
		}
		
	}
	
	
}
