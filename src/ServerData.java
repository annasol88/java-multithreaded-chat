import java.util.ArrayList;

/*
 * Serves as a DB for our server
 * Giving a nice set of mock data to work with upon initial load
 */
public class ServerData {
    public static ArrayList<ChatRoom> chatRooms;
    public static ArrayList<User> accounts;
    public static ArrayList<User> admins;

    public ServerData() {
        chatRooms = new ArrayList<>();
        accounts = new ArrayList<>();
        admins = new ArrayList<>();

        User anna = new User("anna", "software developer", "anna123", "123", new ArrayList<>());
        User emma = new User("emma", "software developer", "emma123", "123", new ArrayList<>());
        User alex = new User("alex", "software developer", "alex123", "123", new ArrayList<>());

        accounts.add(anna);
        accounts.add(emma);
        accounts.add(alex);

        chatRooms.add(new ChatRoom("chat 1", accounts));

        ArrayList<User> chat2 = new ArrayList<>();
        chat2.add(anna);
        chat2.add(alex);
        chatRooms.add(new ChatRoom("chat 2", chat2));
    }
}
