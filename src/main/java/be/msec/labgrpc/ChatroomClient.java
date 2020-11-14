package be.msec.labgrpc;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.StreamObservers;

import be.msec.labgrpc.CalculatorGrpc.CalculatorBlockingStub;
import be.msec.labgrpc.CalculatorGrpc.CalculatorStub;

public class ChatroomClient {
    private final ManagedChannel channel;
    private final ChatroomBlockingStub blockingStub;
    private final ChatroomStub asyncStub;

    public ChatroomClient(String host, int port){
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    public ChatroomClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = CalculatorGrpc.newBlockingStub(channel);
        asyncStub = CalculatorGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        ChatroomClient client = new ChatroomClient("localhost", 50050);
        try{
            client.calculateSum(10, 5);

            client.streamingSum(5);

            client.calculatorHistory();
        } finally {
            client.shutdown();
        }
    }

}
