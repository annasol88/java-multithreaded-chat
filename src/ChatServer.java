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
    // For Stress Testing thread pool
    public int testsReceived = 0;

    private final int port;
    private ServerSocket serverSocket = null;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(50);

    private static ServerData data;

    private List<ChatClientThread> runningChats = Collections.synchronizedList(new ArrayList<>());

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
                    System.err.println("server socket connection lost");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("port connection lost");
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public synchronized void stop() {
        System.out.println("Server closed");
        try {
            serverSocket.close();
            threadPool.shutdown();
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close server", e);
        }
    }

    /**
     * checks credentials are correct
     * @return user the password and username belong to or null if they are wrong
     * validates user credentials.
     * No need to synchronize here because we are only reading data and
     * our concurrentHashmap implementation should cause this to wait for any writes happening at the same time.
     */
    public User verifyUserCredentials(String username, String password) {
        User user = getAccountByUsername(username);
        //if user exist check password and password correct
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * logs in a user
     * @return false if a user is already logged in
     * synchronizing on user ensures 2 users can't log in at the same time.
     * This is intentionally isolated this from verifying credentials above
     * to remove some potential for deadlocks as we are only synchronizing
     * on user when we need to and releasing the object as soon as we log in.
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

    /**
     * saves a username when registering
     * @return true if username is free
     * From our understanding of the Java docs putIfAbsent() should synchronize
     * on the conCurrentHashmap object to prevent a race condition from occurring
     * if 2 threads try to save username at once.
     */
    public boolean saveUsernameIfFree(String username) {
        return data.accounts.putIfAbsent(
                username,
                new User(null, null, null, null)) == null;
    }

    /**
     * adds user to data.accounts
     * by design the key is already reserved with a dud user when registering a username
     * which checks if the key is free so we can be confident that this operation is safe
     * from accidental or synchronous overwrites.
     */
    public void registerUser(User user) {
        data.accounts.put(user.getUsername(), user);
    }

    /**
     * @return a list of user chat rooms that @param user is a part of.
     * This is a read only operation hence safe no lock is placed in here.
     */
    public List<ChatRoom> getUserChatRooms(User user) {
        return data.chatRooms.values().stream().filter(n -> n.getMembers().contains(user))
                .collect(Collectors.toList());
    }

    /**
     * @return if a chat room exists in data
     * This is a read only operation hence safe no lock is placed in here.
     */
    public boolean chatRoomExists(String chatName) {
        return getChatRoomByName(chatName) != null;
    }

    /**
     * @return if a user exists in data
     * This is a read only operation hence safe no lock is placed in here.
     */
    public boolean accountExists(String username) {
        return getAccountByUsername(username) != null;
    }

    /**
     * @return a list of users associated to a chatroom
     * This is a read only operation hence safe no lock is placed in here.
     */
    public Collection<User> getChatRoomMembers(String chatName) {
        ChatRoom chat = getChatRoomByName(chatName);
        if (chatRoomExists(chatName)) {
            return chat.getMembers().values();
        }
        return null;
    }

    /**
     * @return if the @param user is a member of @param chatroom
     * This is a read only operation hence safe no lock is placed in here.
     */
    public boolean isUserMemberOfChatroom(String chatRoomName, String username) {
        return getChatRoomMembers(chatRoomName).contains(getAccountByUsername(username));
    }

    /**
     * adds a ChatClientThread to runningChats so that other threads in the same chatroom receive their message
     * while a synchronized list provides serial access to its elements to promote thead safety
     * it does not lock for add operations hence this is done here to prevent synchronisation issues.
     */
    public void addToRunningChats(ChatClientThread client) {
        synchronized(runningChats) {
            runningChats.add(client);
        }
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
        getChatRoomByName(chatName).getMembers().remove(username);
        // destroy chat if empty
        if (getChatRoomByName(chatName).getMembers().isEmpty()) {
            data.chatRooms.remove(chatName);
        }
    }

    public boolean sendFriendRequest(String requestee, User requester) {
        User requesteeUser = getAccountByUsername(requestee);

        if (getAccountByUsername(requester.getUsername()).getFriends().contains(requesteeUser)) {
            return false;
        }
        //the concurrentHashMap will remove duplicate requests so no need to check
        getAccountByUsername(requestee).addFriendRequest(requester);
        return true;
    }

    public void acceptFriendRequest(String requester, User requestee) {
        User requesterUser = getAccountByUsername(requester);

        getAccountByUsername(requester).addFriend(requestee);
        getAccountByUsername(requestee.getUsername()).addFriend((requesterUser));

        removeFriendRequest(requester, requestee);
    }

    public void removeFriendRequest(String requester, User requestee) {
        requestee.removeFriendRequest(getAccountByUsername(requester));
    }

    public void editAccountName(User user, String name) {
        getAccountByUsername(user.getUsername()).setName(name);
    }

    public void editAccountBio(User user, String bio) {
        getAccountByUsername(user.getUsername()).setBio(bio);
    }

    public void logoutUser(User user) {
        synchronized (user) {
            user.setLoggedIn(false);
        }
    }

    public User getAccountByUsername(String username) {
        return data.accounts.get(username);
    }

    public ChatRoom getChatRoomByName(String name) {
        return data.chatRooms.get(name);
    }

    public void receiveTestRequest() {
        synchronized ((Integer) testsReceived) {
            testsReceived += 1;
        }
    }

    public List<ChatClientThread> getRunningChats() {
        return runningChats;
    }
}


