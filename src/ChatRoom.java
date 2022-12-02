import java.util.ArrayList;
import java.util.Collection;
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

    public Collection<User> getMembers() {
        return members.values();
    }

    public void removeMember(String username) {
        members.remove(username);
    }
}
