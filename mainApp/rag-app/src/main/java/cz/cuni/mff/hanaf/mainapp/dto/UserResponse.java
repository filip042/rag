package cz.cuni.mff.hanaf.mainapp.dto;

/**
 * Represents a user's details and their administrative status within a specific project.
 */
public record UserResponse(
        Long id,
        String username,
        Boolean admin
) {}