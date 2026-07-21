package cz.cuni.mff.hanaf.mainapp.rag.dto;

/**
 * Response payload containing the async task tracking ID.
 *
 * @param taskId the identifier of the task for future polling
 */
public record AskResponse(String taskId) {}