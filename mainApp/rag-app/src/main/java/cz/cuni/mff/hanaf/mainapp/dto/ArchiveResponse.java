package cz.cuni.mff.hanaf.mainapp.dto;

/**
 * Response payload confirming a project's new archive state.
 */
public record ArchiveResponse(boolean isArchived) {}