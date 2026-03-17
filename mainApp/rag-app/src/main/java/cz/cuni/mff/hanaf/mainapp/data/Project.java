package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;


@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean isPublic;

//    private boolean isArchived;
//
//    private boolean isTemporary;
//
//    private LocalDateTime expiresAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_files", joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyColumn(name = "file_name")
    private Map<String, FileInfo> files;

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
        this.files = new HashMap<>();
        this.accessibleUsers = new HashSet<>();
        this.adminUsers = new HashSet<>();
        this.isPublic = false;
    }

    /**
     * Checks the two objects for equality using is, id possible
     * @param o the object being checked for equality
     * @return true if the two objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Project project)) {
            return false;
        }
        return Objects.equals(id, project.id);
    }

    /**
     * Returns the hash code of the Project, computed from id if set
     *
     * @return The hash code of the Project
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : System.identityHashCode(this);
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
     * Checks if the project is public
     *
     * @return true if the project is public, false otherwise
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Sets whether the project is public
     *
     * @param isPublic Whether the project is public
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Get the files in the project and their info
     *
     * @return a map of the files in the project and their info
     */
    public Map<String, FileInfo> getFiles() {
        return files;
    }

    /**
     * Set the files in the project and their info
     *
     * @param files a map of files and their info
     */
    public void setFiles(Map<String, FileInfo> files) {
        this.files = files;
    }

    /**
     * Add a file and its info to the project
     *
     * @param fileName the file's name
     * @param fileInfo the file's info
     */
    public void addFile(String fileName, FileInfo fileInfo) {
        this.files.put(fileName, fileInfo);
    }

    /**
     * Remove the given file and its info
     *
     * @param fileName the file's name
     */
    public void removeFile(String fileName) {
        this.files.remove(fileName);
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
