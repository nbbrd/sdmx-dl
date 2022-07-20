package sdmxdl.grpc;

import io.grpc.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

final class LocalhostOnly implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        SocketAddress address = serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        if (address instanceof InetSocketAddress && ((InetSocketAddress) address).getHostString().equals("127.0.0.1")) {
            return serverCallHandler.startCall(serverCall, metadata);
        }
        serverCall.close(Status.PERMISSION_DENIED, metadata);
        return new ServerCall.Listener<ReqT>() {
        };
    }
}
