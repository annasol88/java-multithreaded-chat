import java.util.concurrent.ConcurrentHashMap;

/**
 * Serves as a DB for our server
 * Currently pre-populated with a set of mock data to work with
 */
public class ServerData {
    public static ConcurrentHashMap<String, ChatRoom> chatRooms;
    public static ConcurrentHashMap<String, User> accounts;
    public static ConcurrentHashMap<String, User> admins;

    public ServerData() {
        chatRooms = new ConcurrentHashMap<>();
        accounts = new ConcurrentHashMap<>();
        admins = new ConcurrentHashMap<>();

        User anna = new User("anna", "software developer", "anna123", "123");
        User emma = new User("emma", "software developer", "emma123", "123");
        User alex = new User("alex", "software developer", "alex123", "123");

        anna.addFriend(alex);
        alex.addFriend(anna);

        accounts.put(anna.getUsername(), anna);
        accounts.put(alex.getUsername(), alex);
        accounts.put(emma.getUsername(), emma);

        ConcurrentHashMap<String, User> chat1Members = new ConcurrentHashMap<>();
        chat1Members.put(anna.getUsername(), anna);
        chat1Members.put(alex.getUsername(), alex);
        chat1Members.put(alex.getUsername(), emma);

        chatRooms.put("chat x", new ChatRoom("chat x", chat1Members));

        ConcurrentHashMap<String, User> chat2Members = new ConcurrentHashMap<>();
        chat2Members.put(anna.getUsername(), anna);
        chat2Members.put(alex.getUsername(), alex);

        chatRooms.put("chat y", new ChatRoom("chat y", chat2Members));
    }
}
