import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ListenerThread implements Runnable {
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;

    private boolean stopped = false;

    public ListenerThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            while (!stopped) {
                String request = reader.readLine();
                client.doServerAction(request);
            }
        } catch (IOException ex) {
            System.err.println("Server connection lost: " + ex.getMessage());
        }
    }

    public void stop() {
        try {
            this.stopped = true;
            reader.close();
        } catch (IOException ex) {
            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }
}
