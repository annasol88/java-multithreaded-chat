import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class ConsoleListenerThread implements Runnable {
    private ChatClient client;
    private BufferedReader userInput;
    private boolean stopped = false;

    public ConsoleListenerThread(ChatClient client, BufferedReader userInput) {
        this.client = client;
        this.userInput = userInput;
    }

    @Override
    public void run() {
        while (!stopped){
            try {
                String input = userInput.readLine();
                handleUserInput(input);
            }
            catch (IOException e) {
                System.err.println("failed to read user input");
            }
        }
    }

    public void stop() {
        try {
            this.stopped = true;
            this.userInput.close();
        } catch (IOException ex) {

            System.out.println("Server Connection Lost: " + ex.getMessage());
        }
    }

    private void handleUserInput(String input) {
        switch(client.currentScreen) {
            case LOGIN:
                client.handleLoginMenuSelection(input);
                break;
            case MAIN:
                client.handleMainMenuSelection(input);
                break;
            case CHATROOM:
                client.sendMessageToChat(input);
                break;
            case ENTERING_CHATROOM:
                client.enterChatRoom(input);
                break;
        }
    }
}
