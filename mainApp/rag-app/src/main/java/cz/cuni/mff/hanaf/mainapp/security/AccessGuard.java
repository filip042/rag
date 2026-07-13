package cz.cuni.mff.hanaf.mainapp.security;

import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * Provides methods to manage access control within the application.
 * It verifies user authentication, session state, and permissions for accessing specific projects.
 */
@Component
public class AccessGuard {

    /**
     * Returns the current authenticated user from the HTTP session
     *
     * @param session the current HTTP session, expected to contain the authenticated user
     * @return the logged-in user, or {@code null} if none is present in the session
     */
    public User currentUser(HttpSession session) {
        return (User) session.getAttribute("authenticatedUser");
    }

    /**
     * Checks whether the admin attribute is set in the current HTTP session
     *
     * @param session the current HTTP session, expected to contain the admin flag
     * @return {@code true} if the admin flag is set, {@code false} otherwise
     */
    public boolean isAdminOfSessionProject(HttpSession session) {
        Boolean admin = (Boolean) session.getAttribute("admin");
        return admin != null && admin;
    }

    /**
     * Checks whether the given user has access to the given project,
     * independent of whatever is cached in the session
     *
     * @param user the user to check the project for access
     * @param project the project to check for user access
     * @return {@code true} if the user has access to the project, {@code false} otherwise
     */
    public boolean hasAccess(User user, Project project) {
        if (project.isPublic()) {
            return true;
        }
        if (user == null || !user.isRegistered()) {
            return false;
        }
        return project.getAdminUsers().contains(user) || project.getAccessibleUsers().contains(user);
    }

    /**
     * Checks whether the given user is an admin in the given project
     *
     * @param user the user to check the project for admin access
     * @param project the project to check for admin access
     * @return {@code true} if the user is an admin in the project, {@code false} otherwise
     */
    public boolean isAdmin(User user, Project project) {
        return user != null && user.isRegistered() && project.getAdminUsers().contains(user);
    }
}