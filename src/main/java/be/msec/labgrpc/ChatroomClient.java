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
}
