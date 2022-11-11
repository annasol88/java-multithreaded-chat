import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ChatServer implements Runnable {
    protected final String serverIP;
    protected final int port;
    protected ServerSocket serverSocket = null;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private ArrayList<ChatClientThread> runningChats = new ArrayList<>();

    private static ServerData data;

    public ChatServer(int port, String serverIP) {
        this.port = port;
        this.serverIP = serverIP;
        data = new ServerData();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running...");

            while(!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.submit(new ChatClientThread(socket, this));
                    System.out.println("new client connected: " + socket.getPort());
                }
                catch (IOException e) {
                    System.err.println("Failed to accept client connection");
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Could not connect to port");
            e.printStackTrace();
        }
        finally {
            stop();
        }
    }

    public synchronized void stop() {
        System.out.println("closed");
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    public List<ChatRoom> getUserChatRooms(User user) {
        return data.chatRooms.values().stream().filter(n -> n.getMembers().contains(user))
                .collect(Collectors.toList());
    }

    public boolean userExistsInChatRoom(String chatName, User user) {
        List<ChatRoom> userChatRooms = getUserChatRooms(user);
        for(ChatRoom room : userChatRooms) {
            if(room.getName().equals(chatName)) {
                return true;
            }
        }
        return false;
    }

    public void addToRunningChats(ChatClientThread client) {
        this.runningChats.add(client);
    }

    public void sendToChatRoom(String message, String chatRoom, ChatClientThread senderThread) {
        for(ChatClientThread clientThread : runningChats) {
            //send message to all clients running the chatroom apart from the sender
            if(clientThread.openChatRoom != null && clientThread.openChatRoom.equals(chatRoom) && clientThread != senderThread ) {
                clientThread.output.println(message);
            }
        }
    }

    public void removeRunningChats(ChatClientThread client) {
        this.runningChats.remove(client);
    }
}


