import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientRunner {
    // get these from args (?)
    private static final int PORT = 9876;
    private static final String SERVER_IP = "localhost";

    public static void main(String[] args) {
        // for simple single client run
        // createClient();

        // to generate clients on command
        String line = readLine();
        while (!line.equals("x")) {
            if(line.equals("n")) {
                createClient();
            }
            else {
                System.out.println( line + " is not a recognised command.");
            }
            line = readLine();
        }
    }

    public static void createClient() {
        try {
            new Socket(SERVER_IP, PORT);
        }
        catch(UnknownHostException e) {
            System.err.println("unknown host. please verify and try again");
            e.printStackTrace();
        }
        catch(IOException e) {
            System.err.println("Failed to create client Socket");
            e.printStackTrace();
        }
    }

    private static String readLine() {
        try {
            System.out.println("type n to create a new client. or x to exit");
            System.out.print(">>>");
            BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
            return buffer.readLine().trim();
        }
        catch(IOException e) {
            System.err.println("Failed to read input.");
            e.printStackTrace();
        }
        return null;
    }
}
