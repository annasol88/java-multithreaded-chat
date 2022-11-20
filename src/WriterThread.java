import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WriterThread implements Runnable {
    private PrintWriter writer;
    private ConcurrentLinkedQueue<String> requestQueue;

    private boolean stopped = false;

    public WriterThread(Socket socket) {
        this.requestQueue = new ConcurrentLinkedQueue<>();

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void sendRequest(String request) {
        requestQueue.add(request);
    }

    public void run() {
        while (!stopped) {
            if(!requestQueue.isEmpty()) {
                String request = requestQueue.poll();
                writer.println(request);
            }
        }
    }

    public void stop() {
        this.stopped = true;
        writer.close();
    }
}
