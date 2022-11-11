public class ServerDriver {
    // get this from args (?)
    private static final int PORT = 6969;
    private static final String SERVER_IP = "localhost";

    public static void main(String[] args) {
        ChatServer server = new ChatServer(PORT, SERVER_IP);
        new Thread(server).start();
    }
}
