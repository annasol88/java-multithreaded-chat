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
    private BufferedReader input;
    PrintWriter output;

    // User currently logged into the client
    private User currentUser = null;
    // Chat room that is currently running or null if the user is not running a chat
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

                switch (header) {
                    case "login":
                        login(params);
                        break;
                    case "check username free":
                        checkUsernameFree(params[0]);
                        break;
                    case "register user":
                        registerUserAndLogin(params);
                        break;
                    case "view chat list":
                        getChatRoomNames();
                        break;
                    case "check chat exists":
                        validateChatExistsForUser(params[0]);
                        break;
                    case "view chat members":
                        viewChatRoomMembers(params[0]);
                        break;
                    case "check chat member exists":
                        validateMemberExistsInChatRoom(params);
                        break;
                    case "send friend request":
                        sendFriendRequest(params[0]);
                        break;
                    case "view profile":
                        getUserProfile(params[0]);
                        break;
                    case "enter chat room":
                        enterChatRoom(params[0]);
                        break;
                    case "send message":
                        sendMessageToChat(params[0]);
                        break;
                    case "close chat room":
                        exitChatRoom();
                        break;
                    case "leave chat room":
                        leaveChatRoom(params[0]);
                        break;
                    case "view friends list":
                        getFriendList();
                        break;
                    case "view friend requests":
                        getFriendRequests();
                        break;
                    case "check friend request exists":
                        checkFriendRequestExists(params[0]);
                        break;
                    case "accept friend request":
                        acceptFriendRequest(params[0]);
                        break;
                    case "deny friend request":
                        denyFriendRequest(params[0]);
                        break;
                    case "view own profile":
                        viewOwnProfile();
                        break;
                    case "edit profile name":
                        editProfileName(params[0]);
                        break;
                    case "edit profile bio":
                        editProfileBio(params[0]);
                        break;
                    case "logout":
                        logout();
                        break;
                    case "stop":
                        break;
                    case "test":
                        test();
                        break;
                    case "test mock user":
                        testMockCurrentUser(params);
                        break;
                    default:
                        output.println("Error: request not recognised: " + request);
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("client connection lost: " + e.getMessage());
        } finally {
            stop();
        }
    }

    private void login(String[] credentials) {
        String username = credentials[0];
        String password = credentials[1];

        User user = server.verifyUserCredentials(username, password);

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
        if (server.saveUsernameIfFree(username)) {
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

        server.registerUser(user);
        server.loginUser(user);
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
        if (server.sendFriendRequest(requestUsername, currentUser.getUsername())) {
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
        server.addRunningChat(this);
        server.sendMessageToChatRoom(currentUser.getName() + " has entered the chat.", openChatRoom, this);
        output.println("run chat room");
    }

    private void sendMessageToChat(String message) {
        if (!message.equals("")) {
            message = currentUser.getName() + ": " + message;
            server.sendMessageToChatRoom(message, openChatRoom, this);
        }
    }

    private void exitChatRoom() {
        server.sendMessageToChatRoom(currentUser.getName() +" has closed the chat.", openChatRoom, this);
        openChatRoom = null;
        server.removeRunningChat(this);
    }

    private void leaveChatRoom(String chatName) {
        server.sendMessageToChatRoom(currentUser.getName() +" has left the chat.", chatName, this);
        server.leaveChatRoom(chatName, currentUser.getUsername());
    }

    private void getFriendList() {
        Collection<User> friends = currentUser.getFriends();
        StringBuilder friendsString = new StringBuilder();
        for (User friend : friends) {
            friendsString.append(friend.getUsername()).append(",");
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
        server.acceptFriendRequest(username, currentUser.getUsername());
    }

    private void denyFriendRequest(String username) {
        server.removeFriendRequest(username, currentUser.getUsername());
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

    private void stop() {
        System.out.println("client disconnected: " + socket.getPort());
        if (currentUser != null) {
            logout();
        }
    }

    // For Testing
    private void test() {
        server.receiveTestRequest();
    }

    private void testMockCurrentUser(String[] params) {
        String username = params[0];
        String password = params[1];
        String name = params[2];
        String bio = params[3];

        User user = new User(name, bio, username, password);
        currentUser = user;
        server.addTestUserAccount(user);
    }
}
