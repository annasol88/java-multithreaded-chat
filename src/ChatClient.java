import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private static BufferedReader input;
    private static BufferedReader userInput;
    private static PrintWriter output;

    public ChatClient(Socket socket) {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            userInput = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintWriter(socket.getOutputStream(), true);

            loginMenu();
            //TO REMOVE anna - just for testing my stuff plz ignore
            //mainMenu();
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
        int choice = getUserOptionSelection(3, true);
        switch (choice) {
            case 1:
                System.out.println("to do");
                break;
            case 2:
                output.println(ServerRequest.REGISTER_USER);
                break;
            case 3:
                System.out.println("to dooo");
                break;
        }
    }

    private int getUserOptionSelection(int limit, boolean allowEscape) {
        int selection = -1;
        //-1 indicates the input is unset or invalid
        //0 indicates user has escaped the option selection
        while(selection == -1) {
            selection = getValidOptionSelectionFromInput(limit, allowEscape);
        }
        return selection;
    }

    private int getValidOptionSelectionFromInput(int limit, boolean allowEscape) {
        System.out.print(">>>");
        try {
            //read user input
            int selection = Integer.parseInt(userInput.readLine().trim());

            if(allowEscape && selection == 0) {
                return 0;
            }
            else if(selection < 0 || selection > limit){
                System.out.println("Not a valid option, please enter a number between 1 and " + limit);
                return -1;
            }
            else {
                return selection;
            }
        }
        catch(NumberFormatException e) {
            System.out.println("Not a valid option, please enter a number");
            return -1;
        }
        catch(IOException e) {
            System.err.println("Failed to read user input");
            return -1;
        }
    }


    private void mainMenu() {
        try {
            boolean logout = false;
            while (!logout) {
                System.out.println("1. view chat list");
                System.out.println("2. create new chat");
                System.out.println("3. view friends list");
                System.out.println("4. logout");
                int selection = getUserOptionSelection(4, false);
                switch (selection) {
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
        }
        // thrown when a server is disconnected during a menu operation
        catch (IOException e) {
            System.err.println("Failed to fetch response from server. Please check connection.");
        }
        finally {
            stop();
        }
    }

    private void viewChatList() throws IOException {
        output.println(ServerRequest.GET_CHATS);
        // receives a comma separated list of chatroom names that the user is part of
        String chats = input.readLine();

        // if user is not part of any chat rooms.
        if(chats == null || chats.equals("")) {
            System.out.println("You aren't currently part of any chat rooms.");
            return;
        }

        String[] chatNames = chats.split(",");
        for(int i = 1; i < chatNames.length + 1; i++) {
            System.out.println(i + ". " + chatNames[i-1]);
        }

        System.out.println("Enter the number of the chat you would like to enter or 0 to return to main menu.");
        int selection = getUserOptionSelection(chatNames.length, true);

        if(selection != 0) {
            output.println(ServerRequest.OPEN_CHAT_ROOM);
            output.println(chatNames[selection-1]);
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
