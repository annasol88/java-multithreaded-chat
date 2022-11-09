import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {

    private String name;
    private ArrayList<Message> messages;
    private ConcurrentHashMap<String, User> members;

    public ChatRoom(String name, ConcurrentHashMap<String, User> members) {
        this.name = name;
        this.members = members;
        messages = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public ConcurrentHashMap<String, User> getMembers() {
        return members;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addMember(User member) {
        this.members.put(member.getUsername(), member);
    }

}
