import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServerSynchronisationTest {
    private ChatServer server;
    private ServerData data;

    @BeforeEach
    public void setUp() {
        server = new ChatServer(Utils.PORT);
        data = new ServerData();

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

        int testNumber = 5;
        for (int i = 0; i < testNumber; i++) {
            new Thread(loginThread).start();
        }

        Thread.sleep(1000);

        long loggedInClients = runningClients.stream()
                .map(client -> client.currentScreen).filter( screen -> screen == CurrentClientScreenEnum.MAIN_MENU).count();

        assertEquals(loggedInClients, 1);
    }

    @Test
    public void addToRunningChats_isSynchronized() throws InterruptedException {
        ArrayList<ChatClient> runningClients = new ArrayList<>();
        Runnable addChatThread = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(Utils.SERVER_IP, Utils.PORT);
                    ChatClient client = new ChatClient(socket, false);
                    client.tempStorage.put("chat room name", "chat x");
                    client.chatRoomSendEnterRequest();
                    runningClients.add(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        int testNumber = 5;
        for (int i = 0; i < testNumber; i++) {
            new Thread(addChatThread).start();
        }

        Thread.sleep(1000);
        assertEquals(server.getRunningChats().size(), testNumber);

    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        Thread.sleep(2000);
        server.stop();
    }
}
