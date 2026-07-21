package cz.cuni.mff.hanaf.mainapp.web.dto;

/**
 * Response payload confirming a project's new visibility state.
 *
 * @param isPublic {@code true} if the project is publicly visible, and {@code false} otherwise
 */
public record VisibilityResponse(boolean isPublic) {}