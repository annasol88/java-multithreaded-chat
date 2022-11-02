import javax.swing.*;
import java.awt.*;
import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/*
 * JFrame or create new console
 */
public class ChatUI extends JFrame {

    public ChatUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        setSize(200,200);
        setLocation(100,0);

        JLabel view = new JLabel("Welcome to the chatroom app");
        add(view, BorderLayout.NORTH);

        setVisible(true);
    }

    //example
    public void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public void showLoginRegisterScreen() {
        //TODO
        Console console = System.console();
    }

    public Map<String, String> getLoginDetails() {
        //TODO
        //a map is probs the best way to handle user details
        Map<String, String> login = new HashMap<>();
        login.put("username", "anna123");
        login.put("password", "123");
        return login;
    }

    public void displayChatList(ArrayList<ChatRoom> chats) {
        //TODO
    }
}
