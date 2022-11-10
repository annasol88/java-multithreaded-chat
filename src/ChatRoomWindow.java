import com.sun.source.tree.ReturnTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatRoomWindow extends JFrame {
    private ChatRoom chatroom;
    private User currentUser;

    public ChatRoomWindow(ChatRoom chatRoom, User currentUser) {
        this.chatroom = chatRoom;
        this.currentUser = currentUser;

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

    }

    public void receiveMessage() {}

    private void createChatBox() {
        JScrollPane jScrollPane = new javax.swing.JScrollPane();
        JTextArea chatArea = new JTextArea();
        chatArea.setColumns(5);
        chatArea.setLineWrap(true);
        chatArea.setRows(5);
        chatArea.setMinimumSize(new Dimension(300, 400));
        jScrollPane.setViewportView(chatArea);

        for(Message message : chatroom.getMessages()){
            chatArea.append(messageFormat(message));
        }

        add(chatArea, BorderLayout.CENTER);
    }

    private void createMessageField() {
        JButton b = new JButton("send");
        JTextField t = new JTextField("Type a message...", 16);
        JPanel p = new JPanel();

        b.addActionListener(new Listener());

        p.add(t);
        p.add(b);
        add(p, BorderLayout.SOUTH);
    }

    private String messageFormat(Message message) {
        if(message.getSender().equals(this.currentUser)) {
            return "you: " + message.getText();
        }
        return message.getSender().getName() + ": " + message.getText();
    }

    class Listener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String s = e.getActionCommand();
            if(s.equals("send")) {
                sendMessage();
            }
        }
    }
}
