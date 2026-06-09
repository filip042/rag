package cz.cuni.mff.hanaf.mainapp.rag.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload for polling the status of an active LLM generation task.
 */
public record AnswerRequest(
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        String taskId
) {}