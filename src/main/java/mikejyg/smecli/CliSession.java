package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * a session is tied to a pair of reader and writer.
 * 
 * @author jgu
 *
 */
public class CliSession {
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	
	private String prompt;
	
	private boolean localEcho;

	// working variables
	private boolean exitFlag;

	/////////////////////////////////////////////////////
	
	public CliSession(BufferedReader bufferedReader, PrintWriter printWriter
			, String prompt, boolean localEcho) {
		this.bufferedReader = bufferedReader;
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

	public BufferedReader getBufferedReader() {
		return bufferedReader;
	}

	public String getPrompt() {
		return prompt;
	}

	public boolean isLocalEcho() {
		return localEcho;
	}

	public void close() throws IOException {
		bufferedReader.close();
		printWriter.close();
	}
	
	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	
}
