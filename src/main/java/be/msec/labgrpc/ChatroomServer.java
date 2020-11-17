package be.msec.labgrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import com.sun.deploy.net.MessageHeader;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatroomServer {
    private final int port;
    private final Server server;

    private static List<String> messageList;
    public static ObservableList<String> userNames;
    public static UserManager userManager;

    private static final Object newMessageMutex = new Object();
    private static final Object newUserMutex = new Object();

    private static boolean isRunning;

    public ChatroomServer(int port) {
        this(ServerBuilder.forPort(port), port);
        messageList = new ArrayList<>();
        userNames = FXCollections.observableArrayList();
        userManager = new UserManager();
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

    private static class ServerService extends ServerGrpc.ServerImplBase {

        @Override
        public void sendMessages(MessageText message, StreamObserver<Empty> responseObserver) {
            synchronized(newMessageMutex) {
                try {
                    User sender = userManager.findUserByName(message.getSender());
                    userManager.addToMessages(new Message("BROAD:"+message.getText(), sender), newMessageMutex);
                    System.out.println(sender.getUsername() + " is broadcasting... " + message.getText());
                    responseObserver.onNext(Empty.newBuilder().build());
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    responseObserver.onCompleted();
                }
            }

        }

        @Override
        public void sendPrivateMsg(PrivateMessageText privateMessageText, StreamObserver<Empty> responseObserver) {
            synchronized(newMessageMutex) {
                try {
                    User sender = userManager.findUserByName(privateMessageText.getMessageText().getSender());
                    User receiver = userManager.findUserByName(privateMessageText.getReceiver());
                    userManager.addToMessages(new Message("PRIVE:" + receiver.getUsername() +":" + privateMessageText.getMessageText().getText(), sender), newMessageMutex);
                    System.out.println(sender.getUsername() + " is sending private message... " + privateMessageText.getMessageText().getText());
                    responseObserver.onNext(Empty.newBuilder().build());
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    responseObserver.onCompleted();
                }
            }
        }

        @Override
        public void connectUser(Username newUser, StreamObserver<Connected> responseObserver) {
            userManager.connectUser(newUser.getName(), newUserMutex);
            Platform.runLater(() -> userNames.add(newUser.getName()));
            responseObserver.onNext(Connected.newBuilder().setUsername(newUser.getName()).setIsConnected(true).build());
            System.out.println("Connected new user: " + newUser.getName());
            responseObserver.onCompleted();
        }

        @Override
        public void disconnectUser(Username user, StreamObserver<Disconnected> responseObserver) {
            userManager.disconnectUser(user.getName());
            Platform.runLater(() -> userNames.remove(user.getName()));
            responseObserver.onNext(Disconnected.newBuilder().setUsername(user.getName()).build());
            responseObserver.onCompleted();
        }

        // users get newest messages
        @Override
        public void getMessages(Username user, StreamObserver<MessageText> responseObserver) {
            while (isRunning) {
                synchronized (newMessageMutex) {
                    try {
                        newMessageMutex.wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseObserver.onCompleted();
                    }
                    Message lastMsg = userManager.getLastMessage(user.getName());

                    System.out.println("Synchronize... " + lastMsg.getText() + " for " + user.getName());
                    responseObserver.onNext(MessageText.newBuilder()
                            .setSender(lastMsg.getSender().getUsername())
                            .setText(lastMsg.getText()).build());
                }
            }
        }

        @Override
        public void getOnlineUsers(Empty nul, StreamObserver<Username> responseObserver) {
            List<User> users = userManager.getOnlineUsers();
            for (User u: users) {
                responseObserver.onNext(Username.newBuilder().setName(u.getUsername()).build());
            }

            while (isRunning) {
                synchronized(newUserMutex) {
                    try {
                        newUserMutex.wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseObserver.onCompleted();
                    }

                    users = userManager.getOnlineUsers();
                    for (User u: users) {
                        responseObserver.onNext(Username.newBuilder().setName(u.getUsername()).build());
                    }
                }
            }
        }
    }
}


