package be.msec.labgrpc;

public class Message {
    private String text;
    private User sender;

    public Message(String inhoud, User user) {
        this.text = inhoud;
        this.sender = user;
    }

    @Override
    public String toString() {
        return "["+this.sender.getUsername()+"] " + this.text;
    }

    public User getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }
}
