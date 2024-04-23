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
    comments = "Source: kademlia.proto")
public final class KademliaServiceGrpc {

  private KademliaServiceGrpc() {}

  public static final String SERVICE_NAME = "KademliaService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<proto.Kademlia.LookupRequest,
      proto.Kademlia.LookupResponse> getPromptNodeLookupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PromptNodeLookup",
      requestType = proto.Kademlia.LookupRequest.class,
      responseType = proto.Kademlia.LookupResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Kademlia.LookupRequest,
      proto.Kademlia.LookupResponse> getPromptNodeLookupMethod() {
    io.grpc.MethodDescriptor<proto.Kademlia.LookupRequest, proto.Kademlia.LookupResponse> getPromptNodeLookupMethod;
    if ((getPromptNodeLookupMethod = KademliaServiceGrpc.getPromptNodeLookupMethod) == null) {
      synchronized (KademliaServiceGrpc.class) {
        if ((getPromptNodeLookupMethod = KademliaServiceGrpc.getPromptNodeLookupMethod) == null) {
          KademliaServiceGrpc.getPromptNodeLookupMethod = getPromptNodeLookupMethod = 
              io.grpc.MethodDescriptor.<proto.Kademlia.LookupRequest, proto.Kademlia.LookupResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "KademliaService", "PromptNodeLookup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.LookupRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.LookupResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KademliaServiceMethodDescriptorSupplier("PromptNodeLookup"))
                  .build();
          }
        }
     }
     return getPromptNodeLookupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Kademlia.FindNodeRequest,
      proto.Kademlia.FindNodeResponse> getFindNodeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FindNode",
      requestType = proto.Kademlia.FindNodeRequest.class,
      responseType = proto.Kademlia.FindNodeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Kademlia.FindNodeRequest,
      proto.Kademlia.FindNodeResponse> getFindNodeMethod() {
    io.grpc.MethodDescriptor<proto.Kademlia.FindNodeRequest, proto.Kademlia.FindNodeResponse> getFindNodeMethod;
    if ((getFindNodeMethod = KademliaServiceGrpc.getFindNodeMethod) == null) {
      synchronized (KademliaServiceGrpc.class) {
        if ((getFindNodeMethod = KademliaServiceGrpc.getFindNodeMethod) == null) {
          KademliaServiceGrpc.getFindNodeMethod = getFindNodeMethod = 
              io.grpc.MethodDescriptor.<proto.Kademlia.FindNodeRequest, proto.Kademlia.FindNodeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "KademliaService", "FindNode"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.FindNodeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.FindNodeResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KademliaServiceMethodDescriptorSupplier("FindNode"))
                  .build();
          }
        }
     }
     return getFindNodeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Kademlia.RetrieveRequest,
      proto.Kademlia.RetrieveResponse> getRetrieveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Retrieve",
      requestType = proto.Kademlia.RetrieveRequest.class,
      responseType = proto.Kademlia.RetrieveResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Kademlia.RetrieveRequest,
      proto.Kademlia.RetrieveResponse> getRetrieveMethod() {
    io.grpc.MethodDescriptor<proto.Kademlia.RetrieveRequest, proto.Kademlia.RetrieveResponse> getRetrieveMethod;
    if ((getRetrieveMethod = KademliaServiceGrpc.getRetrieveMethod) == null) {
      synchronized (KademliaServiceGrpc.class) {
        if ((getRetrieveMethod = KademliaServiceGrpc.getRetrieveMethod) == null) {
          KademliaServiceGrpc.getRetrieveMethod = getRetrieveMethod = 
              io.grpc.MethodDescriptor.<proto.Kademlia.RetrieveRequest, proto.Kademlia.RetrieveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "KademliaService", "Retrieve"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.RetrieveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.RetrieveResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KademliaServiceMethodDescriptorSupplier("Retrieve"))
                  .build();
          }
        }
     }
     return getRetrieveMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Kademlia.StoreRequest,
      proto.Kademlia.StoreResponse> getStoreMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Store",
      requestType = proto.Kademlia.StoreRequest.class,
      responseType = proto.Kademlia.StoreResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Kademlia.StoreRequest,
      proto.Kademlia.StoreResponse> getStoreMethod() {
    io.grpc.MethodDescriptor<proto.Kademlia.StoreRequest, proto.Kademlia.StoreResponse> getStoreMethod;
    if ((getStoreMethod = KademliaServiceGrpc.getStoreMethod) == null) {
      synchronized (KademliaServiceGrpc.class) {
        if ((getStoreMethod = KademliaServiceGrpc.getStoreMethod) == null) {
          KademliaServiceGrpc.getStoreMethod = getStoreMethod = 
              io.grpc.MethodDescriptor.<proto.Kademlia.StoreRequest, proto.Kademlia.StoreResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "KademliaService", "Store"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.StoreRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.StoreResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KademliaServiceMethodDescriptorSupplier("Store"))
                  .build();
          }
        }
     }
     return getStoreMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Kademlia.DeleteRequest,
      proto.Kademlia.DeleteResponse> getDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Delete",
      requestType = proto.Kademlia.DeleteRequest.class,
      responseType = proto.Kademlia.DeleteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Kademlia.DeleteRequest,
      proto.Kademlia.DeleteResponse> getDeleteMethod() {
    io.grpc.MethodDescriptor<proto.Kademlia.DeleteRequest, proto.Kademlia.DeleteResponse> getDeleteMethod;
    if ((getDeleteMethod = KademliaServiceGrpc.getDeleteMethod) == null) {
      synchronized (KademliaServiceGrpc.class) {
        if ((getDeleteMethod = KademliaServiceGrpc.getDeleteMethod) == null) {
          KademliaServiceGrpc.getDeleteMethod = getDeleteMethod = 
              io.grpc.MethodDescriptor.<proto.Kademlia.DeleteRequest, proto.Kademlia.DeleteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "KademliaService", "Delete"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.DeleteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.DeleteResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KademliaServiceMethodDescriptorSupplier("Delete"))
                  .build();
          }
        }
     }
     return getDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Kademlia.Empty,
      proto.Kademlia.Empty> getPingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Ping",
      requestType = proto.Kademlia.Empty.class,
      responseType = proto.Kademlia.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Kademlia.Empty,
      proto.Kademlia.Empty> getPingMethod() {
    io.grpc.MethodDescriptor<proto.Kademlia.Empty, proto.Kademlia.Empty> getPingMethod;
    if ((getPingMethod = KademliaServiceGrpc.getPingMethod) == null) {
      synchronized (KademliaServiceGrpc.class) {
        if ((getPingMethod = KademliaServiceGrpc.getPingMethod) == null) {
          KademliaServiceGrpc.getPingMethod = getPingMethod = 
              io.grpc.MethodDescriptor.<proto.Kademlia.Empty, proto.Kademlia.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "KademliaService", "Ping"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Kademlia.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new KademliaServiceMethodDescriptorSupplier("Ping"))
                  .build();
          }
        }
     }
     return getPingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static KademliaServiceStub newStub(io.grpc.Channel channel) {
    return new KademliaServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static KademliaServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new KademliaServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static KademliaServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new KademliaServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class KademliaServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void promptNodeLookup(proto.Kademlia.LookupRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.LookupResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getPromptNodeLookupMethod(), responseObserver);
    }

    /**
     */
    public void findNode(proto.Kademlia.FindNodeRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.FindNodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getFindNodeMethod(), responseObserver);
    }

    /**
     */
    public void retrieve(proto.Kademlia.RetrieveRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.RetrieveResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRetrieveMethod(), responseObserver);
    }

    /**
     */
    public void store(proto.Kademlia.StoreRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.StoreResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getStoreMethod(), responseObserver);
    }

    /**
     */
    public void delete(proto.Kademlia.DeleteRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.DeleteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteMethod(), responseObserver);
    }

    /**
     */
    public void ping(proto.Kademlia.Empty request,
        io.grpc.stub.StreamObserver<proto.Kademlia.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getPingMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPromptNodeLookupMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Kademlia.LookupRequest,
                proto.Kademlia.LookupResponse>(
                  this, METHODID_PROMPT_NODE_LOOKUP)))
          .addMethod(
            getFindNodeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Kademlia.FindNodeRequest,
                proto.Kademlia.FindNodeResponse>(
                  this, METHODID_FIND_NODE)))
          .addMethod(
            getRetrieveMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Kademlia.RetrieveRequest,
                proto.Kademlia.RetrieveResponse>(
                  this, METHODID_RETRIEVE)))
          .addMethod(
            getStoreMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Kademlia.StoreRequest,
                proto.Kademlia.StoreResponse>(
                  this, METHODID_STORE)))
          .addMethod(
            getDeleteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Kademlia.DeleteRequest,
                proto.Kademlia.DeleteResponse>(
                  this, METHODID_DELETE)))
          .addMethod(
            getPingMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Kademlia.Empty,
                proto.Kademlia.Empty>(
                  this, METHODID_PING)))
          .build();
    }
  }

  /**
   */
  public static final class KademliaServiceStub extends io.grpc.stub.AbstractStub<KademliaServiceStub> {
    private KademliaServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private KademliaServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KademliaServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KademliaServiceStub(channel, callOptions);
    }

    /**
     */
    public void promptNodeLookup(proto.Kademlia.LookupRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.LookupResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPromptNodeLookupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findNode(proto.Kademlia.FindNodeRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.FindNodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFindNodeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void retrieve(proto.Kademlia.RetrieveRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.RetrieveResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRetrieveMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void store(proto.Kademlia.StoreRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.StoreResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getStoreMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void delete(proto.Kademlia.DeleteRequest request,
        io.grpc.stub.StreamObserver<proto.Kademlia.DeleteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void ping(proto.Kademlia.Empty request,
        io.grpc.stub.StreamObserver<proto.Kademlia.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPingMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class KademliaServiceBlockingStub extends io.grpc.stub.AbstractStub<KademliaServiceBlockingStub> {
    private KademliaServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private KademliaServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KademliaServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KademliaServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public proto.Kademlia.LookupResponse promptNodeLookup(proto.Kademlia.LookupRequest request) {
      return blockingUnaryCall(
          getChannel(), getPromptNodeLookupMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Kademlia.FindNodeResponse findNode(proto.Kademlia.FindNodeRequest request) {
      return blockingUnaryCall(
          getChannel(), getFindNodeMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Kademlia.RetrieveResponse retrieve(proto.Kademlia.RetrieveRequest request) {
      return blockingUnaryCall(
          getChannel(), getRetrieveMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Kademlia.StoreResponse store(proto.Kademlia.StoreRequest request) {
      return blockingUnaryCall(
          getChannel(), getStoreMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Kademlia.DeleteResponse delete(proto.Kademlia.DeleteRequest request) {
      return blockingUnaryCall(
          getChannel(), getDeleteMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Kademlia.Empty ping(proto.Kademlia.Empty request) {
      return blockingUnaryCall(
          getChannel(), getPingMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class KademliaServiceFutureStub extends io.grpc.stub.AbstractStub<KademliaServiceFutureStub> {
    private KademliaServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private KademliaServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KademliaServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KademliaServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Kademlia.LookupResponse> promptNodeLookup(
        proto.Kademlia.LookupRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getPromptNodeLookupMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Kademlia.FindNodeResponse> findNode(
        proto.Kademlia.FindNodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getFindNodeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Kademlia.RetrieveResponse> retrieve(
        proto.Kademlia.RetrieveRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRetrieveMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Kademlia.StoreResponse> store(
        proto.Kademlia.StoreRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getStoreMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Kademlia.DeleteResponse> delete(
        proto.Kademlia.DeleteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Kademlia.Empty> ping(
        proto.Kademlia.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getPingMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PROMPT_NODE_LOOKUP = 0;
  private static final int METHODID_FIND_NODE = 1;
  private static final int METHODID_RETRIEVE = 2;
  private static final int METHODID_STORE = 3;
  private static final int METHODID_DELETE = 4;
  private static final int METHODID_PING = 5;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final KademliaServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(KademliaServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PROMPT_NODE_LOOKUP:
          serviceImpl.promptNodeLookup((proto.Kademlia.LookupRequest) request,
              (io.grpc.stub.StreamObserver<proto.Kademlia.LookupResponse>) responseObserver);
          break;
        case METHODID_FIND_NODE:
          serviceImpl.findNode((proto.Kademlia.FindNodeRequest) request,
              (io.grpc.stub.StreamObserver<proto.Kademlia.FindNodeResponse>) responseObserver);
          break;
        case METHODID_RETRIEVE:
          serviceImpl.retrieve((proto.Kademlia.RetrieveRequest) request,
              (io.grpc.stub.StreamObserver<proto.Kademlia.RetrieveResponse>) responseObserver);
          break;
        case METHODID_STORE:
          serviceImpl.store((proto.Kademlia.StoreRequest) request,
              (io.grpc.stub.StreamObserver<proto.Kademlia.StoreResponse>) responseObserver);
          break;
        case METHODID_DELETE:
          serviceImpl.delete((proto.Kademlia.DeleteRequest) request,
              (io.grpc.stub.StreamObserver<proto.Kademlia.DeleteResponse>) responseObserver);
          break;
        case METHODID_PING:
          serviceImpl.ping((proto.Kademlia.Empty) request,
              (io.grpc.stub.StreamObserver<proto.Kademlia.Empty>) responseObserver);
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

  private static abstract class KademliaServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    KademliaServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return proto.Kademlia.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("KademliaService");
    }
  }

  private static final class KademliaServiceFileDescriptorSupplier
      extends KademliaServiceBaseDescriptorSupplier {
    KademliaServiceFileDescriptorSupplier() {}
  }

  private static final class KademliaServiceMethodDescriptorSupplier
      extends KademliaServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    KademliaServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (KademliaServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new KademliaServiceFileDescriptorSupplier())
              .addMethod(getPromptNodeLookupMethod())
              .addMethod(getFindNodeMethod())
              .addMethod(getRetrieveMethod())
              .addMethod(getStoreMethod())
              .addMethod(getDeleteMethod())
              .addMethod(getPingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
