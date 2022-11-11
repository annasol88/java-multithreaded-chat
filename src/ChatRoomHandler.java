import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ChatRoomHandler implements Runnable {

    public ArrayList<ChatRoomHandler> runningChatThreads;
    private ChatRoom chatRoom;
    private User user;
    private ChatRoomWindow window;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public ChatRoomHandler(Socket socket, ChatRoom chatRoom, User user, ArrayList<ChatRoomHandler> runningChats) {
        try {
            this.runningChatThreads = runningChats;
            this.chatRoom = chatRoom;
            this.user = user;
            this.socket = socket;

            this.runningChatThreads.add(this);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException e) {
            System.err.println("Could not connect to server. Please ensure The server is running.");
            stop();
        }
    }

    public ChatRoom getChatRoom() { return chatRoom; }

    /*
     * Thread dedicated to listening to messages from the chat room window
     */
    @Override
    public void run() {
        try {

            while(true) {
                String message = input.readLine().trim();
                sendToAll(message);
            }
        } catch(IOException e) {
            System.out.println("chat room disconnected: " + socket.getPort());
        } finally {
            stop();
        }
    }

    private void sendToAll(String message) {
        for(ChatRoomHandler runningChat : runningChatThreads) {
            //send message to all running chats for the same chat room that isn't itself
            if(!runningChat.equals(this) && runningChat.getChatRoom().equals(chatRoom)) {
                //server.saveMessage(chatRoom, message);
                runningChat.output.println(message);
            }
        }
    }

    private void stop() {
        try {
            runningChatThreads.remove(this);
            output.close();
            input.close();
            socket.close();
        }
        catch(IOException e) {
            System.err.println("Could not close input or output stream.");
            e.printStackTrace();
        }
    }
}
