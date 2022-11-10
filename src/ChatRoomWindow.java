import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 *
 */
public class ChatRoomWindow extends JFrame implements Runnable{
    private ChatRoom chatroom;
    private User currentUser;
    private BufferedReader input;
    private PrintWriter output;

    private JTextField textField;
    private JTextArea chatArea;

    public ChatRoomWindow(Socket socket, ChatRoom chatRoom, User currentUser) throws IOException{
        this.chatroom = chatRoom;
        this.currentUser = currentUser;
        output = new PrintWriter(socket.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(chatRoom.getName());
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
        Message message = new Message(textField.getText(), currentUser,null);
        String messageString = messageFormat(message);

        printMessage(messageString);
        output.println(messageString);
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

        for(Message message : chatroom.getMessages()){
            chatArea.append(messageFormat(message));
        }

        add(chatArea, BorderLayout.CENTER);
    }

    private void createMessageField() {
        JButton button = new JButton("send");
        textField = new JTextField("Type a message...", 16);
        JPanel p = new JPanel();

        button.addActionListener(new Listener());

        p.add(textField);
        p.add(button);
        add(p, BorderLayout.SOUTH);
    }

    private String messageFormat(Message message) {
        if(message.getSender().equals(this.currentUser)) {
            return "you: " + message.getText();
        }
        return message.getSender().getName() + ": " + message.getText();
    }

    @Override
    public void run() {
        //TODO change to while thread open
        while(true) {
            try {
                printMessage(input.readLine().trim());
            }
            catch (IOException e) {
                //handle this
            }
        }
    }

    class Listener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
                sendMessage();
        }
    }
}
