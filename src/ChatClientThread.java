import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/*
 * Thread dedicated to listening to user commands from the console and responding with data from the server.
 */
public class ChatClientThread implements Runnable {
    private Socket socket;
    private ChatServer server;
    BufferedReader input;
    PrintWriter output;

    // User currently logged into the client
    private User user;
    String openChatRoom;

    public ChatClientThread(Socket socket, ChatServer server) {
        try {
            this.socket = socket;
            this.server = server;
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            //TO REMOVE anna - for testing with a logged in user
            user = ServerData.accounts.get("anna123");
        }
        catch(IOException e) {
            System.err.println("Failed to connect to socket input and output stream.");
            stop();
        }
    }

    @Override
    public void run() {
        try {
            String request = "";
            while (!request.equals("stop")) {
                request = input.readLine();
                if(request.startsWith("view chat list")) {
                    getChatRoomNames();
                }
                else if (request.startsWith("enter chat room")) {
                    enterChatRoom(request);
                }
                else if(request.startsWith("send message")) {
                    sendMessageToChat(request);
                }
                else if(request.startsWith("exit chat room")) {
                    exitChatRoom();
                }
                else if(request.startsWith("view friends list")) {
                    getFriendList();
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

    private void getChatRoomNames() {
        List<ChatRoom> chats = server.getUserChatRooms(user);
        StringBuilder chatsString = new StringBuilder();
        for (ChatRoom chat : chats) {
            chatsString.append(chat.getName()).append(",");
        }
        output.println("show chat list:" + chatsString);
    }

    private void enterChatRoom(String request) {
        String chatRoomName = request.replace("enter chat room:", "").trim();
        if(server.userExistsInChatRoom(chatRoomName, user)) {
            openChatRoom = chatRoomName;
            server.addToRunningChats(this);
            output.println("run chat room");
        }
        else {
            output.println("chat room invalid");
        }
    }

    private void sendMessageToChat(String request) {
        String message = request.replace("send message:", "").trim();
        if(!message.equals("")) {
            message = user.getName() + ": " + message;
            server.sendToChatRoom(message, openChatRoom, this);
        }
    }

    private void getFriendList() {
        Collection<User> friends = user.getFriends();
        StringBuilder friendsString = new StringBuilder();
        for (User friend : friends) {
            friendsString.append(friend.getName()).append(",");
        }
        output.println("show friends list:" + friendsString);
    }

    private void exitChatRoom() {
        openChatRoom = null;
        server.removeRunningChats(this);
    }
}
