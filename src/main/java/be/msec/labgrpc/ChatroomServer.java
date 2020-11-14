package be.msec.labgrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.deploy.net.MessageHeader;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class ChatroomServer {
    private final int port;
    private final Server server;

    private List<String> messageList;
    private static List<User> userList;

    private static boolean isRunning;

    public ChatroomServer(int port) {
        messageList = new ArrayList<Message>();
        userList = new ArrayList<User>();
        this (ServerBuilder.forPort(port), port);
    }

    public ChatroomServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        server = serverBuilder.addService(new ChatService()).build();
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

    private static class ChatService extends ChatgRPC.ChatImplBase {
        @Override
        public void sendMessages(MessageText message, StreamObserver<Empty> responseObserver) {

        }

        @Override
        public void connectUser(Client newUser, StreamObserver<Empty> responseObserver) {
            User user = new User(newUser.getName());
            userList.add(user);
            responseObserver.onCompleted();
        }

        @Override
        public void disconnectUser(Client user, StreamObserver<Empty> responseObvserver) {
            userList.remove(user.getName());
            responseObvserver.onCompleted();
        }

        @Override
        public void getMessages(Client user, StreamObserver<MessageText> responseObserver) {

        }
    }
}


