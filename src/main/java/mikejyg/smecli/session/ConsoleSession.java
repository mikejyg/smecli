package mikejyg.smecli.session;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;

/**
 * A session that has console interactions.
 * 
 * @author mikejyg
 *
 */
public class ConsoleSession implements SessionIntf {
	private Session session;
	
	private ConsoleSessionCommon consoleSessionCommonRef;
	
	/**
	 * default value for an interactive session: false
	 */
	private boolean localEcho=false;

	/**
	 * interactive means, reading user input from console.
	 */
	private boolean interactiveFlag=true;
	
	/////////////////////////////////////////////////////
	
	public ConsoleSession(Session session) {
		this.session = session;
		session.setContinueOnError(true);	// the first console session is the root console session.
		
		consoleSessionCommonRef = new ConsoleSessionCommon(session.getSessionCommonRef());
		
		session.setPromptFunc(()->{
			if ( ! consoleSessionCommonRef.isPrompted() && ! getPrompt().isEmpty() ) {
				getPrintWriter().print(getPrompt());
				getPrintWriter().flush();
				consoleSessionCommonRef.setPrompted(true);
			}
		});
		
		initCmdLineListener();
		
		session.getCommandExecutorRef().addMethods(this);
	}
	
	/**
	 * copy constructor
	 * @param parentSession
	 */
	public ConsoleSession(ConsoleSession parentSession) {
		session = parentSession.session.newSubSession();
		session.setContinueOnError(false);
		
		// copy settings
		this.consoleSessionCommonRef = parentSession.consoleSessionCommonRef;
		
		localEcho = true;
		
		initCmdLineListener();
	}
	
	/**
	 * generate a new session, after the current session.
	 * This method is meant to be polymorphic.
	 * @return
	 */
	@Override
	public ConsoleSession newSubSession() {
		return new ConsoleSession(this);
	}
	
	private void initCmdLineListener() {
		session.getSessionCommonRef().setCmdLineListener( (l)->{
			if (interactiveFlag)
				consoleSessionCommonRef.setPrompted(false);
			
			if (localEcho) {
				getPrintWriter().print(l + '\n');
				getPrintWriter().flush();
				consoleSessionCommonRef.setPrompted(false);
			}
		});
		
	}
	
	@Override
	public void setReader(Reader reader) {
		session.setReader(reader);
	}
	
	@Override
	public CmdReturnType execAll() {
		return session.execAll();
	}
	
	@CliCommand(helpString = "With an argument, set local echo to on or off, or without argument, show current local echo state.")
	public CmdReturnType localEcho(CmdCallType cmdCall) {
		String arg = CliUtils.getArg0(cmdCall);
		if ( arg.isEmpty())
			return new CmdReturnType(ReturnCode.OK, isLocalEcho() ? "on" : "off");
		
		if ( arg.contentEquals("on") ) {
			setLocalEcho(true);
			
		} else if ( arg.contentEquals("off") ) {
			setLocalEcho(false);
			
		} else {
			return new CmdReturnType(ReturnCode.INVALID_ARGUMENT);
		}
		
		return new CmdReturnType(ReturnCode.OK, isLocalEcho() ? "on" : "off");
			
	}

	///////////////////////////////////////////////////
	
	public boolean isLocalEcho() {
		return localEcho;
	}

	public void setLocalEcho(boolean localEcho) {
		this.localEcho = localEcho;
	}

	protected PrintWriter getPrintWriter() {
		return consoleSessionCommonRef.getPrintWriter();
	}

	public String getPrompt() {
		return consoleSessionCommonRef.getPrompt();
	}
	
	public void setPrompt(String prompt) {
		consoleSessionCommonRef.setPrompt(prompt);
	}
	
	public void setInteractiveFlag(boolean interactiveFlag) {
		this.interactiveFlag = interactiveFlag;
	}
	
	public void setPrintWriter(PrintWriter printWriter) {
		consoleSessionCommonRef.setPrintWriter(printWriter);
	}
	
	public void flushPrintWriter() {
		consoleSessionCommonRef.flushPrintWriter();
	}
	
	public ConsoleSessionCommon getConsoleSessionCommonRef() {
		return consoleSessionCommonRef;
	}
	
	public void setContinueOnError(boolean continueOnError) {
		session.setContinueOnError(continueOnError);
	}

	public Session getSession() {
		return session;
	}

	/**
	 * run an interactive session (fron stdin).
	 * @throws IOException
	 */
	public static void runInteractive(ConsoleSession cli) throws IOException {
		try (InputStreamReader reader = new InputStreamReader(System.in) ) {
			cli.setReader( reader );
			cli.setInteractiveFlag(true);
			cli.execAll();
		}
	}


}
