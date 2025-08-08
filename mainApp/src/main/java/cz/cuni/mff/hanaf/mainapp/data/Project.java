package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String password;

    public Project() {

    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return this.name;
    }

    public void setUsername(String username) {
        this.name = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
