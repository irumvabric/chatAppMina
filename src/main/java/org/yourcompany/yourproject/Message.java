package org.yourcompany.yourproject;

import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        TEXT, IMAGE
    }

    private final String username;
    private final MessageType type;
    private final byte[] content;

    public Message(String username, MessageType type, byte[] content) {
        this.username = username;
        this.type = type;
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }
}