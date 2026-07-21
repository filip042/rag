package cz.cuni.mff.hanaf.mainapp.rag.dto;

/**
 * Payload for deleting a project.
 *
 * @param project the identifier of the project to be deleted
 */
public record DeleteProjectRequest(long project) {}