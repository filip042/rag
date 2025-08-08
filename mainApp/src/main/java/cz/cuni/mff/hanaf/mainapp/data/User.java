package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class User { // todo temp for testing thymeleaf, will add database soon
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    public User() {

    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
