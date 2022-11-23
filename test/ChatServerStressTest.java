import java.io.IOException;
import java.net.Socket;

public class ChatServerStressTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        int testNumberOfThreads = 200;

        ChatServer server = new ChatServer(Utils.PORT);
        new Thread(server).start();

        for(int i = 0; i < testNumberOfThreads; i++) {
            Socket socket = new Socket(Utils.SERVER_IP, Utils.PORT);
            ChatClient client = new ChatClient(socket, true);
            client.sendTestRequest();
        }

        // Wait for our server to send the test requests
        Thread.sleep(3000);

        System.out.println();
        System.out.println("Submitted thread operations: " + testNumberOfThreads);
        System.out.println("Received thread operations: " + server.testsReceived);
        server.stop();
    }
}

