package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.util.Set;

/**
 * JPA entity representing an application user.
 * Mapped to the "app_user" table to avoid conflicts with reserved SQL keywords.
 */
@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @ManyToMany(mappedBy = "accessibleUsers")
    private Set<Project> accessibleProjects;

    @ManyToMany(mappedBy = "adminUsers")
    private Set<Project> adminProjects;

    /**
     * No-arg constructor required by JPA.
     */
    public User() {}

    /**
     * Returns the unique identifier of this user, or {@code null} if not yet persisted.
     *
     * @return the user id
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the user's password. Holds the plaintext password before persistence
     * and the encoded password after.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the set of projects the user has standard access to.
     *
     * @return the set of projects the user has standard access to
     */
    public Set<Project> getAccessibleProjects() {
        return accessibleProjects;
    }

    /**
     * Returns the set of projects the user has admin access to.
     *
     * @return the set of projects the user has admin access to
     */
    public Set<Project> getAdminProjects() {
        return adminProjects;
    }

    /**
     * Returns whether the given user is registered, i.e., not a guest
     *
     * @return {@code true} if the user is registered, {@code false} otherwise
     */
    public boolean isRegistered() {
        return id != null;
    }
}
