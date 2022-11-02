import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements Runnable {
    protected final int port;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private ArrayList<ChatRoom> chatRooms;
    private ArrayList<User> accounts;
    private ArrayList<User> admins;

    public ChatServer(int port) {
        this.port = port;
        chatRooms = new ArrayList<>();
        accounts = new ArrayList<>();
        admins = new ArrayList<>();

        // accounts for testing
        accounts.add(new User("anna", "software developer", "anna123", "123"));
        accounts.add(new User("emma", "software developer", "emma123", "123"));
        accounts.add(new User("alex", "software developer", "alex123", "123"));
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is waiting for connections...");

            while(!isStopped()) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.submit(new ChatClient(socket));
                }
                catch (IOException e) {
                    System.err.println("Failed to accept socket");
                    e.printStackTrace();
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

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

}


