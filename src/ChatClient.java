import java.io.*;
import java.net.Socket;
import java.util.List;

public class ChatClient {
    private static BufferedReader input;
    private static BufferedReader userInput;
    private static PrintWriter output;

    public ChatClient(Socket socket) {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            userInput = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintWriter(socket.getOutputStream(), true);

            //loginMenu();
            mainMenu();
        }
        catch(IOException e) {
            System.err.println("Could not connect to socket input and output streams.");
        }
        finally {
            stop();
        }
    }

    private void loginMenu() {
        System.out.println("1. login");
        System.out.println("2. register");
        System.out.println("3. stop");
    }

    private int getUserMenuSelection(int limit) {
        int selection = 0;
        //0 indicates the input is unset or invalid
        while(selection == 0) {
            selection = getValidMenuSelectionFromInput(limit);
        }
        return selection;
    }

    private int getValidMenuSelectionFromInput(int limit) {
        System.out.print(">>>");
        try {
            int input = Integer.parseInt(userInput.readLine().trim());
            if(input < 1 || input > limit){
                System.out.println("invalid menu selection, please enter a number between 1 and " + limit);
                return 0;
            }
            else {
                return input;
            }
        }
        catch(NumberFormatException e) {
            System.out.println("menu selection must be a number");
            return 0;
        }
        catch(IOException e) {
            System.err.println("Failed to read user input");
            return 0;
        }
    }

    private void mainMenu() {
        boolean logout = false;
        while(!logout) {
            System.out.println("1. view chat list");
            System.out.println("2. create new chat");
            System.out.println("3. view friends list");
            System.out.println("4. logout");
            int selection = getUserMenuSelection(4);
            switch(selection) {
                case 1:
                    viewChatList();
                    break;
                case 2:
                    System.out.println("To be implemented");
                    break;
                case 3:
                    System.out.println("To be implemented");
                    break;
                case 4:
                    logout = true;
                    break;
            }
        }
        stop();
    }

    private void viewChatList() {
        output.println(ServerRequest.GET_CHATS);
        try {
            String chats = input.readLine();
            System.out.println(chats);
            //TODO

        }
        catch(IOException e) {
            System.err.println("Failed to fetch response from server. Please check connection.");
        }
    }

    private static void stop() {
        try {
            output.println(ServerRequest.STOP);
            output.close();
            input.close();
        }
        catch(IOException e) {
            System.err.println("Could not close input or output stream.");
            e.printStackTrace();
        }
    }
}
