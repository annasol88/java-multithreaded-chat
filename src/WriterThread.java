import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WriterThread implements Runnable {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;
    private ConcurrentLinkedQueue<String> requestQueue;
    private boolean stopped = false;

    public WriterThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;
        this.requestQueue = new ConcurrentLinkedQueue<>();

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void queueAction(String request) {
        requestQueue.add(request);
    }

    public void run() {
        while (!stopped){
            if(!requestQueue.isEmpty()) {
                writer.println(requestQueue.poll());
            }
        }
    }

    public void stop() {
        try {
            this.stopped = true;
            socket.close();
        } catch (IOException ex) {

            System.out.println("Server Connection Lost: " + ex.getMessage());
        }
    }
}
