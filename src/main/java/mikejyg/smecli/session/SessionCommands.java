package mikejyg.smecli.session;

import java.util.ArrayList;
import java.util.List;

import mikejyg.smecli.CliAnnotation.CliCommand;
import mikejyg.smecli.CliAnnotation;
import mikejyg.smecli.CliUtils;
import mikejyg.smecli.CmdCallType;
import mikejyg.smecli.CmdReturnType;
import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.smecli.CommandStruct;

/**
 * This class provides some basic commands for a session.
 * 
 * It uses annotation to add commands.
 * 
 * @author mikejyg
 *
 */
public class SessionCommands {
	private Session sessionRef;
	
	/////////////////////////////////////////////////
	
	public SessionCommands(Session session) {
		sessionRef = session;
	}
	
	public List<CommandStruct> getCommands() {
		ArrayList<CommandStruct> cmds = new ArrayList<>();
		
		cmds.add( new CommandStruct(
				"continueOnError", new String[]{"coe"}, "set whether to continue on command execution error."
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
				}) );
		
		cmds.addAll( CliAnnotation.getCliCommands(this) );
		
		return cmds;
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

