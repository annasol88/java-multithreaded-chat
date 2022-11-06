import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * Runner class for our client.
 * Allow Parallel runs in config to host multiple clients on the server
 */
public class ClientDriver {
    // get these from args (?)
    private static final int PORT = 9876;
    private static final String SERVER_IP = "localhost";

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            new ChatClient(socket);
        }
        catch(UnknownHostException e) {
            System.err.println("Unknown host, please re-verify, try again.");
        }
        catch (IOException e) {
            System.err.println("Could not connect to server. Please ensure The server is running.");
        }
    }
}
