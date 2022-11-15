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
            String header = Utils.getRequestHeader(request);
            String[] params = Utils.getRequestParams(request);

            if (header.equals("login success")) {
                client.loginSuccess();
            } else if (header.equals("login invalid")) {
                client.loginInvalid();
            } else if (header.equals("already logged in")) {
                client.loginAlreadyOnline();
            } else if (header.equals("register username free")) {
                client.registerUsernameFreeGetPassword();
            } else if (header.equals("register username taken")) {
                client.registerUsernameTaken();
            } else if (header.equals("show chat list")) {
                client.chatListShow(params);
            } else if (header.equals("chat room exists")) {
                client.chatRoomValidDoMenuSelection();
            } else if (header.equals("chat room invalid")) {
                client.chatRoomNameInvalid();
            } else if (header.equals("show chat room members")) {
                client.chatRoomShowMembers(params);
            } else if (header.equals("member exists in chat room")) {
                client.chatMemberDoMenuSelection();
            } else if (header.equals("chat room member invalid")) {
                client.chatMemberInvalid();
            } else if (header.equals("friend request sent")) {
                client.chatMemberFriendRequestSent();
            } else if (header.equals("already friends")) {
                client.chatMemberAlreadyFriends();
            }else if (header.equals("cannot friend request yourself")) {
                client.chatMemberCannotFriendRequestYourself();
            } else if (header.equals("show profile")) {
                client.chatMemberShowProfile(params);
            }  else if (header.equals("run chat room")) {
                client.chatRoomRun();
            } else if (header.equals("exit chat room")) {
                client.showMainMenu();
            } else if (header.equals("show friends list")) {
                client.friendsListShow(params);
            } else if (header.equals("show friend request list")) {
                client.friendRequestsShow(params);
            } else if (header.equals("friend request exists")) {
                client.friendRequestsNameValidDoMenuSelection();
            } else if (header.equals("friend request invalid")) {
                client.friendRequestsNameInvalid();
            } else if (header.equals("show own profile")) {
                client.profileShow(params);
            } else {
                client.print(request);
            }
        }
    }
}
