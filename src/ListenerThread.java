import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ListenerThread implements Runnable {
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;

    private boolean stopped = false;

    public ListenerThread(Socket socket, ChatClient client) {
        this.socket = socket;
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
                handleServerInput(request);
            }
        } catch (IOException e) {
            stop();
        }
    }

    public void stop() {
        try {
            this.stopped = true;
            reader.close();
        } catch (IOException ex) {
            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }

    private void handleServerInput(String request) {
        if (request != null) {
            request = request.toLowerCase().trim();

            if (request.startsWith("login success")) {
                client.loginSuccess();
            } else if (request.startsWith("login invalid")) {
                client.loginInvalid();
            } else if (request.startsWith("already logged in")) {
                client.loginAlreadyOnline();
            } else if (request.startsWith("register username free")) {
                client.registerUsernameFreeGetPassword();
            } else if (request.startsWith("register username taken")) {
                client.registerUsernameTaken();
            } else if (request.startsWith("show chat list")) {
                client.chatListShow(request);
            } else if (request.startsWith("chat room exists")) {
                client.chatRoomValidDoMenuSelection();
            } else if (request.startsWith("chat room invalid")) {
                client.chatRoomNameInvalid();
            } else if (request.contains("show chat room members")) {
                client.chatRoomShowMembers(request);
            } else if (request.startsWith("member exists in chat room")) {
                client.chatMemberDoMenuSelection();
            } else if (request.startsWith("chat room member invalid")) {
                client.chatMemberInvalid();
            } else if (request.startsWith("friend request sent")) {
                client.chatMemberFriendRequestSent();
            } else if (request.startsWith("already friends")) {
                client.chatMemberAlreadyFriends();
            }else if (request.startsWith("cannot friend request yourself")) {
                client.chatMemberCannotFriendRequestYourself();
            } else if (request.startsWith("show profile")) {
                client.chatMemberShowProfile(request);
            }  else if (request.startsWith("run chat room")) {
                client.chatRoomRun();
            } else if (request.startsWith("exit chat room")) {
                client.showMainMenu();
            } else if (request.startsWith("show friends list")) {
                client.friendsListShow(request);
            } else if (request.startsWith("show friend request list")) {
                client.friendRequestsShow(request);
            } else if (request.startsWith("friend request exists")) {
                client.friendRequestsNameValidDoMenuSelection();
            } else if (request.startsWith("friend request invalid")) {
                client.friendRequestsNameInvalid();
            } else {
                client.print(request);
            }
        }
    }
}
