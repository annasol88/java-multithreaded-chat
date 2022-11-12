import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private WriterThread writer;
    private ListenerThread listener;
    private ConsoleListenerThread consoleListener;

    // represents the current screen the user is responding to
    // this is used to properly map user input to the proper operation
    public CurrentClientScreen currentScreen;

    public ChatClient(Socket socket) {
        this.socket = socket;
        listener = new ListenerThread(socket, this);
        writer = new WriterThread(socket);

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        consoleListener = new ConsoleListenerThread(this, userInput);

        new Thread(listener).start();
        new Thread(writer).start();
        new Thread(consoleListener).start();

        //loginMenu();
        //TO REMOVE anna - just for testing my stuff plz ignore
        showMainMenu();
    }

    public void showLoginMenu() {
        currentScreen = CurrentClientScreen.LOGIN;
        System.out.println("Welcome to the chat app, please select what you want to do:");
        System.out.println("1. login");
        System.out.println("2. register");
        System.out.println("3. exit");
    }

    public void showMainMenu() {
        currentScreen = CurrentClientScreen.MAIN;
        System.out.println("Select an option from the menu below:");
        System.out.println("1. view chat list");
        System.out.println("2. enter chat room");
        System.out.println("3. create new chat");
        System.out.println("4. view friends list");
        System.out.println("5. edit profile");
        System.out.println("6. logout");
    }

    public void handleLoginMenuSelection(String input) {
        int choice = validateUserMenuInput(input, 3);
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                System.out.println("to do");
                break;
            case 3:
                System.out.println("Goodbye");
                stop();
                break;
        }
    }

    public void handleMainMenuSelection(String input) {
        int selection = validateUserMenuInput(input, 6);
        switch (selection) {
            case 1:
                requestChatList();
                break;
            case 2:
                askChatRoomName();
                break;
            case 3:
                System.out.println("To be implemented");
                break;
            case 4:
                requestFriendsList();
                break;
            case 5:
                System.out.println("To be implemented");
                break;
            case 6:
                logout();
                break;
        }
    }

    public void requestChatList() {
        writer.sendRequest("view chat list");
    }

    public void showChatList(String list) {
        list = list.replace("show chat list:", "").trim();
        if (list.equals("")) {
            System.out.println("You aren't currently part of any chat rooms.");
            return;
        }

        String[] chatNames = list.split(",");
        for (int i = 1; i < chatNames.length + 1; i++) {
            System.out.println(chatNames[i - 1]);
        }
    }

    public void askChatRoomName() {
        currentScreen = CurrentClientScreen.ENTERING_CHATROOM;
        System.out.println("enter the name of the chat you would like to enter or x to return to menu:");
    }

    public void enterChatRoom(String chatName) {
        if (chatName.equals("x")) {
            showMainMenu();
        } else {
            writer.sendRequest("enter chat room:" + chatName);
        }
    }

    public void runChatRoom() {
        currentScreen = CurrentClientScreen.CHATROOM;
        System.out.println("chat room entered");
        System.out.println("type a message or x to exit");
    }

    public void sendMessageToChat(String input) {
        if (input.equals("x")) {
            writer.sendRequest("exit chat room");
            showMainMenu();
        } else {
            writer.sendRequest("send message:" + input);
        }
    }

    public void showInvalidChatRoomName() {
        System.out.println("you are not part of this chat room");
        showMainMenu();
    }

    public void requestFriendsList() {
        writer.sendRequest("view friends list");
    }

    public void showFriendsList(String friends) {
        friends = friends.replace("show friends list:", "").trim();
        if (friends.equals("")) {
            System.out.println("You don't currently have any friends in your friends list.");
            return;
        }

        String[] friendNames = friends.split(",");
        for (int i = 1; i < friendNames.length + 1; i++) {
            System.out.println(friendNames[i - 1]);
        }
    }

    public void login() {
        writer.sendRequest("login");
        showMainMenu();
    }

    public void logout() {
        writer.sendRequest("logout");
        System.out.println("you have logged out");
        showLoginMenu();
    }

    public void stop() {
        writer.sendRequest("stop");
        writer.stop();
        listener.stop();
        consoleListener.stop();
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket connection: " + e.getMessage());
        }
    }

    public void print(String input) {
        System.out.println(input);
    }

    private int validateUserMenuInput(String input, int limit) {
        try {
            int selection = Integer.parseInt(input.trim());
            if (selection < 1 || selection > limit) {
                System.out.println("Not a valid option, please enter a number between 1 and " + limit);
                return -1;
            }
            return selection;
        } catch (NumberFormatException e) {
            System.out.println("Not a valid option, please enter a number");
            return -1;
        }
    }
}
