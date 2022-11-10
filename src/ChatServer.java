import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ChatServer implements Runnable {
    protected final String serverIP;
    protected final int port;
    protected ServerSocket serverSocket = null;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static ServerData data;

    public ChatServer(int port, String serverIP) {
        this.port = port;
        this.serverIP = serverIP;
        data = new ServerData();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is running...");

            while(!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.submit(new ChatClientThread(socket));
                    System.out.println("new client connected: " + socket.getPort());
                }
                catch (IOException e) {
                    System.err.println("Failed to accept socket");
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

    public static synchronized User loginUser(String username, String password) {
        for (User account : ServerData.accounts) {
            if(
                account.getUsername().equals(username)
                && account.getPassword().equals(password)
                && !account.isLoggedIn()
            ) {
                return account;
            }
        }
        return null;
    }

    public static List<ChatRoom> getUserChatRooms(User user) {
        return data.chatRooms.stream().filter(n -> n.getMembers().contains(user))
                .collect(Collectors.toList());
    }

    public void openChatRoomByName(String name) {
        try {
            Socket socket = new Socket(serverIP, port);
            serverSocket.accept();
            System.out.println("new chatroom connected: " + socket.getPort());
            //find chatroom
            //threadPool.submit(new ChatRoomThread(socket, chatRoom));
        }
        catch(UnknownHostException e) {
            System.err.println("Unknown host, please re-verify, try again.");
        }
        catch (IOException e) {
            System.err.println("Failed to create Socket.");
        }
    }

    public static void addChatRoom(ChatRoom room) {
        data.chatRooms.add(room);
    }
}


