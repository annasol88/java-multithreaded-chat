import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatClient {

    private static BufferedReader input;
    private static BufferedReader userInput;
    private static PrintWriter output;

    public ChatClient(Socket socket) {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            userInput = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintWriter(socket.getOutputStream(), true);

            startClient();
        }
        catch(IOException e) {
            System.err.println("Could not connect to socket input and output streams.");
        }
        finally {
            stop();
        }
    }

    private void startClient() {
        try {
            showStartMenu();
            String userRequest = userInput.readLine();
            //TODO - handle user request here
            // example communication below
            output.println(userRequest);
            output.flush();
            Object response = input.readLine();
            System.out.println(response);

        } catch (IOException e) {
            System.err.println("Server connection lost.");
        }
        finally {
            stop();
        }
    }

    private void showStartMenu() {
        System.out.println("1. login");
        System.out.println("2. register");
        System.out.println("3. stop");
    }

    private Map<String, String> getLoginDetails() {
        //TODO
        //a map is probs the best way to handle user details
        Map<String, String> login = new HashMap<>();
        login.put("username", "anna123");
        login.put("password", "123");
        return login;
    }

    private void displayChatList(ArrayList<ChatRoom> chats) {
        //TODO
    }

    private static void stop() {
        try {
            output.write("stop");
            output.close();
            input.close();
        }
        catch(IOException e) {
            System.err.println("Could not close input or output stream.");
            e.printStackTrace();
        }
    }
}
