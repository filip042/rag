package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "project_user_access",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> accessibleUsers;

    public Project() {
        this.accessibleUsers = new HashSet<User>();
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
