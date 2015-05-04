package au.com.bulletproof.maja;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Main class for processing CVS file. It takes reference to input CSV file,
 * read records in it and generate unique ID for every record. Then it writes
 * results into output file which can be found in the same directory as input
 * file. Writing to output file is done using chunking to ensure better
 * performance. Chunk size can be changed in app properties
 * 
 * @author Martin Janousek
 */
@Service
public class CsvUpdateService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Value("${csv.encoding}")
	private String encoding;

	@Value("${output.file.suffix}")
	private String suffix;

	@Value("${chunk.size}")
	private int chunkSize;

	/**
	 * Validates given input file and process CSV update
	 */
	public void process(File inputFile) throws IOException {
		Assert.isTrue(inputFile.exists(), "File '" + inputFile.getAbsolutePath() + "' doesn't exist.");
		Assert.isTrue(inputFile.isFile(), "Path '" + inputFile.getAbsolutePath() + "' doesn't refer to a valid file.");
		log.info("Processing file " + inputFile.getAbsolutePath());

		CSVFormat inFormat = CSVFormat.DEFAULT.withHeader();
		Reader reader = new FileReader(inputFile);
		CSVParser parser = new CSVParser(reader, inFormat);

		CSVFormat outFormat = CSVFormat.DEFAULT.withHeader(getOutputHeader(parser));
		FileWriter writer = new FileWriter(getOutputFileName(inputFile));
		CSVPrinter printer = new CSVPrinter(writer, outFormat);

		try {
			updateIds(parser, printer);
			log.info("File " + inputFile.getAbsolutePath() + " has been successfully processed");
		} finally {
			closeQuietly(writer);
			closeQuietly(printer);
			closeQuietly(reader);
			closeQuietly(parser);
		}
	}

	/**
	 * Iterates through all CSV records, generates unique ID for each record and
	 * print updated record to output writer. It uses chunking to ensure faster
	 * processing. Chunk size can be set in the app properties
	 */
	private void updateIds(CSVParser parser, CSVPrinter printer) throws IOException {
		for (CSVRecord record : parser) {
			printer.printRecord(getValues(record));

			if (parser.getCurrentLineNumber() % chunkSize == 0) {
				printer.flush();
			}
		}
		printer.flush();
	}

	/**
	 * Converts given {@link CSVRecord} into list of values
	 */
	private Iterable<String> getValues(CSVRecord record) {
		List<String> values = new ArrayList<String>();
		Iterator<String> it = record.iterator();
		while (it.hasNext()) {
			values.add(it.next());
		}
		values.add(generateUniqueId());
		return values;
	}

	/**
	 * @return CSV header updated by 'unique-id' column on the last position
	 */
	private String[] getOutputHeader(CSVParser parser) {
		String[] headers = new String[parser.getHeaderMap().size() + 1];
		headers[headers.length - 1] = "unique-id";

		for (Entry<String, Integer> entry : parser.getHeaderMap().entrySet()) {
			headers[entry.getValue()] = entry.getKey();
		}
		return headers;
	}

	/**
	 * @return Output file absolute path generated from input file plus suffix
	 */
	private String getOutputFileName(File inputFile) {
		String absolutePath = inputFile.getAbsolutePath();
		StringBuilder sb = new StringBuilder();
		sb.append(getFullPath(absolutePath));
		sb.append(getBaseName(absolutePath));
		sb.append(suffix);
		sb.append('.');
		sb.append(getExtension(absolutePath));
		return sb.toString();
	}

	/**
	 * @return Unique ID
	 */
	private String generateUniqueId() {
		return UUID.randomUUID().toString();
	}
}