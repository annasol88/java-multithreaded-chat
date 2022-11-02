public class ServerRunner {
    // get this from args (?)
    private static final int PORT = 9876;

    public static void main(String[] args) {
        ChatServer server = new ChatServer(9876);
        new Thread(server).start();
    }
}
