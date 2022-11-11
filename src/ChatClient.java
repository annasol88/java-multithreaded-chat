import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private static BufferedReader userInput;
    private WriterThread writer;
    private ListenerThread listener;

    private User user;
    private boolean inChatRoom;
    private Thread consoleListener;

    public ChatClient(Socket socket) {
        this.socket = socket;
        userInput = new BufferedReader(new InputStreamReader(System.in));

        this.listener = new ListenerThread(socket, this);
        this.writer = new WriterThread(socket, this);

        new Thread(listener).start();
        new Thread(writer).start();

        loginMenu();
        //TO REMOVE anna - just for testing my stuff plz ignore
//        ServerData data = new ServerData();
//        user = data.accounts.get("anna123");
//        showMainMenu();
    }

    private void showMainMenu() {
        try {
            System.out.println("1. view chat list");
            System.out.println("2. enter chat");
            System.out.println("3. create new chat");
            System.out.println("4. view friends list");
            System.out.println("5. edit profile");
            System.out.println("6. logout");
            boolean logout = false;
            inChatRoom = false;
            while (!logout && inChatRoom != true) {
                int selection = getUserOptionSelection(6);
                switch (selection) {
                    case 1:
                        viewChatList();
                        break;
                    case 2:
                        enterChatRoom();
                        break;
                    case 3:
                        System.out.println("To be implemented");
                        break;
                    case 4:
                        System.out.println("To be implemented");
                        break;
                    case 5:
                        System.out.println("To be implemented");
                        break;
                    case 6:
                        logout();
                        logout = true;
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read user input");
        }
    }

    /*
     * handle actions sent from the server
     */
    public void doServerAction(String request) {
        if (request != null) {
            if (request.startsWith("show chat list")) {
                showChatList(request);
            } else if (request.startsWith("run chat room")) {
                runChatRoom();
            } else if (request.contains("chat room invalid")) {
                System.out.println("you are not part of this chat room");
                showMainMenu();
            } else {
                System.out.println(request);
            }
        }
    }

    private void loginMenu() {
        try {
            System.out.println("1. login");
            System.out.println("2. register");
            System.out.println("3. stop");
            int choice = getUserOptionSelection(3);
            switch (choice) {
                case 1:
                    System.out.println("to do");
                    break;
                case 2:
                    System.out.println("to do");
                    break;
                case 3:
                    System.out.println("to do");
                    break;
            }
        } catch (IOException e) {
            System.err.println("Failed to read user input");
        }
    }

    private int getUserOptionSelection(int limit) throws IOException {
        int selection = -1;
        while (selection == -1) {
            selection = getValidOptionSelectionFromInput(limit);
        }
        return selection;
    }

    private int getValidOptionSelectionFromInput(int limit) throws IOException {
        try {
            //read user input
            int selection = Integer.parseInt(userInput.readLine().trim());

            if (selection < 1 || selection > limit) {
                System.out.println("Not a valid option, please enter a number between 1 and " + limit);
                return -1;
            } else {
                return selection;
            }
        } catch (NumberFormatException e) {
            System.out.println("Not a valid option, please enter a number");
            return -1;
        }
    }

    private void viewChatList() {
        writer.queueAction("view chat list");
    }

    private void showChatList(String list) {
        list = list.replace("show chat list: ", "");
        if (list.equals("")) {
            System.out.println("You aren't currently part of any chat rooms.");
            return;
        }

        String[] chatNames = list.split(",");
        for (int i = 1; i < chatNames.length + 1; i++) {
            System.out.println(i + ". " + chatNames[i - 1]);
        }
    }

    private void enterChatRoom() throws IOException {
        inChatRoom = true;
        System.out.println("enter the name of the chat you would like to enter:");
        String chatName = userInput.readLine();
        writer.queueAction("enter chat room:" + chatName);
    }

    private void runChatRoom() {
        System.out.println("chat room entered");
        System.out.println("type a message or x to exit");

        consoleListener = new ConsoleListenerThread(socket, this, userInput);
        consoleListener.start();
    }

    public void sendMessage(String input) {
        if (input != null) {
            if (input.equals("x")) {
                consoleListener.interrupt();
                writer.queueAction("exit chat room");
            } else {
                writer.queueAction("send message:" + user.getName() + ": " + input);
            }
        }
    }

    private void logout() {
        writer.queueAction("logout");
        stop();
    }

    private void stop() {
        writer.stop();
        listener.stop();
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket connection: " + e.getMessage());
        }
    }
}
