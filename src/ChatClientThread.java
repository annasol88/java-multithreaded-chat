import java.io.*;
import java.net.Socket;
import java.util.List;

public class ChatClientThread implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    // User currently logged into the client
    private User user;

    public ChatClientThread(Socket socket) {
        try {
            this.socket = socket;
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            //TO REMOVE anna - for testing with a logged in user
            //user = ServerData.accounts.get(0);
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
                    case REGISTER_USER: registerUser();
                    case GET_CHATS: getChatRoomNames();
                    case STOP: stop();
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
            System.err.println("Failed to close the input or output stream.");
            e.printStackTrace();
        }
    }

    private void loginUser(String username, String password) {
        //example server request
        user = ChatServer.loginUser(username, password);
    }

    private void registerUser() throws IOException {
        ChatServer.registerUser();
    }


    private void getChatRoomNames() {
        List<ChatRoom> chats = ChatServer.getUserChatRooms(user);
        StringBuilder chatsString = new StringBuilder();
        for(ChatRoom chat: chats) {
            chatsString.append(chat.getName()).append(",");
        }
        output.println(chatsString);
    }
}
