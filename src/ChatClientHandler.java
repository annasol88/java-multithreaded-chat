import java.io.*;
import java.net.Socket;


public class ChatClientHandler implements Runnable {
    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    // User associated with the running client
    private User user;

    public ChatClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            String request = input.readLine();
            while(!request.equals("logout")) {
                //do request here
                output.println("request received " + request);
                request = input.readLine();
            }
        }
        catch(IOException e) {
            System.err.println("Failed to get socket input and output stream.");
            e.printStackTrace();
        }
        finally {
            logout();
        }
    }

    public void logout() {
        try {
            output.close();
            input.close();
            socket.close();
        }
        catch(IOException e) {
            System.err.println("Could not close the socket, input or output stream.");
            e.printStackTrace();
        }
    }


    private void loginUser(String username, String password) {
        //example test
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
