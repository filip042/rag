package cz.cuni.mff.hanaf.mainapp.web.dto;

/**
 * Response payload confirming a project's new archive state.
 */
public record ArchiveResponse(boolean isArchived) {}