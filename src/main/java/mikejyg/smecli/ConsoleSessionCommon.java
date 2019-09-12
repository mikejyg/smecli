package mikejyg.smecli;

import java.io.PrintWriter;

/**
 * This class holds the settings and variables common to all console sessions,
 *   but not included in SessionCommon.
 *   
 * @author mikejyg
 *
 */
public class ConsoleSessionCommon {
	
	// hold a reference to a SessionCommon.
	private SessionCommon sessionCommonRef;
	
	private String prompt = "> ";
	
	/**
	 * where to print out things, including prompt, results, errors, status...
	 */
	private PrintWriter printWriter;

	/**
	 * whether there is an prompt outstanding.
	 */
	private boolean prompted;
	
	////////////////////////////////////////////////////////////
	
	public ConsoleSessionCommon(SessionCommon sessionCommon) {
		this.sessionCommonRef = sessionCommon;
		
		printWriter = new PrintWriter(System.out);
		
		sessionCommonRef.setCmdReturnListener( (r)->{
			if ( r.getReturnCode().isCmdExecResult() )
				processResults(r); 
		});
	}
	
	protected void processResults(CmdReturnType cmdReturn) {
		getPrintWriter().print(cmdReturn.getReturnCode().name() + '\n');
		
		String result = cmdReturn.getResult();
		if ( result!=null && ! result.isEmpty() ) {
			getPrintWriter().print(cmdReturn.getResult() + '\n');
			setPrompted(false);
		}
	}
	
	////////////////////////////////////////////////////////////
	
	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	public void flushPrintWriter() {
		printWriter.flush();
	}
	
	public void setPrintWriter(PrintWriter printWriter) {
		this.printWriter = printWriter;
	}

	public boolean isPrompted() {
		return prompted;
	}

	public void setPrompted(boolean prompted) {
		this.prompted = prompted;
	}

	public SessionCommon getSessionCommonRef() {
		return sessionCommonRef;
	}


}
