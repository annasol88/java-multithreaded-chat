import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements Runnable {
    protected final int port;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static ServerData data;

    public ChatServer(int port) {
        this.port = port;
        data = new ServerData();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is running...");

            while(!isStopped()) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.submit(new ChatClientThread(socket));
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

    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    public static synchronized User loginUser(String username, String password) {
        for (User account : data.accounts) {
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


    private synchronized boolean isStopped() {
        return this.isStopped;
    }
}


