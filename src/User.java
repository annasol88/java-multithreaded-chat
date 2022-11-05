import java.util.ArrayList;

public class User {
    private final String name;
    private final String bio;
    private final String username;
    private final String password;
    private boolean isLoggedIn;

    private ArrayList<User> friends;

    public User(String name, String bio, String username, String password, ArrayList<User> friends) {
        this.name = name;
        this.bio = bio;
        this.username = username;
        this.password = password;
        this.friends = friends;
        this.isLoggedIn = false;
    }

    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
