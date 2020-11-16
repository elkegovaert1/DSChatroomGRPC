package be.msec.labgrpc;

import java.util.concurrent.TimeUnit;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatroomClient {
    private User user;
    private final ManagedChannel channel;
    private final ServerGrpc.ServerStub asyncStub;
    private final ServerGrpc.ServerBlockingStub blockingStub;

    public static ObservableList<String> messages;
    public static ObservableList<String> privateMessages;

    public ChatroomClient(String host, int port){
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    public ChatroomClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = ServerGrpc.newBlockingStub(channel);
        asyncStub = ServerGrpc.newStub(channel);
        messages = FXCollections.observableArrayList();
        privateMessages = FXCollections.observableArrayList();
    }

    public void connectUser(String username) {
        Username name = Username.newBuilder().setName(username).build();
        boolean connected = blockingStub.connectUser(name).getIsConnected();
        System.out.println("Connected: " + connected);
        user = new User(username);
        Platform.runLater(() -> sendMessages("joined chat." ));

        getNewMessages();
    }

    public void sendMessages(String text) {
        MessageText msgtxt = MessageText.newBuilder()
                .setText("[" + user.getUsername() + "] " + text)
                .setSender(user.getUsername()).build();
        System.out.println("Broadcasting... " + msgtxt.getText());
        blockingStub.sendMessages(msgtxt);

        //
    }

    public void getNewMessages() {
        StreamObserver<MessageText> observer = new StreamObserver<MessageText>() {
            @Override
            public void onNext(MessageText value) {
                System.out.println("Message received from: " + value.getSender());
                Platform.runLater(() -> messages.add(value.getText()));
            }
            @Override
            public void onError(Throwable t) {}
            @Override
            public void onCompleted() {}
        };
        asyncStub.getMessages(Username.newBuilder().setName(user.getUsername()).build(), observer);
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
}
