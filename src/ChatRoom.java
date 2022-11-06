import java.util.ArrayList;

public class ChatRoom {

    private String name;
    private ArrayList<Message> messages;
    private ArrayList<User> members;

    public ChatRoom(String name, ArrayList<User> members) {
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

    public ArrayList<User> getMembers() {
        return members;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addMember(User member) {
        this.members.add(member);
    }

}
