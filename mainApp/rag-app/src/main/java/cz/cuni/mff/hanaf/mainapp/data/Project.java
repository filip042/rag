package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * JPA entity representing a project that groups indexed documents and manages user access.
 * Projects are either public or private, and may be archived.
 */
@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private boolean isPublic;
    private boolean isArchived;

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

    /**
     * No-arg constructor required by JPA.
     * Initializes files, accessibleUsers, and adminUsers to empty collections.
     */
    public Project() {
        this.files = new HashMap<>();
        this.accessibleUsers = new HashSet<>();
        this.adminUsers = new HashSet<>();
        this.isPublic = false;
        this.isArchived = false;
    }

    /**
     * Checks equality by identity first, then by id if both objects are {@link Project} instances.
     *
     * @param o the object to compare against
     * @return {@code true} if the two objects are the same instance or share the same id;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project project)) return false;
        return Objects.equals(id, project.id);
    }

    /**
     * Returns a hash code derived from id if set, or the identity hash code otherwise.
     *
     * @return the hash code of this project
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }

    /**
     * Returns the unique identifier of this project, or {@code null} if not yet persisted.
     *
     * @return the project id
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the name of this project.
     *
     * @return the project name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this project.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether this project is publicly visible.
     *
     * @return {@code true} if the project is public; {@code false} otherwise
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Sets whether this project is publicly visible.
     *
     * @param isPublic {@code true} to make the project public; {@code false} to make it private
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Returns whether this project is archived.
     *
     * @return {@code true} if the project is archived; {@code false} otherwise
     */
    public boolean isArchived() {
        return isArchived;
    }

    /**
     * Sets whether this project is archived.
     *
     * @param isArchived {@code true} to archive the project; {@code false} to unarchive it
     */
    public void setArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    /**
     * Returns the map of indexed files in this project, keyed by file name.
     *
     * @return a map from file name to {@link FileInfo}
     */
    public Map<String, FileInfo> getFiles() {
        return files;
    }

    /**
     * Replaces the map of indexed files in this project.
     *
     * @param files a map from file name to {@link FileInfo}
     */
    public void setFiles(Map<String, FileInfo> files) {
        this.files = files;
    }

    /**
     * Adds or replaces the {@link FileInfo} entry for the given file name.
     *
     * @param fileName the name of the file to add
     * @param fileInfo the metadata to associate with the file
     */
    public void addFile(String fileName, FileInfo fileInfo) {
        this.files.put(fileName, fileInfo);
    }

    /**
     * Removes the file with the given name from this project, if present.
     *
     * @param fileName the name of the file to remove
     */
    public void removeFile(String fileName) {
        this.files.remove(fileName);
    }

    /**
     * Returns the set of users with read access to this project.
     *
     * @return the set of accessible users
     */
    public Set<User> getAccessibleUsers() {
        return accessibleUsers;
    }

    /**
     * Replaces the set of users with read access to this project.
     *
     * @param accessibleUsers the new set of users
     */
    public void setAccessibleUsers(Set<User> accessibleUsers) {
        this.accessibleUsers = accessibleUsers;
    }

    /**
     * Grants read access to this project for the given user.
     *
     * @param user the user to grant access to
     */
    public void addAccessibleUser(User user) {
        this.accessibleUsers.add(user);
    }

    /**
     * Revokes read access to this project from the given user.
     *
     * @param user the user to revoke access from
     */
    public void removeAccessibleUser(User user) {
        this.accessibleUsers.remove(user);
    }

    /**
     * Returns the set of users with admin access to this project.
     *
     * @return the set of admin users
     */
    public Set<User> getAdminUsers() {
        return adminUsers;
    }

    /**
     * Replaces the set of users with admin access to this project.
     *
     * @param adminUsers the new set of admin users
     */
    public void setAdminUsers(Set<User> adminUsers) {
        this.adminUsers = adminUsers;
    }

    /**
     * Grants admin access to this project for the given user.
     *
     * @param user the user to grant admin access to
     */
    public void addAdminUser(User user) {
        this.adminUsers.add(user);
    }

    /**
     * Revokes admin access to this project from the given user.
     *
     * @param user the user to revoke admin access from
     */
    public void removeAdminUser(User user) {
        this.adminUsers.remove(user);
    }
}
