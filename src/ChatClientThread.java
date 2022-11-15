import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.List;

/*
 * Thread dedicated to listening to user commands from the console and responding with data from the server.
 */
public class ChatClientThread implements Runnable {
    private Socket socket;
    private ChatServer server;
    BufferedReader input;
    PrintWriter output;

    // User currently logged into the client
    User currentUser = null;
    String openChatRoom = null;

    public ChatClientThread(Socket socket, ChatServer server) {
        try {
            this.socket = socket;
            this.server = server;
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            System.err.println("Failed to connect to socket input and output stream.");
            stop();
        }
    }

    @Override
    public void run() {
        try {
            String request = "";
            while (!request.equals("stop")) {
                request = input.readLine().toLowerCase().trim();
                if (request.startsWith("login")) {
                    login(request);
                } else if (request.startsWith("check username free")) {
                    checkUsernameFree(request);
                }  else if (request.startsWith("register user")) {
                    registerUserAndLogin(request);
                } else if (request.startsWith("view chat list")) {
                    getChatRoomNames();
                } else if (request.startsWith("check chat exists")) {
                    validateChatExistsForUser(request);
                } else if (request.startsWith("view chat members")) {
                    viewChatRoomMembers(request);
                } else if (request.startsWith("check chat member exists")) {
                    validateMemberExistsInChatRoom(request);
                } else if (request.startsWith("send friend request")) {
                    sendFriendRequest(request);
                } else if (request.startsWith("view profile")) {
                    getUserProfile(request);
                }else if (request.startsWith("enter chat room")) {
                    enterChatRoom(request);
                } else if (request.startsWith("send message")) {
                    sendMessageToChat(request);
                } else if (request.startsWith("close chat room")) {
                    exitChatRoom();
                } else if (request.startsWith("leave chat room")) {
                    leaveChatRoom(request);
                } else if (request.startsWith("view friends list")) {
                    getFriendList();
                } else if (request.startsWith("view friend requests")) {
                    getFriendRequests();
                } else if (request.startsWith("check friend request exists")) {
                    checkFriendRequestExists(request);
                } else if (request.startsWith("accept friend request")) {
                    acceptFriendRequest(request);
                } else if (request.startsWith("deny friend request")) {
                    denyFriendRequest(request);
                } else if (request.startsWith("view own profile")) {
                    viewOwnProfile();
                }  else if (request.startsWith("edit profile name")) {
                    viewOwnProfile();
                } else if (request.startsWith("edit profile bio")) {
                    viewOwnProfile();
                } else if (request.startsWith("logout")) {
                    logout();
                }
            }
        } catch (IOException e) {
            System.out.println("client connection lost: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        System.out.println("client disconnected: " + socket.getPort());
        if (currentUser != null) {
            logout();
        }
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket input and output streams: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void login(String request) {
        String s = request.replace("login:", "").trim();
        String[] credentials = s.split(",");
        String username = credentials[0];
        String password = credentials[1];

        User user = server.getValidUserFromCredentials(username, password);

        if (user == null) {
            output.println("login invalid");
            return;
        }
        if (server.loginUser(user)) {
            currentUser = user;
            output.println("login success");
            return;
        }
        output.println("already logged in");
    }

    private void checkUsernameFree(String request) {
        String username = request.replace("check username free:", "").trim();
        if (server.loginSaveUsernameIfFree(username)) {
            output.println("register username free");
            return;
        }

        output.println("register username taken");
    }

    private void registerUserAndLogin(String request) {
        request = request.replace("register user:", "").trim();
        String[] params = request.split(",");
        String username = params[0];
        String password = params[1];
        String name = params[2];
        String bio = params[3];

        User user = new User(name, bio, username, password);

        server.registerUserAndLogin(user);
        currentUser = user;
        output.println("login success");
    }

    private void getChatRoomNames() {
        List<ChatRoom> chats = server.getUserChatRooms(currentUser);
        StringBuilder chatsString = new StringBuilder();
        for (ChatRoom chat : chats) {
            chatsString.append(chat.getName()).append(",");
        }
        output.println("show chat list:" + chatsString);
    }

    private void validateChatExistsForUser(String request) {
        String chatRoomName = request.replace("check chat exists:", "").trim();

        if (server.chatRoomExists(chatRoomName) && server.isUserMemberOfChatroom(chatRoomName, currentUser.getUsername())) {
            output.println("chat room exists");
        } else {
            output.println("chat room invalid");
        }
    }

    private void viewChatRoomMembers(String request) {
        String chatRoomName = request.replace("view chat members:", "").trim();
        Collection<User> members = server.getChatRoomMembers(chatRoomName);
        StringBuilder chatsString = new StringBuilder();
        for (User member : members) {
            chatsString.append(member.getUsername()).append(",");
        }
        output.println("show chat room members:" + chatsString);
    }

    private void validateMemberExistsInChatRoom(String request) {
        request = request.replace("check chat member exists:", "").trim();
        String[] params = request.split(",");
        String chatRoom = params[0];
        String username = params[1];

        if (server.accountExists(username) && server.isUserMemberOfChatroom(chatRoom, username)) {
            output.println("member exists in chat room");
        } else {
            output.println("chat room member invalid");
        }
    }

    private void sendFriendRequest(String request) {
        String username = request.replace("send friend request:", "").trim();
        if(username.equals(currentUser.getUsername())) {
            output.println("cannot friend request yourself");
            return;
        }
        if(server.sendFriendRequest(username, currentUser)){
            output.println("friend request sent");
            return;
        }
        output.println("already friends");
    }

    private void getUserProfile(String request) {
        String username = request.replace("view profile:", "").trim();
        User user = server.getAccountByUsername(username);
        String userString = user.getUsername() + "," + user.getName() + "," + user.getBio();
        output.println("show profile:" + userString);
    }

    private void enterChatRoom(String request) {
        openChatRoom = request.replace("enter chat room:", "").trim();
        server.addToRunningChats(this);
        output.println("run chat room");
    }

    private void leaveChatRoom(String request) {
        String chatName = request.replace("leave chat room:", "").trim();
        server.leaveChatRoom(chatName, currentUser.getUsername());
    }

    private void sendMessageToChat(String request) {
        String message = request.replace("send message:", "").trim();
        if (!message.equals("")) {
            message = currentUser.getName() + ": " + message;
            server.sendToChatRoom(message, openChatRoom, this);
        }
    }

    private void getFriendList() {
        Collection<User> friends = currentUser.getFriends();
        StringBuilder friendsString = new StringBuilder();
        for (User friend : friends) {
            friendsString.append(friend.getName()).append(",");
        }
        output.println("show friends list:" + friendsString);
    }

    private void getFriendRequests() {
        Collection<User> friends = currentUser.getPendingFriendRequests();
        StringBuilder friendsString = new StringBuilder();
        for (User friend : friends) {
            friendsString.append(friend.getUsername()).append(",");
        }
        output.println("show friend request list:" + friendsString);
    }

    private void checkFriendRequestExists(String request) {
        String username = request.replace("check friend request exists:", "").trim();
        if(server.accountExists(username) && currentUser.getPendingFriendRequests().contains(server.getAccountByUsername(username))) {
            output.println("friend request exists");
        }
        else {
            output.println("friend request invalid");
        }
    }

    private void acceptFriendRequest(String request) {
        String username = request.replace("accept friend request:", "").trim();
        server.acceptFriendRequest(username, currentUser);
    }

    private void denyFriendRequest(String request) {
        String username = request.replace("deny friend request:", "").trim();
        server.removeFriendRequest(username, currentUser);
    }

    private void viewOwnProfile() {
        output.println("show own profile:" + currentUser.getName() + "," + currentUser.getBio());
    }

    private void editProfileName(String request) {
        String name = request.replace("edit profile name:", "").trim();
        server.editAccountName(currentUser, name);
    }

    private void editProfileBio(String request) {
        String bio = request.replace("edit profile bio:", "").trim();
        server.editAccountBio(currentUser, bio);
    }

    private void exitChatRoom() {
        openChatRoom = null;
        server.removeRunningChats(this);
    }

    private void logout() {
        server.logoutUser(currentUser);
        currentUser = null;
    }

}
