package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> files;

    @ManyToMany
    @JoinTable(
            name = "project_user_access",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> accessibleUsers;

    public Project() {
        this.files = new HashSet<>();
        this.accessibleUsers = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getFiles() {
        return this.files;
    }

    public void setFiles(Set<String> files) {
        this.files = files;
    }

    public void addFiles(Collection<String> files) {
        this.files.addAll(files);
    }

    public void removeFile(String file) {
        this.files.remove(file);
    }

    public Set<User> getAccessibleUsers() {
        return accessibleUsers;
    }

    public void setAccessibleUsers(Set<User> accessibleUsers) {
        this.accessibleUsers = accessibleUsers;
    }

    public void addAccessibleUser(User user) {
        this.accessibleUsers.add(user);
    }

    public void removeAccessibleUser(User user) {
        this.accessibleUsers.remove(user);
    }
}
