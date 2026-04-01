package cz.cuni.mff.hanaf.core.parser;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DocumentParserStrategy {
    boolean supports(String filePath);
    List<Document> read(String filePath);
}