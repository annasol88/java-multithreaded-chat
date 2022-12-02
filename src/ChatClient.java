import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

public class ChatClient {
    // represents the current screen the user is responding to
    // this is used to properly map user input to the proper operation
    public CurrentClientScreenEnum currentScreen;

    private Socket socket;
    private WriterThread writer;
    private ListenerThread listener;
    private ConsoleListenerThread consoleListener;

    public HashMap<String, String> tempStorage = new HashMap<>();

    public ChatClient(Socket socket, boolean isTest) {
        this.socket = socket;

        listener = new ListenerThread(socket, this);
        writer = new WriterThread(socket);

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        consoleListener = new ConsoleListenerThread(this, userInput);

        new Thread(listener).start();
        new Thread(writer).start();
        new Thread(consoleListener).start();

        if (!isTest) {
            showLoginMenu();
        }
    }

    public void showLoginMenu() {
        currentScreen = CurrentClientScreenEnum.LOGIN_MENU;
        tempStorage.clear();
        System.out.println();
        System.out.println("Welcome to the chat app, please select what you want to do:");
        System.out.println("1. login");
        System.out.println("2. register");
        System.out.println("3. exit");
    }

    public void showMainMenu() {
        currentScreen = CurrentClientScreenEnum.MAIN_MENU;
        tempStorage.clear();
        System.out.println();
        System.out.println("Select an option from the menu below:");
        System.out.println("1. view chat list");
        System.out.println("2. create new chat");
        System.out.println("3. view friends list");
        System.out.println("4. view pending friend requests");
        System.out.println("5. view profile");
        System.out.println("6. logout");
    }

    public void showChatRoomOptionMenu() {
        currentScreen = CurrentClientScreenEnum.CHATROOM_OPTION_MENU;
        tempStorage.remove("chat room action");
        tempStorage.remove("chat room name");
        System.out.println();
        System.out.println("Select an option to perform on a chatroom:");
        System.out.println("1. view members");
        System.out.println("2. enter chat room");
        System.out.println("3. leave chat room");
        System.out.println("4. return to main menu");
    }

    public void showChatMemberOptionMenu() {
        currentScreen = CurrentClientScreenEnum.CHAT_MEMBER_OPTION_MENU;
        tempStorage.remove("chat member action");
        tempStorage.remove("chat member name");
        System.out.println();
        System.out.println("Select an option to perform on a chat room member:");
        System.out.println("1. make a friend request");
        System.out.println("2. view member profile");
        System.out.println("3. return to main menu");
    }

    public void showFriendListOptionMenu() {
        currentScreen = CurrentClientScreenEnum.FRIEND_LIST_MENU;
        tempStorage.clear();
        System.out.println();
        System.out.println("Select an option to perform on a friend:");
        System.out.println("1. send private message");
        System.out.println("2. return to main menu");
    }

    public void showFriendRequestOptionMenu() {
        currentScreen = CurrentClientScreenEnum.FRIEND_REQUEST_MENU;
        tempStorage.clear();
        System.out.println();
        System.out.println("Select an option to perform on a your friend request:");
        System.out.println("1. accept request");
        System.out.println("2. deny request");
        System.out.println("3. return to main menu");
    }

    public void showEditProfileMenu() {
        currentScreen = CurrentClientScreenEnum.EDIT_PROFILE_MENU;
        tempStorage.clear();
        System.out.println();
        System.out.println("Select the part of your profile you would like to edit:");
        System.out.println("1. change name");
        System.out.println("2. change bio");
        System.out.println("3. return to main menu");
    }

    public void handleLoginMenuSelection(String input) {
        int choice = validateUserMenuInput(input, 3);
        switch (choice) {
            case 1:
                loginGetUsername();
                break;
            case 2:
                registerGetUsername();
                break;
            case 3:
                stop();
                break;
        }
    }

    public void handleMainMenuSelection(String input) {
        int selection = validateUserMenuInput(input, 6);
        switch (selection) {
            case 1:
                writer.sendRequest("view chat list");
                break;
            case 2:
                System.out.println("to be implemented");
                break;
            case 3:
                writer.sendRequest("view friends list");
                break;
            case 4:
                writer.sendRequest("view friend requests");
                break;
            case 5:
                writer.sendRequest("view own profile");
                break;
            case 6:
                logout();
                break;
        }
    }

    public void handleChatRoomOptionMenuSelection(String input) {
        int selection = validateUserMenuInput(input, 4);
        if (selection != -1) {
            if (selection == 4) {
                showMainMenu();
            } else {
                tempStorage.put("chat room action", Integer.toString(selection));
                chatListAskChatRoomName();
            }
        }
    }

    public void handleChatMemberOptionMenuSelection(String input) {
        int selection = validateUserMenuInput(input, 3);

        if (selection == 3) {
            showMainMenu();
        } else if (selection != -1) {
            tempStorage.put("chat member action", Integer.toString(selection));
            askChatMemberName();

        }
    }

    public void handleFriendListOptionMenuSelection(String input) {
        int selection = validateUserMenuInput(input, 2);
        switch (selection) {
            case 1:
                System.out.println("To be implemented");
                break;
            case 2:
                showMainMenu();
                break;
        }
    }

    public void handleFriendRequestOptionMenuSelection(String input) {
        int selection = validateUserMenuInput(input, 3);

        if (selection == 3) {
            showMainMenu();
        } else if (selection != -1) {
            tempStorage.put("friend request action", Integer.toString(selection));
            askFriendRequestName();
        }
    }

    public void handleEditProfileMenuSelection(String input) {
        int selection = validateUserMenuInput(input, 3);
        switch (selection) {
            case 1:
                currentScreen = CurrentClientScreenEnum.EDITING_NAME;
                System.out.println("Enter new name:");
                break;
            case 2:
                currentScreen = CurrentClientScreenEnum.EDITING_BIO;
                System.out.println("Enter new bio:");
                break;
            case 3:
                showMainMenu();
                break;
        }
    }

    public void loginGetUsername() {
        currentScreen = CurrentClientScreenEnum.LOGIN_ENTERING_USERNAME;
        System.out.println("Please enter your username:");
    }

    public void loginUsernameEnteredGetPassword(String username) {
        tempStorage.put("username", username);
        currentScreen = CurrentClientScreenEnum.LOGIN_ENTERING_PASSWORD;
        System.out.println("Please enter your password:");
    }

    public void loginSendRequest(String password) {
        writer.sendRequest("login:" + tempStorage.get("username") + "," + password);
    }

    public void loginSuccess() {
        System.out.println("You are now logged in as: " + tempStorage.get("username"));
        showMainMenu();
    }

    public void loginInvalid() {
        System.out.println("Your username or password is incorrect.");
        showLoginMenu();
    }

    public void loginAlreadyOnline() {
        System.out.println("You are already logged in on another device.");
        showLoginMenu();
    }

    public void registerGetUsername() {
        currentScreen = CurrentClientScreenEnum.REGISTER_ENTERING_USERNAME;
        System.out.println("Please enter a username:");
    }

    public void registerUsernameEntered(String username) {
        tempStorage.put("username", username);
        writer.sendRequest("check username free:" + username);
    }

    public void registerUsernameTaken() {
        System.out.println("Username is already taken. Please enter another one:");
    }

    public void registerUsernameFreeGetPassword() {
        currentScreen = CurrentClientScreenEnum.REGISTER_ENTERING_PASSWORD;
        System.out.println("Please enter a password:");
    }

    public void registerPasswordEnteredGetName(String password) {
        tempStorage.put("password", password);
        currentScreen = CurrentClientScreenEnum.REGISTER_ENTERING_NAME;
        System.out.println("Please enter an account name:");
    }

    public void registerNameEnteredGetBio(String name) {
        tempStorage.put("name", name);
        currentScreen = CurrentClientScreenEnum.REGISTER_ENTERING_BIO;
        System.out.println("Please enter an account bio:");
    }

    public void registerSendRequest(String bio) {
        writer.sendRequest(
                "register user:" +
                        tempStorage.get("username") +
                        "," + tempStorage.get("password") +
                        "," + tempStorage.get("name") +
                        "," + bio
        );
    }

    public void chatListShow(String[] chats) {
        if (chats.length == 0) {
            System.out.println("You aren't currently part of any chat rooms.");
            showMainMenu();
            return;
        }

        System.out.println("Your chat rooms:");
        for (int i = 1; i < chats.length + 1; i++) {
            System.out.println(chats[i - 1]);
        }
        showChatRoomOptionMenu();
    }

    public void chatListAskChatRoomName() {
        currentScreen = CurrentClientScreenEnum.ENTERING_CHAT_NAME;
        tempStorage.remove("chat room name");
        System.out.println("enter chat room name, or x to return to chat menu:");
    }

    public void chatRoomNameEntered(String chatName) {
        if (chatName.equals("x")) {
            showChatRoomOptionMenu();
        } else {
            tempStorage.put("chat room name", chatName);
            writer.sendRequest("check chat exists:" + chatName);
        }
    }

    public void chatRoomNameInvalid() {
        System.out.println("You are not part of this chat room, please check spelling and enter another chat name:");
    }

    public void chatRoomValidDoMenuSelection() {
        switch (tempStorage.get("chat room action")) {
            case "1":
                writer.sendRequest("view chat members:" + tempStorage.get("chat room name"));
                break;
            case "2":
                chatRoomSendEnterRequest();
                break;
            case "3":
                chatRoomLeave();
                break;
        }
    }

    public void chatRoomShowMembers(String[] members) {
        // chat rooms are deleted without members but
        if (members.length == 0) {
            System.out.println("No chat room members. This chat room has been deleted since your last request.");
            return;
        }

        System.out.println("The members of this chat room are:");
        for (int i = 1; i < members.length + 1; i++) {
            System.out.println(members[i - 1]);
        }
        showChatMemberOptionMenu();
    }

    public void askChatMemberName() {
        currentScreen = CurrentClientScreenEnum.ENTERING_CHAT_MEMBER_NAME;
        tempStorage.remove("chat member name");
        System.out.println("enter username of a chat member, or x to return to chat menu:");
    }

    public void chatMemberNameEntered(String input) {
        if (input.equals("x")) {
            showChatRoomOptionMenu();
        } else {
            tempStorage.put("chat member name", input);
            writer.sendRequest("check chat member exists:" + tempStorage.get("chat room name") + "," + input);
        }
    }

    public void chatMemberInvalid() {
        tempStorage.remove("chat room member");
        System.out.println("This user is not part of the chat room, check spelling and enter another name:");
    }

    public void chatMemberDoMenuSelection() {
        switch (tempStorage.get("chat member action")) {
            case "1":
                chatMemberSendFriendRequest();
                break;
            case "2":
                writer.sendRequest("view profile:" + tempStorage.get("chat member name"));
                break;
        }
    }

    public void chatMemberSendFriendRequest() {
        writer.sendRequest("send friend request:" + tempStorage.get("chat member name"));
    }

    public void chatMemberFriendRequestSent() {
        System.out.println("You have sent a friend request to " + tempStorage.get("chat member name"));
        showChatMemberOptionMenu();
    }

    public void chatMemberAlreadyFriends() {
        System.out.println("You and " + tempStorage.get("chat member name") + " are already friends.");
        showChatMemberOptionMenu();
    }

    public void chatMemberCannotFriendRequestYourself() {
        System.out.println("You cannot send a friend request to yourself.");
        showChatMemberOptionMenu();
    }

    public void chatMemberShowProfile(String[] profile) {
        System.out.println("username: " + profile[0]);
        System.out.println("name: " + profile[1]);
        System.out.println("bio: " + profile[2]);

        showChatMemberOptionMenu();
    }

    public void chatRoomSendEnterRequest() {
        writer.sendRequest("enter chat room:" + tempStorage.get("chat room name"));
    }

    public void chatRoomRun() {
        currentScreen = CurrentClientScreenEnum.CHATTING;
        System.out.println("chat room entered: " + tempStorage.get("chat room name"));
        System.out.println("type a message or x to exit");
    }

    public void chatRoomSendMessage(String input) {
        if (input.equals("x")) {
            writer.sendRequest("close chat room");
            showMainMenu();
        } else {
            writer.sendRequest("send message:" + input);
        }
    }

    public void chatRoomLeave() {
        writer.sendRequest("leave chat room:" + tempStorage.get("chat room name"));
        System.out.println("You have left " + tempStorage.get("chat room name"));

        showMainMenu();
    }

    public void friendsListShow(String[] friends) {
        if (friends.length == 0) {
            System.out.println("You don't currently have any friends in your friends list.");
            showMainMenu();
            return;
        }

        System.out.println("Your friends:");
        for (int i = 1; i < friends.length + 1; i++) {
            System.out.println(friends[i - 1]);
        }
        showFriendListOptionMenu();
    }

    public void friendRequestsShow(String[] requests) {
        if (requests.length == 0) {
            System.out.println("You don't have any pending friend requests.");
            showMainMenu();
            return;
        }

        System.out.println("Your pending requests:");
        for (int i = 1; i < requests.length + 1; i++) {
            System.out.println(requests[i - 1]);
        }
        showFriendRequestOptionMenu();
    }

    public void askFriendRequestName() {
        currentScreen = CurrentClientScreenEnum.ENTERING_FRIEND_REQUEST_NAME;
        tempStorage.remove("friend request name");
        System.out.println("enter username, or x to return to friend request menu:");
    }

    public void friendRequestNameEntered(String input) {
        if (input.equals("x")) {
            showFriendRequestOptionMenu();
        } else {
            tempStorage.put("friend request name", input);
            writer.sendRequest("check friend request exists:" + input);
        }
    }

    public void friendRequestsNameInvalid() {
        System.out.println("this user is not in your friends list, please check spelling and enter another username:");
    }

    public void friendRequestsNameValidDoMenuSelection() {
        switch (tempStorage.get("friend request action")) {
            case "1":
                friendRequestAccept();
                break;
            case "2":
                friendRequestDeny();
                break;
        }
        showMainMenu();
    }

    public void friendRequestAccept() {
        System.out.println("request accepted for: " + tempStorage.get("friend request name"));
        writer.sendRequest("accept friend request:" + tempStorage.get("friend request name"));

    }

    public void friendRequestDeny() {
        System.out.println("request denied for: " + tempStorage.get("friend request name"));
        writer.sendRequest("deny friend request:" + tempStorage.get("friend request name"));
    }

    public void profileShow(String[] profile) {
        System.out.println("Name: " + profile[0]);
        System.out.println("Bio: " + profile[1]);
        showEditProfileMenu();
    }

    public void profileEditName(String input) {
        writer.sendRequest("edit profile name:" + input);
        System.out.println("Name updated to: " + input);
        showMainMenu();
    }

    public void profileEditBio(String input) {
        writer.sendRequest("edit profile bio:" + input);
        System.out.println("Bio updated to: " + input);
        showMainMenu();
    }

    public void logout() {
        writer.sendRequest("logout");
        System.out.println("You have logged out");
        showLoginMenu();
    }

    public void stop() {
        System.out.println("Goodbye");
        currentScreen = null;
        consoleListener.stop();
        writer.sendRequest("stop");
        writer.stop();

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket connection.");
        }
    }

    public void print(String input) {
        System.out.println(input);
    }

    private int validateUserMenuInput(String input, int limit) {
        try {
            int selection = Integer.parseInt(input);

            if (selection < 1 || selection > limit) {
                System.out.println("Not a valid option, please enter a number between 1 and " + limit + ":");
                return -1;
            }
            return selection;
        } catch (NumberFormatException e) {
            System.out.println("Not a valid option, please enter a number:");
            return -1;
        }
    }

    // For Testing
    public void testSendRequest() {
        writer.sendRequest("test");
    }

    public String testMockCurrentUser() {
        // for unique usernames
        String username = "username" + new Random().nextInt();
        writer.sendRequest("test mock user: " + username + ", testPassword, testName, testBio");
        return username;
    }
}
