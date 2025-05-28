package com.ecommerce.project.security.response;

public class MessageRespose {
    private String message;
    public MessageRespose(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
