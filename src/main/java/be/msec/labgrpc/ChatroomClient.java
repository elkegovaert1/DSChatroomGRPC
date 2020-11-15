package be.msec.labgrpc;

import java.util.concurrent.TimeUnit;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.StreamObservers;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatroomClient {
    private User user;
    private String username;
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

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void connectUser(String username) {
        Username name = Username.newBuilder().setName(username).build();
        blockingStub.connectUser(name);
        user = new User(username);
        //Platform.runLater(() -> messages.add("[" + username + "] joined chat." ));
        Platform.runLater(() -> {
            sendMessages("[" + username + "] joined chat." );
        });
    }

    public void sendMessages(String text) {
        if (user != null) {
            MessageText msgtxt = MessageText.newBuilder().setText(text).build();
            blockingStub.sendMessages(msgtxt);
        }
    }

    public void getNewMessages() {
        StreamObserver<MessageText> observer = new StreamObserver<MessageText>() {
            @Override
            public void onNext(MessageText value) {
                Platform.runLater(() -> {
                    messages.add(value.getText());
                });
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
        Disconnected message = blockingStub.disconnectUser(username);
        sendMessages("["+user.getUsername()+"] left the chat.");
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
