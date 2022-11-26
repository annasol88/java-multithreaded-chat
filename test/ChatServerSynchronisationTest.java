import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class ChatServerSynchronisationTest {
    private ChatServer server;

    @BeforeEach
    public void setUp() {
        server = new ChatServer(Utils.PORT);
        new Thread(server).start();
    }

    @Test
    public void loginUser_isSynchronized() throws InterruptedException {
        ArrayList<ChatClient> runningClients = new ArrayList<>();
        Runnable loginThread = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(Utils.SERVER_IP, Utils.PORT);
                    ChatClient client = new ChatClient(socket, false);
                    client.tempStorage.put("username", "anna123");
                    client.loginSendRequest("123");
                    runningClients.add(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        int testNumber = 20;
        for (int i = 0; i < testNumber; i++) {
            new Thread(loginThread).start();
        }

        Thread.sleep(1000);

        long loggedInClients = runningClients.stream()
                .map(client -> client.currentScreen).filter( screen -> screen == CurrentClientScreenEnum.MAIN_MENU).count();

        assertEquals(1,loggedInClients);
    }

    @Test
    public void saveUsernameIfFree_isSynchronized() throws InterruptedException {
        server.getData().accounts.clear();

        Runnable sendFriendRequestThread = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(Utils.SERVER_IP, Utils.PORT);
                    ChatClient client = new ChatClient(socket, false);
                    client.registerUsernameEntered("username" + new Random().nextInt());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        int testNumber = 20;
        for (int i = 0; i < testNumber; i++) {
            new Thread(sendFriendRequestThread).start();
        }

        Thread.sleep(2000);

        assertEquals(testNumber, server.getData().accounts.size());
    }

    @Test
    public void addRunningChat_isSynchronized() throws InterruptedException {
        Runnable addChatThread = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(Utils.SERVER_IP, Utils.PORT);
                    ChatClient client = new ChatClient(socket, false);
                    client.testMockCurrentUser();

                    client.tempStorage.put("chat room name", "chat x");
                    client.chatRoomSendEnterRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        int testNumber = 20;
        for (int i = 0; i < testNumber; i++) {
            new Thread(addChatThread).start();
        }

        Thread.sleep(1000);
        assertEquals(testNumber, server.getRunningChats().size());
    }

    @Test
    public void removeRunningChat_isSynchronized() throws InterruptedException {
        ArrayList<ChatClient> runningClients = new ArrayList<>();
        Runnable addChatThread = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(Utils.SERVER_IP, Utils.PORT);
                    ChatClient client = new ChatClient(socket, false);
                    client.testMockCurrentUser();

                    client.tempStorage.put("chat room name", "chat x");
                    client.chatRoomSendEnterRequest();
                    runningClients.add(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        class RemoveClient implements Runnable {
            private ChatClient client;

            public RemoveClient(ChatClient client) {
                this.client = client;
            }

            @Override
            public void run() {
                synchronized (client) {
                    client.chatRoomSendMessage("x");
                }
            }
        }

        int openChatNumber = 20;
        int closedChatNumber = 10;

        for (int i = 0; i < openChatNumber; i++) {
            new Thread(addChatThread).start();
        }
        Thread.sleep(2000);
        for (int i = 0; i < closedChatNumber; i++) {
            new Thread(new RemoveClient(runningClients.get(i))).start();
        }
        Thread.sleep(2000);

        assertEquals(openChatNumber - closedChatNumber, server.getRunningChats().size());
    }

    @Test
    public void sendFriendRequest_isSynchronized() throws InterruptedException {
        Runnable sendFriendRequestThread = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(Utils.SERVER_IP, Utils.PORT);
                    ChatClient client = new ChatClient(socket, false);
                    client.testMockCurrentUser();
                    client.tempStorage.put("chat member name", "emma123");
                    client.chatMemberSendFriendRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        int testNumber = 20;
        for (int i = 0; i < testNumber; i++) {
            new Thread(sendFriendRequestThread).start();
        }

        Thread.sleep(3000);
        long requestsSent = server.getData().accounts.get("emma123").getPendingFriendRequests().size();

        assertEquals(testNumber, requestsSent);
    }

    @Test
    public void editAccountName_isSynchronized() throws InterruptedException {
        Runnable editAccountChatThread = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(Utils.SERVER_IP, Utils.PORT);
                    ChatClient client = new ChatClient(socket, false);
                    client.testMockCurrentUser();
                    client.profileEditName("new name");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        int testNumber = 20;
        for (int i = 0; i < testNumber; i++) {
            new Thread(editAccountChatThread).start();
        }

        Thread.sleep(3000);
        long editedAccounts = server.getData().accounts.values().stream()
                .filter(account -> account.getName().equals("new name")).count();

        assertEquals(testNumber, editedAccounts);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        Thread.sleep(3000);
        server.stop();
    }
}
