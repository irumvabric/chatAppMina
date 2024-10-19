package org.yourcompany.yourproject;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class ChatClient extends JFrame {
    private JTextPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton imageButton;
    private IoSession session;
    private String username;

    public ChatClient(String username) {
        this.username = username;
        setupGUI();
        connectToServer();
    }

//    private void setupGUI() {
//        setTitle("Chat Client - " + username);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setSize(600, 400);
//
//        chatArea = new JTextPane();
//        chatArea.setEditable(false);
//        StyledDocument document = chatArea.getStyledDocument();
//
//        add(new JScrollPane(chatArea), BorderLayout.CENTER);
//
//        JPanel bottomPanel = new JPanel(new BorderLayout());
//        messageField = new JTextField();
//        sendButton = new JButton("Send");
//        imageButton = new JButton("Send Image");
//
//        bottomPanel.add(messageField, BorderLayout.CENTER);
//        bottomPanel.add(sendButton, BorderLayout.EAST);
//        bottomPanel.add(imageButton, BorderLayout.WEST);
//
//        add(bottomPanel, BorderLayout.SOUTH);
//
//        sendButton.addActionListener(e -> sendMessage());
//        imageButton.addActionListener(e -> sendImage());
//        messageField.addActionListener(e -> sendMessage());
//    }


    private void setupGUI() {
        setTitle("Chat Client - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);

        // Set background color for the main panel
        getContentPane().setBackground(new Color(240, 240, 240)); // light gray

        // Styling chat area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(255, 255, 255));  // White background for chat area
        chatArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));  // Add border to chat area
        StyledDocument document = chatArea.getStyledDocument();
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Add a bottom panel with text field and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();

        // Add margin to the text field
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Adding padding inside the text field
        ));

        // Creating the buttons and CardLayout
        JPanel buttonPanel = new JPanel(new CardLayout());

        // Custom Send Message Button with rounded corners and color
        sendButton = new JButton("Send Message") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(46, 204, 113)); // Light green
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Rounded corners
                super.paintComponent(g2d);
                g2d.dispose();
            }

            @Override
            public void setContentAreaFilled(boolean b) {
                // Do nothing to maintain the custom paint
            }
        };
        sendButton.setForeground(Color.BLACK); // White text color
        sendButton.setFocusPainted(false); // Remove focus border
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Add padding inside the button

        // Custom Send Image Button with rounded corners and color
        imageButton = new JButton("Send Image") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(52, 152, 219)); // Light blue
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Rounded corners
                super.paintComponent(g2d);
                g2d.dispose();
            }

            @Override
            public void setContentAreaFilled(boolean b) {
                // Do nothing to maintain the custom paint
            }
        };
        imageButton.setForeground(Color.BLACK); // White text color
        imageButton.setFocusPainted(false); // Remove focus border
        imageButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Add padding inside the button

        buttonPanel.add(imageButton, "Image");
        buttonPanel.add(sendButton, "Send");

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add margin to bottom panel
        add(bottomPanel, BorderLayout.SOUTH);

        CardLayout cardLayout = (CardLayout) buttonPanel.getLayout();

        // Change to send button when user starts typing
        messageField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                cardLayout.show(buttonPanel, "Send");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (messageField.getText().trim().isEmpty()) {
                    cardLayout.show(buttonPanel, "Image");
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not needed for plain text fields
            }
        });

        // Send message or send image based on button press
        sendButton.addActionListener(e -> sendMessage());
        imageButton.addActionListener(e -> sendImage());
        messageField.addActionListener(e -> sendMessage());
    }



    private void connectToServer() {
        NioSocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

        connector.setHandler(new ClientHandler());
        ConnectFuture future = connector.connect(new InetSocketAddress("localhost", 8080));
        future.awaitUninterruptibly();
        session = future.getSession();
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            Message message = new Message(username, Message.MessageType.TEXT, text.getBytes());
            session.write(message);
            appendToPane("You", text, true);
            messageField.setText("");
        }
    }

//    private void sendImage() {
//        JFileChooser fileChooser = new JFileChooser();
//        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//            try {
//                File file = fileChooser.getSelectedFile();
//                byte[] imageData = Files.readAllBytes(file.toPath());
//                Message message = new Message(username, Message.MessageType.IMAGE, imageData);
//                session.write(message);
//                appendToPane("You", "[Sent an image]", true);
//                insertImageToPane(imageData, "You");
//            } catch (Exception e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(this, "Error sending image");
//            }
//        }
//    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                BufferedImage originalImage = ImageIO.read(file);

                // Resize the image (e.g., to 50% of its original size)
                int newWidth = originalImage.getWidth() / 2;
                int newHeight = originalImage.getHeight() / 2;
                Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

                // Convert the resized image back to a BufferedImage
                BufferedImage resizedBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resizedBufferedImage.createGraphics();
                g2d.drawImage(resizedImage, 0, 0, null);
                g2d.dispose();

                // Convert the resized image to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedBufferedImage, "png", baos);
                byte[] imageData = baos.toByteArray();

                // Send the resized image
                Message message = new Message(username, Message.MessageType.IMAGE, imageData);
                session.write(message);
                appendToPane("You", "[Sent an image]", true);
                insertImageToPane(imageData, "You");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error sending image");
            }
        }
    }


    private void appendToPane(String sender, String text, boolean isOwnMessage) {
        try {
            String prefix = isOwnMessage ? "You: " : sender + ": ";
            StyledDocument document = chatArea.getStyledDocument();
            StyleContext styleContext = StyleContext.getDefaultStyleContext();
            Style style = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
            StyleConstants.setForeground(style, isOwnMessage ? Color.BLUE : Color.BLACK);
            StyleConstants.setBold(style, isOwnMessage);
            document.insertString(document.getLength(), prefix + text + "\n", style);
            chatArea.setCaretPosition(document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void insertImageToPane(byte[] imageData, String sender) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image != null) {
                appendToPane(sender, "[Sent an image]", false);
                Style style = chatArea.addStyle("ImageStyle", null);
                StyleConstants.setIcon(style, new ImageIcon(image));
                chatArea.getStyledDocument().insertString(chatArea.getDocument().getLength(), "ignored", style);
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient("User").setVisible(true));
    }

    private class ClientHandler extends IoHandlerAdapter {
        @Override
        public void messageReceived(IoSession session, Object message) {
            if (message instanceof Message) {
                Message msg = (Message) message;
                if (msg.getType() == Message.MessageType.TEXT) {
                    appendToPane(msg.getUsername(), new String(msg.getContent()), false);
                } else if (msg.getType() == Message.MessageType.IMAGE) {
                    insertImageToPane(msg.getContent(), msg.getUsername());
                }
            }
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) {
            cause.printStackTrace();
        }
    }
}