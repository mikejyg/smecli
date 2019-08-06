package mikejyg.smecli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.function.Function;

import mikejyg.smecli.CliLineReader.IllegalInputCharException;
import mikejyg.smecli.CliLineReader.UnexpectedEofException;

/**
 * A text console does the following,
 * 
 * 1. read an input item one at a time, and
 * 2. parse the input itme into a command name, and a arguments string, and
 * 3. call the corresponding command executor to execute the command.
 * 4. output command result.
 * 5. based on the return code, take the next action. 
 * 
 * modularity: command executors are modules, and can be added or removed at run-time.
 * 
 * extendability: CLI can be extended to change or expand its functionalities.
 * 
 * @author jgu
 *
 */
public class Cli {
	static public class EofException extends Exception {
		private static final long serialVersionUID = 1L;
	};
	
	static public class ExitAllSessions extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	static class CommandStruct {
		String commandName;
		String [] shorthands;
		String helpString;

		Function<CmdCallType, CmdReturnType> cmdFunc;
		
		@Override
		public String toString() {
			String str = commandName;
			for (String s : shorthands) {
				str += ", " + s;
			}
			str += "\t" + helpString;
			
			return str;
		}
	};
	
	///////////////////////////////////////////////////////////////////

	// behavior options:
	
	private String initialPrompt = "";
	
	private boolean initialLocalEcho;

	private boolean continueOnError;

	// command storage & indexes
	
	private Vector<CommandStruct> commands = new Vector<>();
	
	private Map<String, CommandStruct> cmdMap = new TreeMap<>();
	
	// for nested sessions
	private Vector<CliSession> sessionStack = new Vector<>();
	
	///////////////////////////////////////////////////////////

	/**
	 * read a command, that includes a command name and a arguments string.
	 * 
	 * a command name consists of alphanumeric chars, and is separated from the arguments string by a white space. 
	 *
	 * results are in commandName and argumentsStr
	 * 
	 * @return true if a command is read successfully, false otherwise.
	 * 
	 * @throws IOException 
	 * @throws EofException 
	 * @throws UnexpectedEofException 
	 * @throws IllegalInputCharException 
	 * 
	 */
	protected CmdCallType readCmd() throws IOException, EofException, IllegalInputCharException, UnexpectedEofException {
		String cmdLine=getCurrentSession().getCliLineReader().readCliLine();
		
		if (cmdLine.charAt(0)=='#') {
			getCurrentSession().getPrintWriter().println(cmdLine);
			return null;
		}
		
		CmdCallType cmdCall;
		
		int k = cmdLine.indexOf(' ');
		if (k==-1) {
			cmdCall = new CmdCallType(cmdLine, "");
		} else {
			cmdCall = new CmdCallType( cmdLine.substring(0, k).trim(), cmdLine.substring(k+1).trim() );
		}
		
		return cmdCall;
	}
	
	/**
	 *
	 * @return null if the command was not invoked successfully, otherwise a return from the function.
	 */
	protected CmdReturnType execCmd(CmdCallType cmdCall)  {
		CommandStruct cmdStruct = cmdMap.get(cmdCall.commandName);
		
		if (cmdStruct==null) {
			return new CmdReturnType(ReturnCode.INVALID_COMMAND);
		}
		
		return cmdStruct.cmdFunc.apply(cmdCall);
	}
	
	/**
	 * @return true to continue or not.
	 */
	protected boolean processResults(CmdReturnType cmdReturn) {
		if (cmdReturn.result!=null)
			getCurrentSession().getPrintWriter().println(cmdReturn.result);
		
		if (cmdReturn.returnCode != ReturnCode.SUCCESS) {
			getCurrentSession().getPrintWriter().println(cmdReturn.returnCode.name());
			
			if (continueOnError)
				return true;
			else
				return false;
			
		} else {
			getCurrentSession().getPrintWriter().println("OK.");
			return true;
		}
	}
	
	///////////////////////////////////////////////////////////
	
	public Cli() {}
	
	public void addCommand(String commandName, String shorthands[], String helpString, Function<CmdCallType, CmdReturnType> cmdFunc) {
		CommandStruct commandStruct=new CommandStruct();
		
		commandStruct.commandName = commandName;
		commandStruct.shorthands = shorthands;
		commandStruct.helpString = helpString;
		commandStruct.cmdFunc = cmdFunc;
		
		commands.add(commandStruct);
		cmdMap.put(commandStruct.commandName, commandStruct);
		
		for (String s : shorthands) {
			cmdMap.put(s, commandStruct);
		}

	}
	
	public CmdReturnType execAll(Reader reader, PrintWriter printWriter) throws IOException, IllegalInputCharException, UnexpectedEofException, ExitAllSessions {
		CliSession session = new CliSession(new BufferedReader(reader), printWriter, initialPrompt, initialLocalEcho);
		sessionStack.add(session);
		
		CmdReturnType cmdReturnType = execAll(getCurrentSession());
		
		sessionStack.remove(sessionStack.size()-1);

		return cmdReturnType;
	}
	
	public CmdReturnType execAll(Reader reader, Writer writer) throws IOException, IllegalInputCharException, UnexpectedEofException, ExitAllSessions {
		return execAll(reader, new PrintWriter(writer));
	}
	
	/**
	 * execute all commands from the reader.
	 * @throws IOException 
	 * @throws UnexpectedEofException 
	 * @throws IllegalInputCharException 
	 * @throws ExitAllSessions 
	 * 
	 * @return the return of the last command
	 */
	protected CmdReturnType execAll(CliSession session) throws IOException, UnexpectedEofException, IllegalInputCharException, ExitAllSessions {
		CmdReturnType cmdReturn=null;
		
		while (!session.isExitFlag()) {
			
			if (session.getPrompt()!=null)
				session.getPrintWriter().print(session.getPrompt());
			
			CmdCallType cmdCall;
			try {
				cmdCall = readCmd();
			} catch (EofException e) {
				session.getPrintWriter().println("EOF - exiting...");
				break;
			}
			
			if (cmdCall==null)
				continue;
				
			if (session.isLocalEcho())
				session.getPrintWriter().println(cmdCall.toString());
			
			cmdReturn = execCmd(cmdCall);

			if (session.isEndFlag())
				throw new ExitAllSessions();
			
			if (cmdReturn==null)
				throw new RuntimeException("failed to invoke: " + cmdCall);
				
			if ( ! processResults(cmdReturn) )
				break;
		}
		
		return cmdReturn;
	}
	
	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	public void setLocalEcho(boolean localEcho) {
		this.initialLocalEcho = localEcho;
	}

	public void setPrompt(String prompt) {
		this.initialPrompt = prompt;
	}

	public CliSession getCurrentSession() {
		return sessionStack.lastElement();
	}
	
	public Vector<CommandStruct> getCommands() {
		return commands;
	}

	
}
