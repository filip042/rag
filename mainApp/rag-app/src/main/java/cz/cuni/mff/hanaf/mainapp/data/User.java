package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.util.Objects;
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
     * Checks equality by identity first, then by id if both objects are {@link User} instances.
     *
     * @param o the object to compare against
     * @return {@code true} if the two objects are the same instance or share the same id;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    /**
     * Returns a hash code derived from id if set, or the identity hash code otherwise.
     *
     * @return the hash code of this user
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }

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
