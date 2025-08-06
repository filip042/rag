package cz.cuni.mff.hanaf.mainapp;

public class User { // todo temp for testing thymeleaf, will add database soon
    public String username;

    User() {
    }

    User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
