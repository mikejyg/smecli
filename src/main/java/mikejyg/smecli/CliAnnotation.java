package mikejyg.smecli;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * to provide a way of adding commands to a CLI through annotation.
 *  
 * @author jgu
 *
 */
public class CliAnnotation {
	
	/**
	 * a commandName function is of the form: CmdReturnType commandName(String argumentsStr)
	 * 
	 * see CliBase.execAll(CliSession session) for details on returns.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface CliCommand {
		String commandName() default "";	// when default, use the function name.
		String [] shorthands() default {};
		String helpString() default "";
	}
	
	public static void addMethods(CliCommands cliCommands, Object cmdObj) {
		Method [] methods = cmdObj.getClass().getMethods();
		
		for (Method method : methods) {
			CliCommand cliCmd = method.getAnnotation(CliCommand.class);
			if (cliCmd==null)
				continue;
			
			String commandName;
			if (cliCmd.commandName().isEmpty())
				commandName = method.getName();
			else
				commandName = cliCmd.commandName();
			
			cliCommands.addCommand(commandName, cliCmd.shorthands(), cliCmd.helpString()
					, (x) -> { 
						CmdReturnType r = null;
						try {
							r = (CmdReturnType) method.invoke(cmdObj, x);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						return r;
					});
			
		}

	}
	

}
