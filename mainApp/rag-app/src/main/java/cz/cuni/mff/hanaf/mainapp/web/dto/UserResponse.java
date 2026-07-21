package cz.cuni.mff.hanaf.mainapp.web.dto;

/**
 * Represents a user's details and their administrative status within a specific project.
 *
 * @param id the identifier of the given user
 * @param username the username of the given user
 * @param admin {@code true} if the user has administrator access in the project, and {@code false} otherwise
 */
public record UserResponse(
        Long id,
        String username,
        Boolean admin
) {}