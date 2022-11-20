import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {

    private String name;
    private ConcurrentHashMap<String, User> members;

    public ChatRoom(String name, ConcurrentHashMap<String, User> members) {
        this.name = name;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public ConcurrentHashMap<String, User> getMembers() {
        return members;
    }
}
