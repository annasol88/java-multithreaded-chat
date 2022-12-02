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
        return data.accounts.put(
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
        data.accounts.putIfAbsent(user.getUsername(), user);
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
            return chat.getMembers();
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
     * adds a ChatClientThread to runningChats so that other threads in the same chatroom receive their message.
     * synchronizedList implementation ensures this operation is thread safe
     */
    public void addRunningChat(ChatClientThread client) {
        runningChats.add(client);
    }

    /**
     * sends the @param message to everyone currently running @param chatroom.
     * While a synchronized list provides synchronized access to elements, it is suggested in the documentation
     * that atomic access does not guarantee thread safety on iterations of it, therefore it is synchronized here, to prevent
     * parallel access issues if the array is modified while being traversed.
     * It was also considered that if the application supported saving messages in a chatroom in future, this would need
     * to lock on the current chatroom object being written to, and consideration for deadlocks would need to be taken.
     */
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

    /**
     * removes the ChatClientThread from runningChats when a user closes a chat room.
     * synchronizedList implementation ensures this operation is thread safe
     */
    public void removeRunningChat(ChatClientThread client) {
        this.runningChats.remove(client);
    }

    /**
     * removes @param member from a chat room provided by @param chatName.
     * and destroys the chatroom if it is empty.
     * ConcurrentHashMap will ensure the remove operations for the chat member and chatroom are
     * synchronous hence thread safe.
     */
    public void leaveChatRoom(String chatName, String username) {
        ChatRoom chat = getChatRoomByName(chatName);
        chat.removeMember(username);
        // destroy chat if empty
        if (chat.getMembers().isEmpty()) {
            data.chatRooms.remove(chatName);
        }
    }

    /**
     * sends a friend request to the @param requestee.
     * checking if the users are already friends is a read-only operation
     * hence it's intentionally done without any lock because it would be unnecessary.
     * the ConcurrentHashmap implementation for user friendRequests will provide concurrent access when a new request is added.
     * With the addition of admin functionality a user could get deleted mid operation,
     * hence a synchronized lock should be placed on both the requesteeUser and requestorUser to make this thread safe,
     * with requesterUser locked around the if statement and requesteeUser locked around the add operation to mitigate
     * the possibility of deadlocks with nested locks.
     */
    public boolean sendFriendRequest(String requestee, String requester) {
        User requesterUser = getAccountByUsername(requester);
        User requesteeUser = getAccountByUsername(requestee);

        if (requesterUser.getFriends().contains(requesteeUser)) {
            return false;
        }
        // ConcurrentHashMap will overwrite duplicate requests so no need to check
        requesteeUser.addFriendRequest(requesterUser);
        return true;
    }

    public void acceptFriendRequest(String requester, String requestee) {
        User requesterUser = getAccountByUsername(requester);
        User requesteeUser = getAccountByUsername(requestee);

        requesterUser.addFriend(requesteeUser);
        requesteeUser.addFriend(requesterUser);

        removeFriendRequest(requester, requestee);
        // if exists
        removeFriendRequest(requestee, requester);
    }

    public void removeFriendRequest(String requester, String requestee) {
        User requesterUser = getAccountByUsername(requester);
        User requesteeUser = getAccountByUsername(requestee);

        requesteeUser.removeFriendRequest(requesterUser);
    }

    /**
     * updates the @param user's name to @param name
     * this request will never be made in parallel since the application only supports one user having access to modify their account at a time
     * however adding admin functionality would require a synchronized lock on the user prevent parallel access issues.
     */
    public void editAccountName(User user, String name) {
        getAccountByUsername(user.getUsername()).setName(name);
    }

    /**
     * updates the @param user's bio to @param bio
     * this request will never be made in parallel since the application only supports one user having access to modify their account at a time
     * however adding admin functionality would require a synchronized lock on the user prevent parallel access issues.
     */
    public void editAccountBio(User user, String bio) {
        getAccountByUsername(user.getUsername()).setBio(bio);
    }

    /**
     * logs out the @param user
     * since the application only supports one user being logged in at a given time this request will never be made in parallel
     * however if admin functionality was integrated into the application, this would need a synchronized lock
     * to prevent parallel access issues.
     */
    public void logoutUser(User user) {
        user.setLoggedIn(false);
    }

    /**
     * @return the user associated with the @param username
     * readonly access which doesn't require a lock.
     * by design, extracted from functions where one user is modified to remove bottle necks from locking on the
     * entire account object.
     * with the addition of admin functionality, functions calling this will need to lock on the user object returned
     * to prevent synchronization issues with the user being deleted mid modification.
     */
    public User getAccountByUsername(String username) {
        return data.accounts.get(username);
    }

    /**
     * @return the chatroom associated with the @param chatName
     * readonly access which doesn't require a lock.
     * by design, extracted from functions where one chat room is modified to remove bottle necks from locking on the
     * entire chatRooms object.
     */
    public ChatRoom getChatRoomByName(String name) {
        return data.chatRooms.get(name);
    }

    //  For Testing - to give access to different properties in the server

    public void receiveTestRequest() {
        synchronized ((Integer) testsReceived) {
            testsReceived += 1;
        }
    }

    public void addTestUserAccount(User user) {
        data.accounts.putIfAbsent(user.getUsername(), user);
    }

    public List<ChatClientThread> getRunningChats() {
        return runningChats;
    }

    public ServerData getData() {
        return data;
    }
}


