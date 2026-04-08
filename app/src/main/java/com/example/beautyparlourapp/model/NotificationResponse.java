package com.example.beautyparlourapp.model;

public class NotificationResponse {
    private boolean success;
    private String error;
    private Object messageId;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Object getMessageId() { return messageId; }
    public void setMessageId(Object messageId) { this.messageId = messageId; }
}