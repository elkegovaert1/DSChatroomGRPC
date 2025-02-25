package be.msec.labgrpc;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private final List<Message> messages;
    private final List<String> users;

    public UserManager() {
        messages = new ArrayList<>();
        users = new ArrayList<>();
    }

    public void connectUser(String username, Object mutex) throws UsernameAlreadyExistsException {
        synchronized (mutex) {
            if (users.contains(username)) {
                throw new UsernameAlreadyExistsException(username);
            } else {
                users.add(username);
                mutex.notifyAll();
            }
        }
    }

    public void disconnectUser(String username, Object mutex) {
        synchronized(mutex) {
            users.remove(username);
            mutex.notifyAll();
        }
    }

    public String findUserByName(String username) {
        for (String u: users) {
            if (u.equals(username)) {
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

    public Message getLastMessage() {
        if (!messages.isEmpty())
            return messages.get(messages.size() - 1);
        else
            return null;
    }

    public String getNewUser() {
        return users.get(users.size()-1);
    }

    public List<String> getOnlineUsers() {
        return users;
    }


}
