package internal.util.http;

import lombok.NonNull;
import nbbrd.io.net.MediaType;

import java.io.*;
import java.nio.charset.StandardCharsets;

public interface HttpResponse extends Closeable {

    @NonNull MediaType getContentType() throws IOException;

    @NonNull InputStream getBody() throws IOException;

    default @NonNull Reader getBodyAsReader() throws IOException {
        return new InputStreamReader(getBody(), getContentType().getCharset().orElse(StandardCharsets.UTF_8));
    }

    default @NonNull InputStream asDisconnectingInputStream() throws IOException {
        return DisconnectingInputStream.of(this);
    }
}
