package sdmxdl.grpc.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.protobuf.Any;
import com.google.rpc.ErrorInfo;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import javax.net.ssl.SSLException;
import sdmxdl.ResourceNotFoundException;
import sdmxdl.web.HttpStatusException;

/**
 * Mapping of errors to gRPC statuses and REST problem details, shared by gRPC and REST so that both report the
 * same codes, reasons and metadata.
 *
 * @param grpcCode       gRPC status code
 * @param httpStatus     HTTP status code
 * @param reason         stable error reason
 * @param detail         human-readable explanation
 * @param upstreamStatus HTTP status code returned by the source, if any
 */
record ApiError(Status.Code grpcCode, int httpStatus, String reason, String detail, Integer upstreamStatus) {

    static final String DOMAIN = "sdmx-dl";
    static final String SOURCE_KEY = "source";
    static final String UPSTREAM_STATUS_KEY = "upstreamStatus";

    static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";
    static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    static final String UPSTREAM_HTTP_ERROR = "UPSTREAM_HTTP_ERROR";
    static final String UPSTREAM_TIMEOUT = "UPSTREAM_TIMEOUT";
    static final String UPSTREAM_UNREACHABLE = "UPSTREAM_UNREACHABLE";
    static final String UPSTREAM_ERROR = "UPSTREAM_ERROR";
    static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    static ApiError of(Throwable error) {
        Throwable actual = error instanceof UncheckedIOException && error.getCause() != null ? error.getCause() : error;
        String detail = getDetail(actual);
        if (actual instanceof IllegalArgumentException) {
            return new ApiError(Status.Code.INVALID_ARGUMENT, 400, INVALID_ARGUMENT, detail, null);
        }
        if (find(actual, ResourceNotFoundException.class) != null) {
            return new ApiError(Status.Code.NOT_FOUND, 404, RESOURCE_NOT_FOUND, detail, null);
        }
        HttpStatusException http = find(actual, HttpStatusException.class);
        if (http != null) {
            return ofUpstreamStatus(http.getStatusCode(), detail);
        }
        if (find(actual, SocketTimeoutException.class) != null || find(actual, HttpTimeoutException.class) != null) {
            return new ApiError(Status.Code.DEADLINE_EXCEEDED, 504, UPSTREAM_TIMEOUT, detail, null);
        }
        if (find(actual, UnknownHostException.class) != null
                || find(actual, ConnectException.class) != null
                || find(actual, NoRouteToHostException.class) != null
                || find(actual, SSLException.class) != null) {
            return new ApiError(Status.Code.UNAVAILABLE, 503, UPSTREAM_UNREACHABLE, detail, null);
        }
        if (actual instanceof IOException) {
            return new ApiError(Status.Code.UNAVAILABLE, 503, UPSTREAM_ERROR, detail, null);
        }
        return new ApiError(Status.Code.INTERNAL, 500, INTERNAL_ERROR, detail, null);
    }

    private static ApiError ofUpstreamStatus(int statusCode, String detail) {
        return switch (statusCode) {
            case 400 -> new ApiError(Status.Code.INVALID_ARGUMENT, 400, UPSTREAM_HTTP_ERROR, detail, statusCode);
            case 401 -> new ApiError(Status.Code.UNAUTHENTICATED, 401, UPSTREAM_HTTP_ERROR, detail, statusCode);
            case 403 -> new ApiError(Status.Code.PERMISSION_DENIED, 403, UPSTREAM_HTTP_ERROR, detail, statusCode);
            case 404 -> new ApiError(Status.Code.NOT_FOUND, 404, UPSTREAM_HTTP_ERROR, detail, statusCode);
            case 429 -> new ApiError(Status.Code.RESOURCE_EXHAUSTED, 429, UPSTREAM_HTTP_ERROR, detail, statusCode);
            default -> new ApiError(Status.Code.UNAVAILABLE, 503, UPSTREAM_HTTP_ERROR, detail, statusCode);
        };
    }

    /**
     * Converts this error to a gRPC exception carrying a {@link ErrorInfo} detail.
     *
     * @param source the requested source, if any
     * @return a non-null exception
     */
    StatusRuntimeException toGrpc(String source) {
        ErrorInfo.Builder info = ErrorInfo.newBuilder().setReason(reason).setDomain(DOMAIN);
        if (source != null && !source.isEmpty()) {
            info.putMetadata(SOURCE_KEY, source);
        }
        if (upstreamStatus != null) {
            info.putMetadata(UPSTREAM_STATUS_KEY, upstreamStatus.toString());
        }
        return StatusProto.toStatusRuntimeException(com.google.rpc.Status.newBuilder()
                .setCode(grpcCode.value())
                .setMessage(detail)
                .addDetails(Any.pack(info.build()))
                .build());
    }

    /**
     * Converts this error to a problem details object (RFC 9457).
     *
     * @param source the requested source, if any
     * @return a non-null problem
     */
    Problem toProblem(String source) {
        return new Problem(
                "about:blank",
                getTitle(httpStatus),
                httpStatus,
                detail,
                reason,
                source != null && !source.isEmpty() ? source : null,
                upstreamStatus);
    }

    /**
     * Problem details (RFC 9457) returned as {@code application/problem+json} by the REST API.
     *
     * @param type           problem type URI
     * @param title          short summary of the HTTP status
     * @param status         HTTP status code
     * @param detail         human-readable explanation
     * @param reason         stable error reason, same as the gRPC {@code ErrorInfo} reason
     * @param source         requested source, if any
     * @param upstreamStatus HTTP status code returned by the source, if any
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Problem(
            String type,
            String title,
            int status,
            String detail,
            String reason,
            String source,
            Integer upstreamStatus) {}

    private static String getTitle(int httpStatus) {
        return switch (httpStatus) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 429 -> "Too Many Requests";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "Internal Server Error";
        };
    }

    private static String getDetail(Throwable error) {
        String message = error.getMessage();
        return message != null && !message.isEmpty()
                ? message
                : error.getClass().getSimpleName();
    }

    private static <T extends Throwable> T find(Throwable error, Class<T> type) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            if (current.getCause() == current) {
                break;
            }
        }
        return null;
    }
}
