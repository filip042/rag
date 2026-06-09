package cz.cuni.mff.hanaf.mainapp.web.dto;

/**
 * Response payload confirming a project's new visibility state.
 */
public record VisibilityResponse(boolean isPublic) {}