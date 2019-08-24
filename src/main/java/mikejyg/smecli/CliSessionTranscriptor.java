package mikejyg.smecli;

import java.io.IOException;
import java.io.Writer;

/**
 * writes commands and results to a writer.
 * 
 * @author mikejyg
 *
 */
public class CliSessionTranscriptor {
	private boolean writerFailed;
	
	public CliSessionTranscriptor(Writer writer, CliBase cliBase) {
		cliBase.setCmdExecListener((c)->{
			if ( ! writerFailed ) {
				try {
					writer.write("> ");
					writer.write(c.toString());
					writer.write('\n');
				} catch (IOException e) {
					e.printStackTrace();
					writerFailed=true;
				}
			}
		});
		
		cliBase.setCmdReturnListener((r)->{
			if ( ! writerFailed ) { 
				try {
					writer.write(r.getReturnCode().toString());
					writer.write('\n');
					writer.write(r.getResult());
					writer.write('\n');
				} catch (IOException e) {
					e.printStackTrace();
					writerFailed=true;
				}
			}
			
			cliBase.processResults(r);
		});
		
	}
	
	
}
