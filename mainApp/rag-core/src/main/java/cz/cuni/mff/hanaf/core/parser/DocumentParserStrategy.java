package cz.cuni.mff.hanaf.core.parser;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * Strategy interface for selecting document parsers.
 */
public interface DocumentParserStrategy {

    /**
     * Checks whether the file with the given path can be parsed by the given document parser.
     *
     * @param filePath the path of the file to check for parsing support
     * @return {@code true} if the parser supports the file format, {@code false} otherwise
     */
    boolean supports(String filePath);

    /**
     * Reads and parses the contents of a file located at the specified file path.
     *
     * @param filePath the path of the file to be read and parsed
     * @return a list of {@code Document} objects obtained by parsing the contents of the file
     */
    List<Document> read(String filePath);
}
