package internal.util.http;

import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;

public interface StreamDecoder {

    @NonNull String getName();

    @NonNull InputStream decode(@NonNull InputStream stream) throws IOException;

    static @NonNull StreamDecoder noOp() {
        return HttpImpl.StreamDecoders.NONE;
    }

    static @NonNull StreamDecoder gzip() {
        return HttpImpl.StreamDecoders.GZIP;
    }

    static @NonNull StreamDecoder deflate() {
        return HttpImpl.StreamDecoders.DEFLATE;
    }
}
