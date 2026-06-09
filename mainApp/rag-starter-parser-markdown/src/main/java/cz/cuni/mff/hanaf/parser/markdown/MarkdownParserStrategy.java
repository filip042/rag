package cz.cuni.mff.hanaf.parser.markdown;

import cz.cuni.mff.hanaf.core.parser.DocumentParserStrategy;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link DocumentParserStrategy} implementation for Markdown files.
 */
@Component
public class MarkdownParserStrategy implements DocumentParserStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String filePath) {
        return filePath.endsWith(".md");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Document> read(String filePath) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .build();
        return new MarkdownDocumentReader("file:" + filePath, config).get();
    }
}
