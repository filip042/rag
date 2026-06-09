package cz.cuni.mff.hanaf.mainapp.rag.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload for asking a question against a specific project.
 */
public record AskRequest(
        @Schema(example = "What is the answer to life, the universe, and everything?")
        String query,

        @Schema(example = "1")
        long project
) {}
