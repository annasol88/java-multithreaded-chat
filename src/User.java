import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private final String name;
    private final String bio;
    private final String username;
    private final String password;
    private boolean isLoggedIn;

    private final ConcurrentHashMap<String, User> friends;

    private final ConcurrentHashMap<String, User> pendingFriendRequest;

    public User(String name, String bio, String username, String password) {
        this.name = name;
        this.bio = bio;
        this.username = username;
        this.password = password;
        this.friends = new ConcurrentHashMap<>();
        this.pendingFriendRequest = new ConcurrentHashMap<>();
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

    public Collection<User> getFriends() {
        return friends.values();
    }

    public Collection<User> getPendingFriendRequests() {
        return pendingFriendRequest.values();
    }

    public void addFriend(User user) {
        friends.put(user.getUsername(), user);
    }

    public void addFriendRequest(User user) {
        pendingFriendRequest.put(user.getUsername(), user);
    }

    public void removeFriendRequest(User user) {
        pendingFriendRequest.remove(user.getUsername());
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
