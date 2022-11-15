import java.io.BufferedReader;
import java.io.IOException;

public class ConsoleListenerThread implements Runnable {
    private ChatClient client;
    private BufferedReader userInput;
    private boolean stopped = false;

    public ConsoleListenerThread(ChatClient client, BufferedReader userInput) {
        this.client = client;
        this.userInput = userInput;
    }

    @Override
    public void run() {
        try {
            while (!stopped) {
                String input = userInput.readLine();
                handleUserInput(input);
            }
        } catch (IOException e) {
            System.err.println("Failed to read user input");
            client.stop();
        }
    }

    public void stop() {
        try {
            this.stopped = true;
            this.userInput.close();
        } catch (IOException e) {
            System.out.println("Failed closing user input stream: " + e.getMessage());
        }
    }

    private void handleUserInput(String input) {
        switch (client.currentScreen) {
            case LOGIN_MENU:
                client.handleLoginMenuSelection(input);
                break;
            case LOGIN_ENTERING_USERNAME:
                client.loginUsernameEnteredGetPassword(input);
                break;
            case LOGIN_ENTERING_PASSWORD:
                client.loginSendRequest(input);
                break;
            case REGISTER_ENTERING_USERNAME:
                client.registerUsernameEntered(input);
                break;
            case REGISTER_ENTERING_PASSWORD:
                client.registerPasswordEnteredGetName(input);
                break;
            case REGISTER_ENTERING_NAME:
                client.registerNameEnteredGetBio(input);
                break;
            case REGISTER_ENTERING_BIO:
                client.registerSendRequest(input);
                break;
            case MAIN_MENU:
                client.handleMainMenuSelection(input);
                break;
            case CHATROOM_OPTION_MENU:
                client.handleChatRoomOptionMenuSelection(input);
                break;
            case CHAT_MEMBER_OPTION_MENU:
                client.handleChatMemberOptionMenuSelection(input);
                break;
            case ENTERING_CHAT_NAME:
                client.chatRoomNameEntered(input);
                break;
            case ENTERING_CHAT_MEMBER_NAME:
                client.chatMemberNameEntered(input);
                break;
            case FRIEND_REQUEST_MENU:
                client.handleFriendRequestOptionMenuSelection(input);
                break;
            case ENTERING_FRIEND_REQUEST_NAME:
                client.friendRequestNameEntered(input);
                break;
            case CHATTING:
                client.chatRoomSendMessage(input);
                break;
            case EDIT_PROFILE_MENU:
                client.handleEditProfileMenuSelection(input);
                break;
            case EDITING_NAME:
                client.profileEditName(input);
                break;
            case EDITING_BIO:
                client.profileEditBio(input);
                break;
        }
    }
}
