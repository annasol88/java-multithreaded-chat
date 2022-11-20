import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ListenerThread implements Runnable {
    private BufferedReader reader;
    private ChatClient client;

    private boolean stopped = false;

    public ListenerThread(Socket socket, ChatClient client) {
        this.client = client;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error getting input stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (!stopped) {
                String request = reader.readLine();
                mapRequestToAction(request);
            }
        } catch (IOException e) {
            stop();
        }
    }

    public void stop() {
        this.stopped = true;

        try {
            reader.close();
        } catch (IOException ex) {
            System.out.println("Failed to close Listener Server Listener: " + ex.getMessage());
        }
    }

    private void mapRequestToAction(String request) {
        if (request != null) {
            String header = Utils.getRequestHeader(request);
            String[] params = Utils.getRequestParams(request);

            switch (header) {
                case "login success":
                    client.loginSuccess();
                    break;
                case "login invalid":
                    client.loginInvalid();
                    break;
                case "already logged in":
                    client.loginAlreadyOnline();
                    break;
                case "register username free":
                    client.registerUsernameFreeGetPassword();
                    break;
                case "register username taken":
                    client.registerUsernameTaken();
                    break;
                case "show chat list":
                    client.chatListShow(params);
                    break;
                case "chat room exists":
                    client.chatRoomValidDoMenuSelection();
                    break;
                case "chat room invalid":
                    client.chatRoomNameInvalid();
                    break;
                case "show chat room members":
                    client.chatRoomShowMembers(params);
                    break;
                case "member exists in chat room":
                    client.chatMemberDoMenuSelection();
                    break;
                case "chat room member invalid":
                    client.chatMemberInvalid();
                    break;
                case "friend request sent":
                    client.chatMemberFriendRequestSent();
                    break;
                case "already friends":
                    client.chatMemberAlreadyFriends();
                    break;
                case "cannot friend request yourself":
                    client.chatMemberCannotFriendRequestYourself();
                    break;
                case "show profile":
                    client.chatMemberShowProfile(params);
                    break;
                case "run chat room":
                    client.chatRoomRun();
                    break;
                case "exit chat room":
                    client.showMainMenu();
                    break;
                case "show friends list":
                    client.friendsListShow(params);
                    break;
                case "show friend request list":
                    client.friendRequestsShow(params);
                    break;
                case "friend request exists":
                    client.friendRequestsNameValidDoMenuSelection();
                    break;
                case "friend request invalid":
                    client.friendRequestsNameInvalid();
                    break;
                case "show own profile":
                    client.profileShow(params);
                    break;
                default:
                    client.print(request);
                    break;
            }
        }
    }
}
