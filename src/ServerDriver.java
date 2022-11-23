public class ServerDriver {
    public static void main(String[] args) {
        ChatServer server = new ChatServer(Utils.PORT);
        new Thread(server).start();
    }
}
