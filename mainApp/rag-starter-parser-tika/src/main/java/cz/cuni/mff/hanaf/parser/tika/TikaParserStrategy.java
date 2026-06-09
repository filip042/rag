package cz.cuni.mff.hanaf.parser.tika;

import cz.cuni.mff.hanaf.core.parser.DocumentParserStrategy;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link DocumentParserStrategy} implementation for Apache Tika-supported files.
 */
@Component
public class TikaParserStrategy implements DocumentParserStrategy {
    private final List<String> extensions = List.of(".txt", ".html", ".pdf");

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String filePath) {
        return extensions.stream().anyMatch(filePath::endsWith);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Document> read(String filePath) {
        return new TikaDocumentReader("file:" + filePath).get();
    }
}
