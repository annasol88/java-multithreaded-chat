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
                request = input.readLine();
                String header = Utils.getRequestHeader(request);
                String[] params = Utils.getRequestParams(request);

                if (header.equals("login")) {
                    login(params);
                } else if (header.equals("check username free")) {
                    checkUsernameFree(params[0]);
                } else if (header.equals("register user")) {
                    registerUserAndLogin(params);
                } else if (header.equals("view chat list")) {
                    getChatRoomNames();
                } else if (header.equals("check chat exists")) {
                    validateChatExistsForUser(params[0]);
                } else if (header.equals("view chat members")) {
                    viewChatRoomMembers(params[0]);
                } else if (header.equals("check chat member exists")) {
                    validateMemberExistsInChatRoom(params);
                } else if (header.equals("send friend request")) {
                    sendFriendRequest(params[0]);
                } else if (header.equals("view profile")) {
                    getUserProfile(params[0]);
                } else if (header.equals("enter chat room")) {
                    enterChatRoom(params[0]);
                } else if (header.equals("send message")) {
                    sendMessageToChat(params[0]);
                } else if (header.equals("close chat room")) {
                    exitChatRoom();
                } else if (header.equals("leave chat room")) {
                    leaveChatRoom(params[0]);
                } else if (header.equals("view friends list")) {
                    getFriendList();
                } else if (header.equals("view friend requests")) {
                    getFriendRequests();
                } else if (header.equals("check friend request exists")) {
                    checkFriendRequestExists(params[0]);
                } else if (header.equals("accept friend request")) {
                    acceptFriendRequest(params[0]);
                } else if (header.equals("deny friend request")) {
                    denyFriendRequest(params[0]);
                } else if (header.equals("view own profile")) {
                    viewOwnProfile();
                } else if (header.equals("edit profile name")) {
                    editProfileName(params[0]);
                } else if (header.equals("edit profile bio")) {
                    editProfileBio(params[0]);
                } else if (header.equals("logout")) {
                    logout();
                } else {
                    output.println("Error: request not recognised: " + request);
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

    private void login(String[] credentials) {
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

    private void checkUsernameFree(String username) {
        if (server.loginSaveUsernameIfFree(username)) {
            output.println("register username free");
            return;
        }
        output.println("register username taken");
    }

    private void registerUserAndLogin(String[] account) {
        String username = account[0];
        String password = account[1];
        String name = account[2];
        String bio = account[3];

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

    private void validateChatExistsForUser(String chatName) {
        if (server.chatRoomExists(chatName) && server.isUserMemberOfChatroom(chatName, currentUser.getUsername())) {
            output.println("chat room exists");
        } else {
            output.println("chat room invalid");
        }
    }

    private void viewChatRoomMembers(String chatName) {
        Collection<User> members = server.getChatRoomMembers(chatName);
        StringBuilder chatsString = new StringBuilder();
        for (User member : members) {
            chatsString.append(member.getUsername()).append(",");
        }
        output.println("show chat room members:" + chatsString);
    }

    private void validateMemberExistsInChatRoom(String[] params) {
        String chatRoom = params[0];
        String username = params[1];

        if (server.accountExists(username) && server.isUserMemberOfChatroom(chatRoom, username)) {
            output.println("member exists in chat room");
        } else {
            output.println("chat room member invalid");
        }
    }

    private void sendFriendRequest(String requestUsername) {
        if (requestUsername.equals(currentUser.getUsername())) {
            output.println("cannot friend request yourself");
            return;
        }
        if (server.sendFriendRequest(requestUsername, currentUser)) {
            output.println("friend request sent");
            return;
        }
        output.println("already friends");
    }

    private void getUserProfile(String username) {
        User user = server.getAccountByUsername(username);
        String userString = user.getUsername() + "," + user.getName() + "," + user.getBio();
        output.println("show profile:" + userString);
    }

    private void enterChatRoom(String chatName) {
        openChatRoom = chatName;
        server.addToRunningChats(this);
        output.println("run chat room");
    }

    private void sendMessageToChat(String message) {
        if (!message.equals("")) {
            message = currentUser.getName() + ": " + message;
            server.sendToChatRoom(message, openChatRoom, this);
        }
    }

    private void exitChatRoom() {
        openChatRoom = null;
        server.removeRunningChats(this);
    }

    private void leaveChatRoom(String chatName) {
        server.leaveChatRoom(chatName, currentUser.getUsername());
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

    private void checkFriendRequestExists(String requestUsername) {
        if (server.accountExists(requestUsername) && currentUser.getPendingFriendRequests().contains(server.getAccountByUsername(requestUsername))) {
            output.println("friend request exists");
        } else {
            output.println("friend request invalid");
        }
    }

    private void acceptFriendRequest(String username) {
        server.acceptFriendRequest(username, currentUser);
    }

    private void denyFriendRequest(String username) {
        server.removeFriendRequest(username, currentUser);
    }

    private void viewOwnProfile() {
        output.println("show own profile:" + currentUser.getName() + "," + currentUser.getBio());
    }

    private void editProfileName(String name) {
        server.editAccountName(currentUser, name);
    }

    private void editProfileBio(String bio) {
        server.editAccountBio(currentUser, bio);
    }

    private void logout() {
        server.logoutUser(currentUser);
        currentUser = null;
    }
}
