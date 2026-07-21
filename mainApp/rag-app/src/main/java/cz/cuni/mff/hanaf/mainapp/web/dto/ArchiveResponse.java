package cz.cuni.mff.hanaf.mainapp.web.dto;

/**
 * Response payload confirming a project's new archive state.
 *
 * @param isArchived {@code true} if the project has been archived, and {@code false} otherwise
 */
public record ArchiveResponse(boolean isArchived) {}