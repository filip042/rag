package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Instant lastIndexedTime;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> files;

    @ManyToMany
    @JoinTable(
            name = "project_user_access",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> accessibleUsers;

    @ManyToMany
    @JoinTable(
            name = "project_user_admin",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> adminUsers;

    public Project() {
        this.files = new HashSet<>();
        this.accessibleUsers = new HashSet<>();
        this.adminUsers = new HashSet<>();
    }

    /**
     * Get the id of the project
     *
     * @return The id of the project
     */
    public Long getId() {
        return id;
    }

    /**
     * Get the name of the project
     *
     * @return The project's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name of the project to the given name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the last time this project was indexed
     *
     * @return The last indexed timestamp, or null if never indexed
     */
    public Instant getLastIndexedTime() {
        return lastIndexedTime;
    }

    /**
     * Set the last indexed time
     *
     * @param lastIndexedTime The time to set
     */
    public void setLastIndexedTime(Instant lastIndexedTime) {
        this.lastIndexedTime = lastIndexedTime;
    }

    /**
     * Returns the current set of filenames
     *
     * @return The current set of filenames
     */
    public Set<String> getFiles() {
        return this.files;
    }

    /**
     * Replaces the current set of filenames with the given one
     *
     * @param files The new set of filenames
     */
    public void setFiles(Set<String> files) {
        this.files = files;
    }

    /**
     * Adds the given filenames to the project
     *
     * @param files The collection of filenames to add
     */
    public void addFiles(Collection<String> files) {
        this.files.addAll(files);
    }

    /**
     * Removes the given file from the project
     *
     * @param file The filename of the file to remove
     */
    public void removeFile(String file) {
        this.files.remove(file);
    }

    /**
     * Returns the current set of users with access
     *
     * @return The current set of users with access
     */
    public Set<User> getAccessibleUsers() {
        return accessibleUsers;
    }

    /**
     * Replaces the current set of users with acces with the given set of users
     *
     * @param accessibleUsers The new set of users with access
     */
    public void setAccessibleUsers(Set<User> accessibleUsers) {
        this.accessibleUsers = accessibleUsers;
    }

    /**
     * Adds the given user to the set of users with access
     *
     * @param user The iser to add
     */
    public void addAccessibleUser(User user) {
        this.accessibleUsers.add(user);
    }

    /**
     * Removes the current user from the set of users with access
     *
     * @param user The user to remove
     */
    public void removeAccessibleUser(User user) {
        this.accessibleUsers.remove(user);
    }

    /**
     * Gets the current set of admins
     *
     * @return The current set of admins
     */
    public Set<User> getAdminUsers() {
        return adminUsers;
    }

    /**
     * Replaces the current set of admins with the given set
     *
     * @param adminUsers The new set of admins
     */
    public void setAdminUsers(Set<User> adminUsers) {
        this.adminUsers = adminUsers;
    }

    /**
     * Adds the given user to the set of admins
     *
     * @param user The user to add to admins
     */
    public void addAdminUser(User user) {
        this.adminUsers.add(user);
    }

    /**
     * Removes the given admin
     *
     * @param user The admin to remove
     */
    public void removeAdminUser(User user) {
        this.adminUsers.remove(user);
    }
}
