package mikejyg.smecli.session;

import java.io.PrintWriter;

import mikejyg.smecli.CmdReturnType;

/**
 * writes commands and results to a writer.
 * 
 * @author mikejyg
 *
 */
public class SessionTranscriptor {
	private PrintWriter writer;
	
	public SessionTranscriptor(PrintWriter writer) {
		this.writer = writer;
	}
	
	public void onCmdLine(String l) {
			writer.print("> " + l + '\n');
	}
	
	public void onCmdReturn(CmdReturnType r) {
		if (r.getReturnCode().isCmdExecResult())
				writer.write(r.getReturnCode().toString() + '\n' + r.getResult() + '\n');
	}

	
}
