package be.msec.labgrpc;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class ChatroomServer {
    private final int port;
    private final Server server;

    public ChatroomServer(int port) throws IOException {
        this (ServerBuilder.forPort(port), port);
    }

    public ChatroomServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        server = serverBuilder.addService(new ServerService()).build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("*** shutting down gRPC server since JVM is shutting down");
            ChatroomServer.this.stop();
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

    private static class ServerService extends ServerGRPC.ServerImplBase {
        @Override
        public void writeMessage(String message) {

        }

        @Override
        public void getOnlineClients(Empty nul, StreamObserver)
    }
}


