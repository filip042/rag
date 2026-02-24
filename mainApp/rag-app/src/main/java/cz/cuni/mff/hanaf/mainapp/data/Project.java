package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.util.*;


@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_file_hashes", joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyColumn(name = "file_name")
    @Column(name = "file_hash")
    private Map<String, String> fileHashes;

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
        this.fileHashes = new HashMap<>();
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
     * Get the files in the project and their hashes
     *
     * @return a map of the files in the project and their hashes
     */
    public Map<String, String> getFileHashes() {
        return fileHashes;
    }

    /**
     * Set the files in the project and their hashes
     * @param fileHashes a map of files and their hashes
     */
    public void setFileHashes(Map<String, String> fileHashes) {
        this.fileHashes = fileHashes;
    }

    /**
     * Add a file and its hash to the project
     *
     * @param fileName the file's name
     * @param hash the file's hash
     */
    public void addFileHash(String fileName, String hash) {
        this.fileHashes.put(fileName, hash);
    }

    /**
     * Remove the given file and its hash
     *
     * @param fileName
     */
    public void removeFileHash(String fileName) {
        this.fileHashes.remove(fileName);
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
