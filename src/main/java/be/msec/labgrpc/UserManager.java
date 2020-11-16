package be.msec.labgrpc;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private final List<Message> messages;
    private final List<User> users;

    public UserManager() {
        messages = new ArrayList<>();
        users = new ArrayList<>();
    }

    public void connectUser(String username) {
        User user = new User(username);
        users.add(user);

    }

    public void disconnectUser(String username) {
        users.remove(username);
    }

    public User findUserByName(String username) {
        for (User u: users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public void addToMessages(Message message, Object mutex) {
        synchronized (mutex) {
            try {
                messages.add(message);
                mutex.notifyAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Message getLastMessage(String userName) {
        if (!messages.isEmpty())
            return messages.get(messages.size() - 1);
        else
            return null;
    }


}
