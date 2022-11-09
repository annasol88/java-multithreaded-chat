import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Serves as a DB for our server
 * Giving a nice set of mock data to work with upon initial load
 */
public class ServerData {
    public static ArrayList<ChatRoom> chatRooms;
    public static ConcurrentHashMap<String, User> accounts;
    public static ConcurrentHashMap<String, User> admins;

    public ServerData() {
        chatRooms = new ArrayList<>();
        accounts = new ConcurrentHashMap<>();
        admins = new ConcurrentHashMap<>();

        User anna = new User("anna", "software developer", "anna123", "123", new ArrayList<>());
        User emma = new User("emma", "software developer", "emma123", "123", new ArrayList<>());
        User alex = new User("alex", "software developer", "alex123", "123", new ArrayList<>());

        accounts.put(anna.getUsername(), anna);
        accounts.put(alex.getUsername(), alex);
        accounts.put(emma.getUsername(), emma);

        chatRooms.add(new ChatRoom("chat 1", accounts));

        ConcurrentHashMap<String, User> chat2 = new ConcurrentHashMap<>();
        chat2.put(anna.getUsername(), alex);
        chat2.put(alex.getUsername(), alex);
        chatRooms.add(new ChatRoom("chat 2", chat2));
    }

    public ConcurrentHashMap<String, User> getAccounts() {
        return accounts;
    }

    public void addAccount(User account) {
        accounts.put(account.getUsername(), account);
        System.out.println(accounts);
    }

}
