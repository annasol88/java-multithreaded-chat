public class ServerDriver {
    // get this from args (?)
    private static final int PORT = 9876;

    public static void main(String[] args) {
        ChatServer server = new ChatServer(PORT);
        new Thread(server).start();
    }
}
