import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private ArrayList<ChatRoomThread> runningChats = new ArrayList<>();

    private static BufferedReader userInput;
    private static ServerData data;

    public ChatServer(int port, String serverIP) {
        this.port = port;
        this.serverIP = serverIP;
        data = new ServerData();
        userInput = new BufferedReader(new InputStreamReader(System.in));
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

    static synchronized void registerUser() throws IOException {

        System.out.println("Please enter username");
        System.out.println(">>>");
        String username = userInput.readLine();
        System.out.println(username);
        for (User account : ServerData.accounts.values()) {
            System.out.println(account.getUsername().toLowerCase());
            if (account.getUsername().toLowerCase().equals(username.toLowerCase())) {
                System.out.println("This username already exists");
                registerUser();
            }
        }
        System.out.println("Please enter your name");
        System.out.println(">>>");
        String name = userInput.readLine();
        System.out.println("Please enter your bio");
        System.out.println(">>>");
        String bio = userInput.readLine();
        System.out.println("Please set password");
        System.out.println(">>>");
        String password = userInput.readLine();
        User user = new User(name, username, bio, password, new ArrayList<>());
        data.addAccount(user);
    }

    public synchronized User loginUser(String username, String password) {
        for (User account : ServerData.accounts.values()) {
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

    public List<ChatRoom> getUserChatRooms(User user) {
        return data.chatRooms.values().stream().filter(n -> n.getMembers().contains(user))
                .collect(Collectors.toList());
    }

    public ChatRoom getChatRoomByName(String name) {
        return data.chatRooms.get(name);
    }

    public void addRunningChat(ChatRoomThread chatThread) {
        this.runningChats.add(chatThread);
    }

    public ArrayList<ChatRoomThread> getRunningChats() {
        return runningChats;
    }
}


