package mikejyg.smecli;

import java.io.IOException;
import java.io.Reader;

import mikejyg.smecli.CliBase.EofException;

/**
 * This class is for reading an CLI line from a buffered reader.
 * 
 * An CLI line here means an input unit, that is processed independently
 * 	, and it can contain multiple input lines.
 * 
 * The following mechanism is to pass multiple input lines into a single CLI line.

 * 1. The quotation character sequence: \"
 *   It denotes quoted text, everything within is copied verbatim, except the character sequence \" and \\" (see below).
 * 
 * 2. All sequences of \\" is changed to \".
 * 
 * So, to quote user inputs, the following steps are taken,
 * 
 * 1. Change all character sequences of \" to \\".
 * 
 * 2. Add \" at both ends. 
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
	
	private Reader reader;
	
	// working variables
	
	StringBuilder cliLineBuilder;
	
	boolean withinQuotes;
	
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
	 * 2. the escaped sequence \\" to \".
	 * 
	 * @param inputLine
	 * @throws IllegalInputCharException 
	 */
	static protected String transformQuote(String inputLine) throws IllegalInputCharException {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i=0; i<inputLine.length(); i++) {
			char c = inputLine.charAt(i);
			
			// check whether it is \" or \\"
			if (c=='\\') {
				if ( i+1 < inputLine.length() && inputLine.charAt(i+1)=='\"' ) {	// \" to QUOTE_CHAR
					i++;	// skip the quote character
					c = QUOTE_CHAR;

				} else if (i+2 < inputLine.length() 
						&& inputLine.charAt(i+1)=='\\' && inputLine.charAt(i+2)=='\"' )	// \\" to \"
					i++;			// skip the next \
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
				break;	
			}
			
			cliLineBuilder.append(c);
		}
	}
	
	/**
	 * @throws IOException 
	 * @throws EofException 
	 * @throws IllegalInputCharException 
	 * @throws UnexpectedEofException 
	 * 
	 * @return a read command line, or an empty string.
	 */
	public String readCliLine() throws IOException, EofException, IllegalInputCharException, UnexpectedEofException {
		if (eofFlag)
			throw new EofException();
		
		cliLineBuilder=new StringBuilder();
		withinQuotes = false;
		
		do {
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
			
			// add back the consumed line break LF
			if ( cliLineBuilder.length() != 0 )
				cliLineBuilder.append('\n');
			
			while ( pos < inputLine.length() ) {
				if ( withinQuotes ) {
					processWithinQuote();
				} else {
					processOutsideQuote();
				}
			}
			
		} while ( withinQuotes && !eofFlag ); 
		
		return cliLineBuilder.toString().trim();
		
	}
	
}
