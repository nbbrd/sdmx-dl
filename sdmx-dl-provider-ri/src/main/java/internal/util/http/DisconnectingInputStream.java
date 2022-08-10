package internal.util.http;

import lombok.AccessLevel;
import nbbrd.io.Resource;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class DisconnectingInputStream extends InputStream {

    public static DisconnectingInputStream of(HttpResponse response) throws IOException {
        return new DisconnectingInputStream(response.getBody(), response);
    }

    @lombok.experimental.Delegate(excludes = Closeable.class)
    private final InputStream delegate;

    private final Closeable onClose;

    @Override
    public void close() throws IOException {
        Resource.closeBoth(delegate, onClose);
    }
}
