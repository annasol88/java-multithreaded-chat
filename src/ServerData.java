import java.util.ArrayList;

/*
 * Serves as a DB for our server
 */
public class ServerData {
    public ArrayList<ChatRoom> chatRooms;
    public ArrayList<User> accounts;
    public ArrayList<User> admins;

    public ServerData() {
        chatRooms = new ArrayList<>();
        accounts = new ArrayList<>();
        admins = new ArrayList<>();

        accounts.add(new User("anna", "software developer", "anna123", "123", new ArrayList<>()));
        accounts.add(new User("emma", "software developer", "emma123", "123", new ArrayList<>()));
        accounts.add(new User("alex", "software developer", "alex123", "123", new ArrayList<>()));
    }
}
