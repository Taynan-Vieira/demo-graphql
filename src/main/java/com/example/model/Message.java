package com.example.model;

import java.time.LocalDateTime;

public class Message {
    private final String id;
    private final String content;
    private final String timestamp;

    public Message(String id, String content) {
        this.id = id;
        this.content = content;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }
}