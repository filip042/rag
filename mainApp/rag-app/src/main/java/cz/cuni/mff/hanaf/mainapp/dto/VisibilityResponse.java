package cz.cuni.mff.hanaf.mainapp.dto;

/**
 * Response payload confirming a project's new visibility state.
 */
public record VisibilityResponse(boolean isPublic) {}