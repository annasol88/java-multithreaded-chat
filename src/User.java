public class User {
    private final String name;
    private final String bio;
    private final String username;
    private final String password;
    private boolean isLoggedIn;

    public User(String name, String bio, String username, String password) {
        this.name = name;
        this.bio = bio;
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
