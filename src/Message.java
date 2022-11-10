import java.sql.Timestamp;

public class Message {
    private final String text;
    private final User sender;
    private final Timestamp timestamp;

    public Message(String text, User sender, Timestamp timestamp) {
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public User getSender() {
        return sender;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
