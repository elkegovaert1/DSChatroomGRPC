package be.msec.labgrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

//import com.sun.deploy.net.MessageHeader;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatroomServer {
    private final int port;
    private final Server server;

    private static List<String> messageList;
    private static List<User> userList;
    private static List<Username> usernameList;
    public static ObservableList<String> userNames;

    private static boolean isRunning;

    public ChatroomServer(int port) {
        this(ServerBuilder.forPort(port), port);
        messageList = new ArrayList<>();
        userList = new ArrayList<>();
        userNames = FXCollections.observableArrayList();
        usernameList = new ArrayList<>();
    }

    public ChatroomServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        server = serverBuilder.addService(new ServerService()).build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Server started, listening on " + port);
        isRunning = true;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("*** shutting down gRPC server since JVM is shutting down");
            ChatroomServer.this.stop();
            isRunning = false;
            System.out.println("*** server shut down");
        }));
    }

    public void stop() {
        if (server != null)
            server.shutdown();
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null)
            server.awaitTermination();
    }

    public static void main(String[] args) throws Exception{
        ChatroomServer server = new ChatroomServer(50050);
        server.start();
        server.blockUntilShutdown();

    }

    private static class ServerService extends ServerGrpc.ServerImplBase {

        @Override
        public void sendMessages(MessageText message, StreamObserver<Empty> responseObserver) {
            messageList.add(message.getText());
            for (String s: messageList) {
                System.out.println(s);
            }
            // send notification to all users

            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void connectUser(Username newUser, StreamObserver<Connected> responseObserver) {
            User user = new User(newUser.getName());
            userList.add(user);
            usernameList.add(newUser);
            Platform.runLater(() -> userNames.add(newUser.getName()));
            responseObserver.onNext(Connected.newBuilder().setUsername(newUser.getName()).setIsConnected(true).build());
            System.out.println("Connected user");
            responseObserver.onCompleted();
        }

        @Override
        public void disconnectUser(Username user, StreamObserver<Disconnected> responseObvserver) {
            userList.remove(user.getName());
            Platform.runLater(() -> {
                userNames.remove(user.getName());
            });
            responseObvserver.onCompleted();
        }

        // users get newest messages
        @Override
        public void getMessages(Username user, StreamObserver<MessageText> responseObserver) {
            String lastMsg = messageList.get(messageList.size()-1);
            responseObserver.onNext(MessageText.newBuilder().setText(lastMsg).build());
            responseObserver.onCompleted();
        }
    }
}


