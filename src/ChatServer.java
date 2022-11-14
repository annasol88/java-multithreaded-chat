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
    protected final int port;
    protected ServerSocket serverSocket = null;
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
        System.out.println("closed");
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    public User getValidUserFromCredentials(String username, String password) {
        User user = data.accounts.get(username);
        //if user exist check password and password correct
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean loginUser(User user) {
        if (user.isLoggedIn()) {
            return false;
        }
        user.setLoggedIn(true);
        return true;
    }

    public boolean loginSaveUsernameIfFree(String username) {
        if(!accountExists(username)) {
            //adding placeholder to prevent username being taken by another client
            data.accounts.put(username, new User(null, null, null, null));
            return true;
        }
        return false;
    }

    public void registerUserAndLogin(User user) {
        data.accounts.put(user.getUsername(), user);
        user.setLoggedIn(true);
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

    public boolean sendFriendRequest(String requestee, User requestor) {
        User requesteeUser = getAccountByUsername(requestee);

        if(data.accounts.get(requestor.getUsername()).getFriends().contains(requesteeUser)) {
            return false;
        }
        //the concurrentHashMap will remove duplicate requests so no need to check
        data.accounts.get(requestee).addFriendRequest(requestor);
        return true;
    }

    public User getAccountByUsername(String username) {
        return data.accounts.get(username);
    }

    public void leaveChatRoom(String chatName, String username) {
        data.chatRooms.get(chatName).getMembers().remove(username);
        // destroy chat if empty
        if (data.chatRooms.get(chatName).getMembers().isEmpty()) {
            data.chatRooms.remove(chatName);
        }
    }

    public void addToRunningChats(ChatClientThread client) {
        this.runningChats.add(client);
    }

    public void sendToChatRoom(String message, String chatRoom, ChatClientThread senderThread) {
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

    public void acceptFriendRequest(String requester, User requestee) {
        User requesterUser = getAccountByUsername(requester);

        data.accounts.get(requester).addFriend(requestee);
        data.accounts.get(requestee.getUsername()).addFriend((requesterUser));

        removeFriendRequest(requester, requestee);
    }

    public void removeFriendRequest(String requester, User requestee) {
        data.accounts.get(requestee).removeFriendRequest(requestee);
    }

    public void logoutUser(User user) {
        user.setLoggedIn(false);
    }
}


