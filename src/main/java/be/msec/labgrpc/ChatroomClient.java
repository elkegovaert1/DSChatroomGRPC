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
    private String user;
    private List<String> onlineUsers;
    private final ManagedChannel channel;
    private final ServerGrpc.ServerStub asyncStub;
    private final ServerGrpc.ServerBlockingStub blockingStub;
    private boolean isConnected;

    public static ObservableList<String> messages;
    public static ObservableList<PriveGesprek> privegesprekken;

    public ChatroomClient(String host, int port){
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    public ChatroomClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = ServerGrpc.newBlockingStub(channel);
        asyncStub = ServerGrpc.newStub(channel);
        messages = FXCollections.observableArrayList();
        messages.add("[Server] Welcome to our Chatroom!");
        privegesprekken = FXCollections.observableArrayList();
        onlineUsers = new ArrayList<>();
        isConnected = false;
    }

    public boolean checkName(String username) {
        Username name = Username.newBuilder().setName(username).build();
        isConnected = blockingStub.connectUser(name).getIsConnected();
        return isConnected;
    }

    public void connectUser(String username) {
        this.user = username;
        Platform.runLater(() -> sendMessages("joined chat." ));
        Platform.runLater(() -> getOnlineUsers());
        getNewMessages();
        notifyDisconnectedUser();
    }

    public void sendMessages(String text) {
        MessageText msgtxt = MessageText.newBuilder()
                .setText("[" + user + "] " + text)
                .setSender(user).build();
        System.out.println("Broadcasting... " + msgtxt.getText());
        blockingStub.sendMessages(msgtxt);

    }

    public void sendPrivateMessage(String text, String receiver) {
        for (PriveGesprek pg: privegesprekken) {
            if (pg.getPartner().equals(receiver)) {
                Platform.runLater(() -> pg.addBericht("["+user+"] "+text));
            }
        }
        MessageText msgtxt = MessageText.newBuilder()
                .setText("[" + user + "] " + text)
                .setSender(user).build();
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
        asyncStub.getMessages(Username.newBuilder().setName(user).build(), observer);
    }

    public void getOnlineUsers() {
        StreamObserver<Username> observer = new StreamObserver<Username>() {
            @Override
            public void onNext(Username value) {
                onlineUsers.add(value.getName());
                generatePriveBerichten(value.getName());
            }

            @Override
            public void onError(Throwable t) {}
            @Override
            public void onCompleted() {}
        };
        asyncStub.getOnlineUsers(Empty.newBuilder().build(), observer);
    }

    public void generatePriveBerichten(String u) {
        if (!user.equals(u)) {
            boolean bevat = false;
            for (PriveGesprek pg: privegesprekken) {
                if (pg.getPartner().equals(u)) {
                    bevat = true;
                    break;
                }
            }
            if (!bevat) {
                PriveGesprek pg = new PriveGesprek(this, u);
                Platform.runLater(() -> privegesprekken.add(pg));
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
            if (split[1].equals(user)) {
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

    public void notifyDisconnectedUser() {
        StreamObserver<Username> observer = new StreamObserver<Username>() {
            @Override
            public void onNext(Username value) {
                onlineUsers.remove(value.getName());
                for (PriveGesprek pg: privegesprekken) {
                    if (pg.getPartner().equals(value.getName())) {
                        Platform.runLater(() -> privegesprekken.remove(pg));
                    }
                }
            }
            @Override
            public void onError(Throwable t) {}
            @Override
            public void onCompleted() {}
        };
        asyncStub.notifyDisconnectedUser(Empty.newBuilder().build(), observer);
    }

    public void stopUser() throws InterruptedException {
        sendMessages("left the chat.");
        Username username = Username.newBuilder().setName(user).build();
        blockingStub.disconnectUser(username);
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
