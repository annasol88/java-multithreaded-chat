import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatRoomWindow extends JFrame {
    private User currentUser;
    private BufferedReader input;
    private PrintWriter output;

    private JTextField textField;
    private JTextArea chatArea;

    public ChatRoomWindow(String name, Socket socket, User currentUser) throws IOException{
        this.currentUser = currentUser;
        output = new PrintWriter(socket.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(name);
        setLayout(new BorderLayout());
        setLocation(200, 100);
        setPreferredSize(new Dimension(400, 500));

        //draw components
        createChatBox();
        createMessageField();

        pack();
        setVisible(true);
    }

    public void sendMessage() {
        String message = textField.getText();

        printMessage("You: " + message);
        output.println(formatMessageToSend(message));
    }

    public void printMessage(String message) {
        chatArea.append(message + "\n");
    }

    private void createChatBox() {
        JScrollPane jScrollPane = new javax.swing.JScrollPane();
        chatArea = new JTextArea();
        chatArea.setColumns(5);
        chatArea.setLineWrap(true);
        chatArea.setRows(5);
        chatArea.setMinimumSize(new Dimension(300, 400));
        chatArea.setBackground(Color.LIGHT_GRAY);
        jScrollPane.setViewportView(chatArea);

        add(chatArea, BorderLayout.CENTER);
    }

    private void createMessageField() {
        JButton button = new JButton("send");
        textField = new JTextField("", 12);
        JPanel p = new JPanel();

        button.addActionListener(new Listener());

        p.add(textField);
        p.add(button);
        add(p, BorderLayout.SOUTH);
    }

    private String formatMessageToSend(String message) {
        return currentUser.getName() + ": " + message;
    }

    class Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            sendMessage();
        }
    }
}
