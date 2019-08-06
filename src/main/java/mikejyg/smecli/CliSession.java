package mikejyg.smecli;

import java.io.PrintWriter;
import java.io.Reader;

/**
 * a session is tied to a pair of reader and writer.
 * 
 * @author jgu
 *
 */
public class CliSession {
	private CliLineReader cliLineReader;
	
	private PrintWriter printWriter;
	
	private String prompt;
	
	private boolean localEcho;

	// working variables
	private boolean exitFlag;

	private boolean endFlag;	// exit all (nested) sessions.
	
	/////////////////////////////////////////////////////
	
	public CliSession(Reader reader, PrintWriter printWriter
			, String prompt, boolean localEcho) {
		cliLineReader = new CliLineReader(reader);
		this.printWriter = printWriter;
		this.prompt = prompt;
		this.localEcho = localEcho;
	}
	
	public boolean isExitFlag() {
		return exitFlag;
	}

	public void setExitFlag(boolean exitFlag) {
		this.exitFlag = exitFlag;
	}

	public String getPrompt() {
		return prompt;
	}

	public boolean isLocalEcho() {
		return localEcho;
	}

	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	public CliLineReader getCliLineReader() {
		return cliLineReader;
	}

	public boolean isEndFlag() {
		return endFlag;
	}

	public void setEndFlag(boolean endFlag) {
		this.endFlag = endFlag;
	}

	
}
