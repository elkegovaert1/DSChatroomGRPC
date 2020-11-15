package be.msec.labgrpc;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;

    public User(String name) {
        this.username = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
