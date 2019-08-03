package mikejyg.smecli;

import java.io.IOException;
import java.io.Reader;

import mikejyg.smecli.Cli.EofException;

/**
 * This class is for reading an CLI line from a buffered reader.
 * 
 * An CLI line here means an input unit, that is processed independently
 * 	, and it can contain multiple input lines.
 * 
 * The following mechanism is to pass multiple input lines into a single CLI line.

 * The character sequence \\ becomes the regular \ with no special meaning.
 *
 * 1. the quotation character sequence: \"
 *   It denotes quoted text, everything within is copied verbatim, except the sequence \\ and the end of line \.
 * 
 * 2. The end of line \ is for joining the next line, with a space inserted in place of the line break.
 * 	Note: any white spaces between \ and end of line are ignored, and it is still an end of line \.
 *
 * For cross-platform compatibility,
 * 	The newline character will be LF (\n).
 *  The sequence CR LF (\r\n) will be transformed to a single LF, by omitting \r.
 *
 * Input character space verification,
 * 	Allowed input character space: any ASCII code equal or greater than 0x20 (the space character), whitespace characters, CR and LF.
 * 
 * Character space expansion and transformation,
 * 	The unused portion of the ASCII code is re-used to expand the character space, that is used internally to aid processing.
 * 
 * @author jgu
 *
 */
public class CliLineReader {
	
	static public class IllegalInputCharException extends Exception {
		private static final long serialVersionUID = 1L;
	};
	
	static public class UnexpectedEofException extends Exception {
		private static final long serialVersionUID = 1L;
	};
	
	////////////////////////////////////////////////////////////
	
	/**
	 * the special symbol begin/end of quote is mapped to 0x01;
	 */
	private static final char QUOTE_CHAR = 0x01;
	
	/**
	 * the special symbol for continue on next line is mapped to 0x02;
	 */
	private static final char CONT_NEXT_LINE_CHAR = 0x02;
	
	private Reader reader;
	
	// working variables
	
	StringBuilder cliLineBuilder;
	
	boolean withinQuotes;
	boolean continueNextLine;
	
	boolean eofFlag;
	
	String inputLine;
	int pos;			// the current processing index into the inputLine
	
	////////////////////////////////////////////////////////////
	
	public CliLineReader(Reader reader) {
		this.reader = reader;
	}

	////////////////////////////////////////////////////////////
	
	/**
	 * read an input line, and
	 * 
	 * - validate allowed character set
	 * - remove \r
	 * - convert all whitespace to space
	 * 
	 * @return
	 * @throws IOException
	 * @throws IllegalInputCharException
	 */
	protected String readInputLine() throws IOException, IllegalInputCharException {
		StringBuilder stringBuilder = new StringBuilder();
		
		while (true) {
			int k = reader.read();
			
			if (k==-1) {	// EOF
				eofFlag = true;
				break;
			}
			
			char c = (char) k;
			
			if ( c < 0x20 ) {
				if (c=='\r')
					continue;	// skip \r

				if (c=='\n')
					break;		// end of line

				if ( ! Character.isWhitespace(c) )
					throw new IllegalInputCharException();
				else
					c = ' ';
			}
			
			stringBuilder.append(c);
		}
		
		return stringBuilder.toString();
	}
	
	/**
	 * transform
	 * 1. the quote escape sequence \" to the special character space.
	 * 2. the end of line \ to the special character
	 * 3. \\ to \
	 * 
	 * @param inputLine
	 * @throws IllegalInputCharException 
	 */
	static protected String transformQuote(String inputLine) throws IllegalInputCharException {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i=0; i<inputLine.length(); i++) {
			char c = inputLine.charAt(i);
			
			if (c=='\\') {
				if (i+1 >= inputLine.length()) {	// \ at end of line
					c = CONT_NEXT_LINE_CHAR;
					
				} else {
					char c1 = inputLine.charAt(i+1);
					
					if ( c1 == '\"' ) {		// quote
						i++;			// skip the quote character
						c = QUOTE_CHAR;
						
					} else if ( c1 == '\\' )	{ // \
						i++;			// skip the extra \ 
					
					} else {
						// look ahead to see if this is the end of line \
						int pos2 = i+1;
						while ( pos2 < inputLine.length() ) {
							c1 = inputLine.charAt(pos2);
							if ( ! Character.isWhitespace(c1) )
								break;
							pos2++;
						}
						if (pos2==inputLine.length()) {
							// this is an end of line joiner
							c = CONT_NEXT_LINE_CHAR;
							i = inputLine.length();
						}
					}
				}
			}
			
			stringBuilder.append(c);
		}
		return stringBuilder.toString();
	}
	
	/**
	 * not within a quote, need to,
	 * 
	 * 1. transform \\ to \
	 * 2. process end of line \ to continuation
	 */
	protected void processOutsideQuote() {
		while ( pos < inputLine.length() ) {
			char c = inputLine.charAt(pos++);
			
			if (c==QUOTE_CHAR) { // begin of quote
				// set quote flag and exit
				withinQuotes=true;
				return;			
					
			} else if ( c == CONT_NEXT_LINE_CHAR ) {	// end of line joiner
				continueNextLine = true;
				break;
			}
					
			cliLineBuilder.append(c);
		}
	}
	
	/**
	 * within a quote, need to,
	 * 
	 * transform \\ to \
	 */
	protected void processWithinQuote() {
		while ( pos < inputLine.length() ) {
			char c = inputLine.charAt(pos++);
			
			if ( c == QUOTE_CHAR ) {	// end of quote
				// release quote flag and exit
				withinQuotes=false;
				return;
				
			} else if ( c == CONT_NEXT_LINE_CHAR ) {
				continueNextLine = true;
				break;
			}
			
			cliLineBuilder.append(c);
		}
		
		if (!continueNextLine) { // end of line, add line break
			cliLineBuilder.append('\n');
		}
	}
	
	/**
	 * @throws IOException 
	 * @throws EofException 
	 * @throws IllegalInputCharException 
	 * @throws UnexpectedEofException 
	 * 
	 */
	public String readCliLine() throws IOException, EofException, IllegalInputCharException, UnexpectedEofException {
		if (eofFlag)
			throw new EofException();
		
		cliLineBuilder=new StringBuilder();
		withinQuotes = false;
		
		continueNextLine = true;
		
		while ( ( withinQuotes || continueNextLine ) && !eofFlag ) {
			continueNextLine = false;
			
			inputLine = readInputLine();
			
			if ( inputLine.isEmpty() && eofFlag ) {
				if ( cliLineBuilder.length() != 0 )
					throw new UnexpectedEofException();
				else
					throw new EofException();
			}
			
			pos = 0;

			inputLine = transformQuote(inputLine);
			
			// if not within quote, trim the front white spaces
			if ( ! withinQuotes ) {
				while (pos < inputLine.length()) {
					char c = inputLine.charAt(pos);
					if ( Character.isWhitespace(c) )
						pos++;
					else
						break;
				}	
			}
			
			// use a space in place of the line break
			if ( !withinQuotes && cliLineBuilder.length()!=0)
				cliLineBuilder.append(' ');
			
			while ( pos < inputLine.length() ) {
				if ( withinQuotes ) {
					processWithinQuote();
				} else {
					processOutsideQuote();
				}
			}
			
		}
		
		return cliLineBuilder.toString().trim();
		
	}
	
}
