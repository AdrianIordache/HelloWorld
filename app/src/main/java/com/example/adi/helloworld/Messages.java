package com.example.adi.helloworld;

public class Messages
{
    public String message, from, type, data, time;

    Messages()
    {
        message = "Unknown";
        type = "Unknown";
        from = "Unknown";
        data = "Unknown";
        time = "Unknown";
    }

    public Messages(String message, String from, String type, String data, String time)
    {
        this.message = message;
        this.from = from;
        this.type = type;
        this.data = data;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
