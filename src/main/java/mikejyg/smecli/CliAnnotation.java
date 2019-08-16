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
public class CliAnnotation extends CliBase {
	/**
	 * a commandName function is of the form: CmdReturnType commandName(String argumentsStr)
	 * The return can be null, in which case it means the command itself has no result,
	 *   e.g. when the source command complete successfully
	 *   (as the result of the last command within the source, is assumed to have been captured already).
	 *  
	 * @param argumentsStr	a string contains device specific arguments... 
	 * @return
	 */
	@Retention(RetentionPolicy.RUNTIME)
	static public @interface CliCommand {
		String commandName() default "";	// when default, use the function name.
		String [] shorthands() default {};
		String helpString() default "";
	}
	
	public void addMethods(Object cmdObj) {
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
			
			addCommand(commandName, cliCmd.shorthands(), cliCmd.helpString()
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
