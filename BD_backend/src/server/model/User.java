package server.model;

public abstract class User {
    private String id, username;

    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    //getter
    public String getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }

}