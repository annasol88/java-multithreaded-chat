import java.io.*;
import java.net.Socket;
import java.util.List;

/*
 * Thread dedicated to listening to user commands from the console and responding with data from the server.
 */
public class ChatClientThread implements Runnable {
    private Socket socket;
    private ChatServer server;
    private BufferedReader input;
    private PrintWriter output;
    // User currently logged into the client
    private User user;

    public ChatClientThread(Socket socket, ChatServer server) {
        try {
            this.socket = socket;
            this.server = server;
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            //TO REMOVE anna - for testing with a logged in user
            //user = ServerData.accounts.get("anna123");
        }
        catch(IOException e) {
            System.err.println("Failed to connect to socket input and output stream.");
            stop();
        }
    }

    @Override
    public void run() {
        try {
            ServerRequest request = null;
            while (request != ServerRequest.STOP) {
                request = ServerRequest.valueOf(input.readLine());
                switch(request) {
                    case REGISTER_USER:
                        registerUser();
                        break;
                    case GET_CHATS:
                        getChatRoomNames();
                        break;
                    case OPEN_CHAT_ROOM:
                        openChatRoom();
                        break;
                    case STOP:
                        stop();
                        break;
                }
            }
        } catch(IOException e) {
            System.out.println("client disconnected: " + socket.getPort());
        } finally {
            stop();
        }
    }

    public synchronized void stop() {
        try {
            output.close();
            input.close();
        }
        catch(IOException e) {
            System.err.println("Failed to close the thread input or output stream.");
            e.printStackTrace();
        }
    }

    private void loginUser(String username, String password) {
        //example server request
        user = server.loginUser(username, password);
    }

    private void registerUser() throws IOException {
        server.registerUser();
    }


    private void getChatRoomNames() {
        List<ChatRoom> chats = server.getUserChatRooms(user);
        StringBuilder chatsString = new StringBuilder();
        for(ChatRoom chat: chats) {
            chatsString.append(chat.getName()).append(",");
        }
        output.println(chatsString);
    }

    private void openChatRoom() throws IOException {
        String name = input.readLine();
        ChatRoom roomToOpen = server.getChatRoomByName(name);
        new ChatRoomThread(socket, roomToOpen, user);
    }
}
