package internal.util.http;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface HttpResponse extends Closeable {

    @NonNull MediaType getContentType() throws IOException;

    @NonNull InputStream getBody() throws IOException;
}
