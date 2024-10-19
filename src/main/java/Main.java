import org.yourcompany.yourproject.ChatClient;
import org.yourcompany.yourproject.ChatServer;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Start the server in a new thread
        new Thread(() -> {
            try {
                ChatServer.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Start the first client in a new thread
        new Thread(() -> {
            try {
                ChatClient clientA = new ChatClient("ClientA");
                SwingUtilities.invokeLater(() -> clientA.setVisible(true));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Start the second client in a new thread
        new Thread(() -> {
            try {
                ChatClient clientB = new ChatClient("ClientB");
                SwingUtilities.invokeLater(() -> clientB.setVisible(true));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}