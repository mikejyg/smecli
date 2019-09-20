package mikejyg.smecli.session;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;
import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.cmdexecutor.CommandExecutorIntf;

/**
 * A session that has console interactions.
 * 
 * @author mikejyg
 *
 */
public class ConsoleSession implements SessionIntf {
	private SessionBase sessionBase;
	
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
	
	public ConsoleSession(SessionBase session) {
		this.sessionBase = session;
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
		
		session.addMethods(this);
		
		initSourceCommand();
	}
	
	public ConsoleSession(CommandExecutorIntf commandExecutor) {
		this( new SessionWithLoop(commandExecutor) );
	}
	
	public ConsoleSession(SessionCommon sessionCommon) {
		this( new SessionWithLoop(sessionCommon) );
	}
	
	/**
	 * copy constructor
	 * @param parentSession
	 */
	public ConsoleSession(ConsoleSession parentSession) {
		sessionBase = parentSession.sessionBase.newSession();
		sessionBase.setContinueOnError(false);
		
		// copy settings
		this.consoleSessionCommonRef = parentSession.consoleSessionCommonRef;
		
		localEcho = true;
		
		initCmdLineListener();
		
		initSourceCommand();
	}
	
	/**
	 * generate a new session, after the current session.
	 * This method is meant to be polymorphic.
	 * @return
	 */
	public ConsoleSession newSession() {
		return new ConsoleSession(this);
	}
	
	private void initCmdLineListener() {
		sessionBase.setCmdLineListener( (l)->{
			if (interactiveFlag)
				consoleSessionCommonRef.setPrompted(false);
			
			if (localEcho) {
				getPrintWriter().print(l + '\n');
				getPrintWriter().flush();
				consoleSessionCommonRef.setPrompted(false);
			}
		});
		
	}
	
	private void initSourceCommand() {
		// override the source command
		SourceCommand sourceCommandExecutor = new SourceCommand( ()->{
			return newSession();
		});
		sessionBase.addMethods(sourceCommandExecutor);
	}
	
	@Override
	public void setReader(Reader reader) {
		sessionBase.setReader(reader);
	}
	
	@Override
	public CmdReturnType execAll() throws IOException, UnexpectedEofException, IllegalInputCharException {
		return sessionBase.execAll();
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
		sessionBase.setContinueOnError(continueOnError);
	}

	public SessionBase getSessionBase() {
		return sessionBase;
	}

	/**
	 * run an interactive session (fron stdin).
	 * @throws IOException
	 * @throws UnexpectedEofException
	 * @throws IllegalInputCharException
	 */
	public static void runInteractive(ConsoleSession cli) throws IOException, UnexpectedEofException, IllegalInputCharException {
		try (InputStreamReader reader = new InputStreamReader(System.in) ) {
			cli.setReader( reader );
			cli.setInteractiveFlag(true);
			cli.execAll();
		}
	}


}
