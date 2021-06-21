package internal.util.rest;

import nbbrd.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@lombok.AllArgsConstructor
public final class TeeInputStream extends InputStream {

    private static final int MAX_SKIP_BUFFER_SIZE = 2048;

    @lombok.NonNull
    private final InputStream input;

    @lombok.NonNull
    private final OutputStream output;

    @Override
    public int read() throws IOException {
        int result = input.read();
        if (result != -1) {
            output.write(result);
        }
        return result;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int result = input.read(b);
        if (result != -1) {
            output.write(b, 0, result);
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = input.read(b, off, len);
        if (result != -1) {
            output.write(b, off, result);
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        return input.available();
    }

    @Override
    public void close() throws IOException {
        Resource.closeBoth(input, output);
    }

    @Override
    public synchronized void mark(int readlimit) {
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}
