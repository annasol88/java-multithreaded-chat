import java.io.*;
import java.net.Socket;

public class ChatClientThread implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    // User associated with the running client
    private User user;

    public ChatClientThread(Socket socket) {
        try {
            this.socket = socket;
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        }
        catch(IOException e) {
            System.err.println("Failed to get socket input and output stream.");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String request = input.readLine();
            while (!request.equals("stop")) {
                //TODO - handle request here
                // example
                output.println("request received " + request);
                output.flush();
                request = input.readLine();
            }
        } catch(IOException e) {
            System.err.println("Failed to read input from client");
        } finally {
            stop();
        }
    }

    public synchronized void stop() {
        try {
            output.close();
            input.close();
        }
        catch(IOException e) {
            System.err.println("Could not close the socket, input or output stream.");
            e.printStackTrace();
        }
    }


    private void loginUser(String username, String password) {
        //example server request
        user = ChatServer.loginUser(username, password);
        if(user != null) {
            output.write(user.getUsername());
        } else {
            output.write("invalid username or password");
        }
    }

    private void viewChats() {
        //TODO
    }
}
