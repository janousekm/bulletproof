package au.com.bulletproof.maja;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test for {@link CsvUpdateService}
 * 
 * @author Martin Janousek
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MajaExerciseApplication.class)
public class CsvUpdateServiceTest {

	@Autowired
	private CsvUpdateService csvUpdateService;

	@Test(expected = IllegalArgumentException.class)
	public void processNonExistingFile() throws Exception {
		File csvFile = new File("/this/file/surely/does/not/exist.csv");
		csvUpdateService.process(csvFile);
	}

	@Test(expected = IllegalArgumentException.class)
	public void processDirectory() throws Exception {
		File csvFile = new File(System.getProperty("java.io.tmpdir"));
		csvUpdateService.process(csvFile);
	}

	@Test
	public void processCsvWithHeader() throws Exception {
		process("input-with-header.csv", "input-with-header-out.csv");
	}

	@Test
	public void processCsvWithoutHeader() throws Exception {
		process("input-no-header.csv", "input-no-header-out.csv");
	}

	@Test
	public void processHugeCsv() throws Exception {
		process("input-huge.csv", "input-huge-out.csv");
	}

	private void process(String inputFileName, String outputFileName) throws Exception {
		File inputFile = new File(getClass().getClassLoader().getResource(inputFileName).toURI());
		csvUpdateService.process(inputFile);

		File outputFile = new File(getClass().getClassLoader().getResource(outputFileName).toURI());
		Assert.assertTrue(outputFile.exists());
		Assert.assertTrue(outputFile.length() > 0);
		Assert.assertTrue(outputFile.length() > inputFile.length());

		int linesInput = countLines(inputFile);
		int linesOutput = countLines(outputFile);
		Assert.assertEquals(linesInput, linesOutput);
	}

	private static int countLines(File file) throws IOException {
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new FileReader(file));
			while ((reader.readLine()) != null)
				;
			return reader.getLineNumber();
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
}