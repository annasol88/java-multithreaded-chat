import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatClient {
    // get these from args (?)
    private static final int PORT = 9876;
    private static final String SERVER_IP = "localhost";

    private static BufferedReader input;
    private static BufferedReader userInput;
    private static PrintWriter output;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(SERVER_IP, PORT);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        userInput = new BufferedReader(new InputStreamReader(System.in));
        output = new PrintWriter(socket.getOutputStream(), true);

        showLoginRegisterScreen();
    }

    private static void showLoginRegisterScreen() {
        //TODO
        //example
        String request = "login anna123";
        System.out.println("sending request " + request);
        output.println(request);

        // change to: while keyboard request is not logout or sys.exit
        try {
            while(true) {
                Object response = input.readLine();
                System.out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            logout();
        }

    }

    private static Map<String, String> getLoginDetails() {
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

    private static void logout() {
        try {
            output.write("logout");
            output.close();
            input.close();
        }
        catch(IOException e) {
            System.err.println("Could not close input or output stream.");
            e.printStackTrace();
        }
    }
}
