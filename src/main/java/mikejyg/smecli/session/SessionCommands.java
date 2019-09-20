package mikejyg.smecli.session;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.commands.AssertCommand;

/**
 * This class provides some basic commands for a session.
 * 
 * It uses annotation to add commands.
 * 
 * @author mikejyg
 *
 */
public class SessionCommands {
	private SessionBase sessionRef;
	
	/////////////////////////////////////////////////
	
	public SessionCommands(SessionBase session) {
		sessionRef = session;
		
		sessionRef.addMethods(this);
		addCommands();
	}
	
	protected void addCommands() {
		sessionRef.addCommand("continueOnError", new String[]{"coe"}, "set whether to continue on command execution error."
				+ " if no argument is given, prints out current state, otherwise use argument on or off."
				, (CmdCallType cmdCall)->{
					String arg = CliUtils.getArg0(cmdCall);
					if ( arg.isEmpty() )
						return new CmdReturnType(ReturnCode.OK, sessionRef.isContinueOnError() ? "on" : "off");

					if ( arg.contentEquals("on") ) {
						sessionRef.setContinueOnError(true);

					} else if ( arg.contentEquals("off") ) {
						sessionRef.setContinueOnError(false);

					} else {
						return new CmdReturnType(ReturnCode.INVALID_ARGUMENT);
					}

					return new CmdReturnType(ReturnCode.OK, sessionRef.isContinueOnError() ? "on" : "off");
				});
		
		AssertCommand.addToCliCommands(sessionRef, ()->{return sessionRef.getLastCmdReturn();});
	}		
	
	@CliCommand(shorthands = {"?"}, helpString = "print help.")
	public CmdReturnType help(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.OK, sessionRef.toHelpString());
	}
	
	@CliCommand(commandName="exit", helpString = "exit current session with an optional argument.")
	public CmdReturnType exitSession(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.EXIT, CliUtils.getArg0(cmdCall));
	}

	@CliCommand(helpString = "exit current session and all parent sessions with an optional argument.")
	public CmdReturnType end(CmdCallType cmdCall) {
		return new CmdReturnType(ReturnCode.END, CliUtils.getArg0(cmdCall));
	}


}

