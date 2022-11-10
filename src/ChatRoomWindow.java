import javax.swing.*;
import java.awt.*;

public class ChatRoomWindow extends JFrame {
    public ChatRoomWindow(ChatRoom chatRoom) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(chatRoom.getName());
        setLayout(new BorderLayout());
        setLocation(200, 100);
        setPreferredSize(new Dimension(300, 400));

        pack();
        setVisible(true);
    }

}
