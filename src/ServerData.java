import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Serves as a DB for our server
 * Giving a nice set of mock data to work with upon initial load
 */
public class ServerData {
    public static ConcurrentHashMap<String, ChatRoom> chatRooms;
    public static ConcurrentHashMap<String, User> accounts;
    public static ConcurrentHashMap<String, User> admins;

    public ServerData() {
        chatRooms = new ConcurrentHashMap<>();
        accounts = new ConcurrentHashMap<>();
        admins = new ConcurrentHashMap<>();

        User anna = new User("anna", "software developer", "anna123", "123", new ArrayList<>());
        User emma = new User("emma", "software developer", "emma123", "123", new ArrayList<>());
        User alex = new User("alex", "software developer", "alex123", "123", new ArrayList<>());

        accounts.put(anna.getUsername(), anna);
        accounts.put(alex.getUsername(), alex);
        accounts.put(emma.getUsername(), emma);

        chatRooms.put("chat x", new ChatRoom("chat x", accounts));

        ConcurrentHashMap<String, User> chat2Members = new ConcurrentHashMap<>();
        chat2Members.put(anna.getUsername(), anna);
        chat2Members.put(alex.getUsername(), alex);

        chatRooms.put("chat y", new ChatRoom("chat y", chat2Members));
    }

    public ConcurrentHashMap<String, User> getAccounts() {
        return accounts;
    }

    public void addAccount(User account) {
        accounts.put(account.getUsername(), account);
        System.out.println(accounts);
    }

}
