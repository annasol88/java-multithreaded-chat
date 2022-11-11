import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class ConsoleListenerThread extends Thread {
    private Socket socket;
    private ChatClient client;
    private BufferedReader userInput;

    public ConsoleListenerThread(Socket socket, ChatClient client, BufferedReader userInput) {
        this.socket = socket;
        this.client = client;
        this.userInput = userInput;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                String input = userInput.readLine();
                client.sendMessage(input);
            }
            catch (IOException e) {
                System.err.println("failed to read user input");
            }
        }
    }
}
