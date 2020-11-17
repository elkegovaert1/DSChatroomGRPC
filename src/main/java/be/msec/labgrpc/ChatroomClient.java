package be.msec.labgrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatroomClient {
    private User user;
    private List<User> onlineUsers;
    private final ManagedChannel channel;
    private final ServerGrpc.ServerStub asyncStub;
    private final ServerGrpc.ServerBlockingStub blockingStub;

    public static ObservableList<String> messages;
    public static ObservableList<String> privateMessages;
    public static ObservableList<PriveGesprek> privegesprekken;

    public ChatroomClient(String host, int port){
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    public ChatroomClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = ServerGrpc.newBlockingStub(channel);
        asyncStub = ServerGrpc.newStub(channel);
        messages = FXCollections.observableArrayList();
        privateMessages = FXCollections.observableArrayList();
        privegesprekken = FXCollections.observableArrayList();
        onlineUsers = new ArrayList<>();
    }

    public void connectUser(String username) {
        Username name = Username.newBuilder().setName(username).build();
        boolean connected = blockingStub.connectUser(name).getIsConnected();
        System.out.println("Connected: " + connected);
        user = new User(username);
        Platform.runLater(() -> sendMessages("joined chat." ));
        Platform.runLater(() -> getOnlineUsers());
        getNewMessages();
    }

    public void sendMessages(String text) {
        MessageText msgtxt = MessageText.newBuilder()
                .setText("[" + user.getUsername() + "] " + text)
                .setSender(user.getUsername()).build();
        System.out.println("Broadcasting... " + msgtxt.getText());
        blockingStub.sendMessages(msgtxt);

    }

    public void sendPrivateMessage(String text, String receiver) {
        for (PriveGesprek pg: privegesprekken) {
            if (pg.getPartner().equals(receiver)) {
                Platform.runLater(() -> pg.addBericht("["+user.getUsername()+"] "+text));
            }
        }
        MessageText msgtxt = MessageText.newBuilder()
                .setText("[" + user.getUsername() + "] " + text)
                .setSender(user.getUsername()).build();
        PrivateMessageText pmt = PrivateMessageText.newBuilder()
                .setMessageText(msgtxt)
                .setReceiver(receiver).build();

        System.out.println("Sending private message... " + msgtxt.getText());
        blockingStub.sendPrivateMsg(pmt);
    }

    public void getNewMessages() {
        StreamObserver<MessageText> observer = new StreamObserver<MessageText>() {
            @Override
            public void onNext(MessageText value) {
                System.out.println("Message received from: " + value.getSender());
                placeInRightMessageList(value.getText(), value.getSender());
            }
            @Override
            public void onError(Throwable t) {}
            @Override
            public void onCompleted() {}
        };
        asyncStub.getMessages(Username.newBuilder().setName(user.getUsername()).build(), observer);
    }

    public void getOnlineUsers() {
        StreamObserver<Username> observer = new StreamObserver<Username>() {
            @Override
            public void onNext(Username value) {
                User u = new User(value.getName());
                onlineUsers.add(u);
                generatePriveBerichten(u);
            }

            @Override
            public void onError(Throwable t) {}
            @Override
            public void onCompleted() {}
        };
        asyncStub.getOnlineUsers(Empty.newBuilder().build(), observer);
    }

    public void generatePriveBerichten(User u) {
        if (!user.getUsername().equals(u.getUsername())) {
            boolean bevat = false;
            for (PriveGesprek pg: privegesprekken) {
                if (pg.getPartner().equals(u.getUsername())) {
                    bevat = true;
                    break;
                }
            }
            if (!bevat) {
                PriveGesprek pg = new PriveGesprek(this, u.getUsername());
                privegesprekken.add(pg);
                System.out.println("Added privegesprek: " + pg.getPartner());
            }
        }

    }

    public void placeInRightMessageList(String text, String sender) {
        String [] split = text.split(":");
        StringBuilder sb = new StringBuilder();
        if (split[0].equals("BROAD")) {
            for (int i = 1; i < split.length; i++) {
                sb.append(split[i]);
            }
            Platform.runLater(() -> messages.add(sb.toString()));
        }
        else {
            if (split[1].equals(user.getUsername())) {
                for (int i = 2; i < split.length; i++) {
                    sb.append(split[i]);
                }
                for (PriveGesprek pg: privegesprekken) {
                    if (pg.getPartner().equals(sender)) {
                        Platform.runLater(() -> pg.addBericht(sb.toString()));

                        break;
                    }
                }
            }
        }
    }

    public void stopUser() throws InterruptedException {
        Username username = Username.newBuilder().setName(user.getUsername()).build();
        blockingStub.disconnectUser(username);
        sendMessages("left the chat.");
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static ObservableList<PriveGesprek> getPrivegesprekken() {
        return privegesprekken;
    }

    public static void setPrivegesprekken(ObservableList<PriveGesprek> privegesprekken) {
        ChatroomClient.privegesprekken = privegesprekken;
    }
}
