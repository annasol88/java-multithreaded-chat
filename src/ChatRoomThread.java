import java.net.Socket;
import java.util.ArrayList;

public class ChatRoomThread implements Runnable {

    public static ArrayList<ChatRoomThread> runningChatRooms = new ArrayList<>();
    private ChatRoom chatRoom;
    private Socket socket;

    public ChatRoomThread(Socket socket, ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        this.socket = socket;
    }

    @Override
    public void run() {
        new ChatRoomWindow(chatRoom);
    }
}
