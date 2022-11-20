import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ChatServer implements Runnable {
    private final int port;
    private ServerSocket serverSocket = null;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static ServerData data;

    private final List<ChatClientThread> runningChats = Collections.synchronizedList(new ArrayList<>());

    public ChatServer(int port) {
        this.port = port;
        data = new ServerData();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running...");

            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.submit(new ChatClientThread(socket, this));
                    System.out.println("new client connected: " + socket.getPort());
                } catch (IOException e) {
                    System.err.println("Failed to accept client connection");
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Could not connect to port");
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public synchronized void stop() {
        System.out.println("Server closed");
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    /*
     * validates user credentials.
     * No need to synchronize here because we are only reading data and
     * our concurrentHashmap will take care of writes happening at the same time.
     */
    public User VerifyUserCredentials(String username, String password) {
        User user = getAccountByUsername(username);
        //if user exist check password and password correct
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /*
     * synchronizing on user ensures 2 users can't log in at the same time.
     * This is intentionally isolated this from verifying credentials above
     * since it could remove some potential for deadlocks as we are only synchronizing
     * on user when we need to. This also allows us to reuse this when we register a user
     */
    public boolean loginUser(User user) {
        synchronized (user) {
            if (user.isLoggedIn()) {
                return false;
            }
            user.setLoggedIn(true);
            return true;
        }
    }

    public boolean saveUsernameIfFree(String username) {
        if (!accountExists(username)) {
            //adding placeholder to prevent username being taken by another client
            data.accounts.put(username, new User(null, null, null, null));
            return true;
        }
        return false;
    }

    public void registerUser(User user) {
        data.accounts.put(user.getUsername(), user);
    }

    public boolean chatRoomExists(String chatName) {
        return data.chatRooms.get(chatName) != null;
    }

    public boolean accountExists(String username) {
        return data.accounts.get(username) != null;
    }

    public List<ChatRoom> getUserChatRooms(User user) {
        return data.chatRooms.values().stream().filter(n -> n.getMembers().contains(user))
                .collect(Collectors.toList());
    }

    public Collection<User> getChatRoomMembers(String chatName) {
        ChatRoom chat = data.chatRooms.get(chatName);
        if (chat != null) {
            return chat.getMembers().values();
        }
        return null;
    }

    public boolean isUserMemberOfChatroom(String chatRoomName, String username) {
        return getChatRoomMembers(chatRoomName).contains(getAccountByUsername(username));
    }

    public User getAccountByUsername(String username) {
        return data.accounts.get(username);
    }

    public void addToRunningChats(ChatClientThread client) {
        this.runningChats.add(client);
    }

    public void sendMessageToChatRoom(String message, String chatRoom, ChatClientThread senderThread) {
        synchronized (runningChats) {
            for (ChatClientThread clientThread : runningChats) {
                //send message to all clients running the chatroom apart from the sender
                if (
                        clientThread.openChatRoom != null &&
                                clientThread.openChatRoom.equals(chatRoom) &&
                                clientThread != senderThread
                ) {
                    clientThread.output.println(message);
                }
            }
        }
    }

    public void removeRunningChats(ChatClientThread client) {
        this.runningChats.remove(client);
    }

    public void leaveChatRoom(String chatName, String username) {
        data.chatRooms.get(chatName).getMembers().remove(username);
        // destroy chat if empty
        if (data.chatRooms.get(chatName).getMembers().isEmpty()) {
            data.chatRooms.remove(chatName);
        }
    }

    public boolean sendFriendRequest(String requestee, User requester) {
        User requesteeUser = getAccountByUsername(requestee);

        if (data.accounts.get(requester.getUsername()).getFriends().contains(requesteeUser)) {
            return false;
        }
        //the concurrentHashMap will remove duplicate requests so no need to check
        data.accounts.get(requestee).addFriendRequest(requester);
        return true;
    }

    public void acceptFriendRequest(String requester, User requestee) {
        User requesterUser = getAccountByUsername(requester);

        data.accounts.get(requester).addFriend(requestee);
        data.accounts.get(requestee.getUsername()).addFriend((requesterUser));

        removeFriendRequest(requester, requestee);
    }

    public void removeFriendRequest(String requester, User requestee) {
        requestee.removeFriendRequest(getAccountByUsername(requester));
    }

    public void editAccountName(User user, String name) {
        data.accounts.get(user.getUsername()).setName(name);
    }

    public void editAccountBio(User user, String bio) {
        data.accounts.get(user.getUsername()).setBio(bio);
    }

    public void logoutUser(User user) {
        synchronized (user) {
            user.setLoggedIn(false);
        }
    }
}


