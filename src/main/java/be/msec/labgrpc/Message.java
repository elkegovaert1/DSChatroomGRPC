package be.msec.labgrpc;

public class Message {
    private String text;
    private String sender;

    public Message(String inhoud, String user) {
        this.text = inhoud;
        this.sender = user;
    }

    @Override
    public String toString() {
        return "["+this.sender+"] " + this.text;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
