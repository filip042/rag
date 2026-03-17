package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "app_user")
public class User { // todo temp for testing thymeleaf, will add database soon
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @ManyToMany(mappedBy = "accessibleUsers")
    private Set<Project> accessibleProjects;

    @ManyToMany(mappedBy = "adminUsers")
    private Set<Project> adminProjects;

    public User() {

    }

    /**
     * Returns the user's id number
     *
     * @return the user's id
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the user's username
     *
     * @return The user's username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Replaces the current username with the given one
     *
     * @param username The new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user's password
     *
     * @return The user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password
     *
     * @param password The new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the set of projects the user has standard access to
     *
     * @return The set of projects the user has standard access to
     */
    public Set<Project> getAccessibleProjects() {
        return accessibleProjects;
    }

    /**
     * Gets the set of projects the user has admin access to
     *
     * @return The set of projects the user has admin access to
     */
    public Set<Project> getAdminProjects() {
        return adminProjects;
    }

    /**
     * Checks whether the given user is a guest
     *
     * @return true if the user is a guest, false otherwise
     */
    public boolean isGuest() {
        return id == null;
    }
}
