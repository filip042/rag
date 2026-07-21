package cz.cuni.mff.hanaf.mainapp.rag.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload for asking a question against a specific project.
 *
 * @param query the query to be answered
 * @param project the identifier of the project being queried
 */
public record AskRequest(
        @Schema(example = "What is the answer to life, the universe, and everything?")
        String query,

        @Schema(example = "1")
        long project
) {}
