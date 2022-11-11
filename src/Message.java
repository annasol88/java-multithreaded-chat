import java.sql.Timestamp;

public class Message {
    private final String text;
    private final User sender;

    public Message(String text, User sender) {
        this.text = text;
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public User getSender() {
        return sender;
    }
}
