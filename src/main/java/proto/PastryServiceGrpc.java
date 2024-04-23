package proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: pastry.proto")
public final class PastryServiceGrpc {

  private PastryServiceGrpc() {}

  public static final String SERVICE_NAME = "PastryService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<proto.Pastry.NodeState,
      proto.Pastry.NewNodes> getNotifyExistenceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "NotifyExistence",
      requestType = proto.Pastry.NodeState.class,
      responseType = proto.Pastry.NewNodes.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Pastry.NodeState,
      proto.Pastry.NewNodes> getNotifyExistenceMethod() {
    io.grpc.MethodDescriptor<proto.Pastry.NodeState, proto.Pastry.NewNodes> getNotifyExistenceMethod;
    if ((getNotifyExistenceMethod = PastryServiceGrpc.getNotifyExistenceMethod) == null) {
      synchronized (PastryServiceGrpc.class) {
        if ((getNotifyExistenceMethod = PastryServiceGrpc.getNotifyExistenceMethod) == null) {
          PastryServiceGrpc.getNotifyExistenceMethod = getNotifyExistenceMethod = 
              io.grpc.MethodDescriptor.<proto.Pastry.NodeState, proto.Pastry.NewNodes>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "PastryService", "NotifyExistence"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.NodeState.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.NewNodes.getDefaultInstance()))
                  .setSchemaDescriptor(new PastryServiceMethodDescriptorSupplier("NotifyExistence"))
                  .build();
          }
        }
     }
     return getNotifyExistenceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Pastry.ForwardRequest,
      proto.Pastry.ForwardResponse> getForwardMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Forward",
      requestType = proto.Pastry.ForwardRequest.class,
      responseType = proto.Pastry.ForwardResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Pastry.ForwardRequest,
      proto.Pastry.ForwardResponse> getForwardMethod() {
    io.grpc.MethodDescriptor<proto.Pastry.ForwardRequest, proto.Pastry.ForwardResponse> getForwardMethod;
    if ((getForwardMethod = PastryServiceGrpc.getForwardMethod) == null) {
      synchronized (PastryServiceGrpc.class) {
        if ((getForwardMethod = PastryServiceGrpc.getForwardMethod) == null) {
          PastryServiceGrpc.getForwardMethod = getForwardMethod = 
              io.grpc.MethodDescriptor.<proto.Pastry.ForwardRequest, proto.Pastry.ForwardResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "PastryService", "Forward"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.ForwardRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.ForwardResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new PastryServiceMethodDescriptorSupplier("Forward"))
                  .build();
          }
        }
     }
     return getForwardMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Pastry.JoinRequest,
      proto.Pastry.JoinResponse> getJoinMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Join",
      requestType = proto.Pastry.JoinRequest.class,
      responseType = proto.Pastry.JoinResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Pastry.JoinRequest,
      proto.Pastry.JoinResponse> getJoinMethod() {
    io.grpc.MethodDescriptor<proto.Pastry.JoinRequest, proto.Pastry.JoinResponse> getJoinMethod;
    if ((getJoinMethod = PastryServiceGrpc.getJoinMethod) == null) {
      synchronized (PastryServiceGrpc.class) {
        if ((getJoinMethod = PastryServiceGrpc.getJoinMethod) == null) {
          PastryServiceGrpc.getJoinMethod = getJoinMethod = 
              io.grpc.MethodDescriptor.<proto.Pastry.JoinRequest, proto.Pastry.JoinResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "PastryService", "Join"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.JoinRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.JoinResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new PastryServiceMethodDescriptorSupplier("Join"))
                  .build();
          }
        }
     }
     return getJoinMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Pastry.MoveKeysRequest,
      proto.Pastry.Empty> getMoveKeysMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MoveKeys",
      requestType = proto.Pastry.MoveKeysRequest.class,
      responseType = proto.Pastry.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Pastry.MoveKeysRequest,
      proto.Pastry.Empty> getMoveKeysMethod() {
    io.grpc.MethodDescriptor<proto.Pastry.MoveKeysRequest, proto.Pastry.Empty> getMoveKeysMethod;
    if ((getMoveKeysMethod = PastryServiceGrpc.getMoveKeysMethod) == null) {
      synchronized (PastryServiceGrpc.class) {
        if ((getMoveKeysMethod = PastryServiceGrpc.getMoveKeysMethod) == null) {
          PastryServiceGrpc.getMoveKeysMethod = getMoveKeysMethod = 
              io.grpc.MethodDescriptor.<proto.Pastry.MoveKeysRequest, proto.Pastry.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "PastryService", "MoveKeys"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.MoveKeysRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new PastryServiceMethodDescriptorSupplier("MoveKeys"))
                  .build();
          }
        }
     }
     return getMoveKeysMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Pastry.NeighborSetRequest,
      proto.Pastry.NeighborSetResponse> getGetNeighborSetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNeighborSet",
      requestType = proto.Pastry.NeighborSetRequest.class,
      responseType = proto.Pastry.NeighborSetResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Pastry.NeighborSetRequest,
      proto.Pastry.NeighborSetResponse> getGetNeighborSetMethod() {
    io.grpc.MethodDescriptor<proto.Pastry.NeighborSetRequest, proto.Pastry.NeighborSetResponse> getGetNeighborSetMethod;
    if ((getGetNeighborSetMethod = PastryServiceGrpc.getGetNeighborSetMethod) == null) {
      synchronized (PastryServiceGrpc.class) {
        if ((getGetNeighborSetMethod = PastryServiceGrpc.getGetNeighborSetMethod) == null) {
          PastryServiceGrpc.getGetNeighborSetMethod = getGetNeighborSetMethod = 
              io.grpc.MethodDescriptor.<proto.Pastry.NeighborSetRequest, proto.Pastry.NeighborSetResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "PastryService", "GetNeighborSet"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.NeighborSetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.NeighborSetResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new PastryServiceMethodDescriptorSupplier("GetNeighborSet"))
                  .build();
          }
        }
     }
     return getGetNeighborSetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Pastry.Empty,
      proto.Pastry.Empty> getPingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Ping",
      requestType = proto.Pastry.Empty.class,
      responseType = proto.Pastry.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Pastry.Empty,
      proto.Pastry.Empty> getPingMethod() {
    io.grpc.MethodDescriptor<proto.Pastry.Empty, proto.Pastry.Empty> getPingMethod;
    if ((getPingMethod = PastryServiceGrpc.getPingMethod) == null) {
      synchronized (PastryServiceGrpc.class) {
        if ((getPingMethod = PastryServiceGrpc.getPingMethod) == null) {
          PastryServiceGrpc.getPingMethod = getPingMethod = 
              io.grpc.MethodDescriptor.<proto.Pastry.Empty, proto.Pastry.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "PastryService", "Ping"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Pastry.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new PastryServiceMethodDescriptorSupplier("Ping"))
                  .build();
          }
        }
     }
     return getPingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PastryServiceStub newStub(io.grpc.Channel channel) {
    return new PastryServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PastryServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new PastryServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PastryServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new PastryServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class PastryServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void notifyExistence(proto.Pastry.NodeState request,
        io.grpc.stub.StreamObserver<proto.Pastry.NewNodes> responseObserver) {
      asyncUnimplementedUnaryCall(getNotifyExistenceMethod(), responseObserver);
    }

    /**
     * <pre>
     * used by put,get and delete to find storing node, function similar to Join
     * </pre>
     */
    public void forward(proto.Pastry.ForwardRequest request,
        io.grpc.stub.StreamObserver<proto.Pastry.ForwardResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getForwardMethod(), responseObserver);
    }

    /**
     */
    public void join(proto.Pastry.JoinRequest request,
        io.grpc.stub.StreamObserver<proto.Pastry.JoinResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getJoinMethod(), responseObserver);
    }

    /**
     */
    public void moveKeys(proto.Pastry.MoveKeysRequest request,
        io.grpc.stub.StreamObserver<proto.Pastry.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getMoveKeysMethod(), responseObserver);
    }

    /**
     */
    public void getNeighborSet(proto.Pastry.NeighborSetRequest request,
        io.grpc.stub.StreamObserver<proto.Pastry.NeighborSetResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetNeighborSetMethod(), responseObserver);
    }

    /**
     */
    public void ping(proto.Pastry.Empty request,
        io.grpc.stub.StreamObserver<proto.Pastry.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getPingMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getNotifyExistenceMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Pastry.NodeState,
                proto.Pastry.NewNodes>(
                  this, METHODID_NOTIFY_EXISTENCE)))
          .addMethod(
            getForwardMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Pastry.ForwardRequest,
                proto.Pastry.ForwardResponse>(
                  this, METHODID_FORWARD)))
          .addMethod(
            getJoinMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Pastry.JoinRequest,
                proto.Pastry.JoinResponse>(
                  this, METHODID_JOIN)))
          .addMethod(
            getMoveKeysMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Pastry.MoveKeysRequest,
                proto.Pastry.Empty>(
                  this, METHODID_MOVE_KEYS)))
          .addMethod(
            getGetNeighborSetMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Pastry.NeighborSetRequest,
                proto.Pastry.NeighborSetResponse>(
                  this, METHODID_GET_NEIGHBOR_SET)))
          .addMethod(
            getPingMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Pastry.Empty,
                proto.Pastry.Empty>(
                  this, METHODID_PING)))
          .build();
    }
  }

  /**
   */
  public static final class PastryServiceStub extends io.grpc.stub.AbstractStub<PastryServiceStub> {
    private PastryServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private PastryServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PastryServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new PastryServiceStub(channel, callOptions);
    }

    /**
     */
    public void notifyExistence(proto.Pastry.NodeState request,
        io.grpc.stub.StreamObserver<proto.Pastry.NewNodes> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getNotifyExistenceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * used by put,get and delete to find storing node, function similar to Join
     * </pre>
     */
    public void forward(proto.Pastry.ForwardRequest request,
        io.grpc.stub.StreamObserver<proto.Pastry.ForwardResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getForwardMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void join(proto.Pastry.JoinRequest request,
        io.grpc.stub.StreamObserver<proto.Pastry.JoinResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getJoinMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void moveKeys(proto.Pastry.MoveKeysRequest request,
        io.grpc.stub.StreamObserver<proto.Pastry.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMoveKeysMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNeighborSet(proto.Pastry.NeighborSetRequest request,
        io.grpc.stub.StreamObserver<proto.Pastry.NeighborSetResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetNeighborSetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void ping(proto.Pastry.Empty request,
        io.grpc.stub.StreamObserver<proto.Pastry.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPingMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class PastryServiceBlockingStub extends io.grpc.stub.AbstractStub<PastryServiceBlockingStub> {
    private PastryServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private PastryServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PastryServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new PastryServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public proto.Pastry.NewNodes notifyExistence(proto.Pastry.NodeState request) {
      return blockingUnaryCall(
          getChannel(), getNotifyExistenceMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * used by put,get and delete to find storing node, function similar to Join
     * </pre>
     */
    public proto.Pastry.ForwardResponse forward(proto.Pastry.ForwardRequest request) {
      return blockingUnaryCall(
          getChannel(), getForwardMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Pastry.JoinResponse join(proto.Pastry.JoinRequest request) {
      return blockingUnaryCall(
          getChannel(), getJoinMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Pastry.Empty moveKeys(proto.Pastry.MoveKeysRequest request) {
      return blockingUnaryCall(
          getChannel(), getMoveKeysMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Pastry.NeighborSetResponse getNeighborSet(proto.Pastry.NeighborSetRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetNeighborSetMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Pastry.Empty ping(proto.Pastry.Empty request) {
      return blockingUnaryCall(
          getChannel(), getPingMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class PastryServiceFutureStub extends io.grpc.stub.AbstractStub<PastryServiceFutureStub> {
    private PastryServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private PastryServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PastryServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new PastryServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Pastry.NewNodes> notifyExistence(
        proto.Pastry.NodeState request) {
      return futureUnaryCall(
          getChannel().newCall(getNotifyExistenceMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * used by put,get and delete to find storing node, function similar to Join
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Pastry.ForwardResponse> forward(
        proto.Pastry.ForwardRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getForwardMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Pastry.JoinResponse> join(
        proto.Pastry.JoinRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getJoinMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Pastry.Empty> moveKeys(
        proto.Pastry.MoveKeysRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getMoveKeysMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Pastry.NeighborSetResponse> getNeighborSet(
        proto.Pastry.NeighborSetRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetNeighborSetMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Pastry.Empty> ping(
        proto.Pastry.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getPingMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_NOTIFY_EXISTENCE = 0;
  private static final int METHODID_FORWARD = 1;
  private static final int METHODID_JOIN = 2;
  private static final int METHODID_MOVE_KEYS = 3;
  private static final int METHODID_GET_NEIGHBOR_SET = 4;
  private static final int METHODID_PING = 5;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final PastryServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(PastryServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_NOTIFY_EXISTENCE:
          serviceImpl.notifyExistence((proto.Pastry.NodeState) request,
              (io.grpc.stub.StreamObserver<proto.Pastry.NewNodes>) responseObserver);
          break;
        case METHODID_FORWARD:
          serviceImpl.forward((proto.Pastry.ForwardRequest) request,
              (io.grpc.stub.StreamObserver<proto.Pastry.ForwardResponse>) responseObserver);
          break;
        case METHODID_JOIN:
          serviceImpl.join((proto.Pastry.JoinRequest) request,
              (io.grpc.stub.StreamObserver<proto.Pastry.JoinResponse>) responseObserver);
          break;
        case METHODID_MOVE_KEYS:
          serviceImpl.moveKeys((proto.Pastry.MoveKeysRequest) request,
              (io.grpc.stub.StreamObserver<proto.Pastry.Empty>) responseObserver);
          break;
        case METHODID_GET_NEIGHBOR_SET:
          serviceImpl.getNeighborSet((proto.Pastry.NeighborSetRequest) request,
              (io.grpc.stub.StreamObserver<proto.Pastry.NeighborSetResponse>) responseObserver);
          break;
        case METHODID_PING:
          serviceImpl.ping((proto.Pastry.Empty) request,
              (io.grpc.stub.StreamObserver<proto.Pastry.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class PastryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PastryServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return proto.Pastry.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PastryService");
    }
  }

  private static final class PastryServiceFileDescriptorSupplier
      extends PastryServiceBaseDescriptorSupplier {
    PastryServiceFileDescriptorSupplier() {}
  }

  private static final class PastryServiceMethodDescriptorSupplier
      extends PastryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    PastryServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (PastryServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PastryServiceFileDescriptorSupplier())
              .addMethod(getNotifyExistenceMethod())
              .addMethod(getForwardMethod())
              .addMethod(getJoinMethod())
              .addMethod(getMoveKeysMethod())
              .addMethod(getGetNeighborSetMethod())
              .addMethod(getPingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
