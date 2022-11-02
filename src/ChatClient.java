import java.io.*;
import java.net.Socket;

public class ChatClient implements Runnable {
    private final Socket socket;
    private ChatUI ui;
    private InputStream inputStream;
    private OutputStream outputStream;
    // User associated with the running client
    private User user;

    public ChatClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ui = new ChatUI();
            inputStream  = socket.getInputStream();
            outputStream = socket.getOutputStream();

            startLoginOrRegister();
            //after successful login
            viewChats();

            // example communication
            System.out.println("client running");

        }
        catch(IOException e) {
            System.err.println("Failed to get socket input and output stream.");
            e.printStackTrace();
        }
    }

    public void logout() {
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        }
        catch(IOException e) {
            System.err.println("Could not close the socket, input or output stream.");
            e.printStackTrace();
        }
    }

    private void startLoginOrRegister() {

        //ui.showLoginRegisterScreen();
        //get login details
        //outputStream.write("replace with login details".getBytes());
    }

    private void viewChats() {

    }
}
