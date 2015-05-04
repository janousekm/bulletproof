package au.com.bulletproof.maja;

import static org.springframework.util.StringUtils.hasText;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * This class checks program arguments and if there is some input file name in
 * first argument it invokes {@link CsvUpdateService} to process input csv file
 * 
 * @author Martin Janousek
 */
@Component
public class ArgumentsProcessor implements CommandLineRunner {
	private static final String ERR_MSG = "Program arguments must not be empty. Please set csv file path as the first argument.";

	@Autowired
	private CsvUpdateService csvUpdateService;

	@Override
	public void run(String... args) throws Exception {
		if (args.length == 0 || !hasText(args[0])) {
			System.out.println(ERR_MSG);
		} else {
			csvUpdateService.process(new File(args[0]));
		}
	}
}