package tests.sdmxdl.web;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;

public final class IconSamples {

    private IconSamples() {}

    /** Creates the header of a PNG file, which is enough to read its size. */
    public static byte[] png(int width, int height) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}, 0, 8);
        writeInt(result, 13);
        result.write(new byte[] {'I', 'H', 'D', 'R'}, 0, 4);
        writeInt(result, width);
        writeInt(result, height);
        result.write(new byte[] {8, 6, 0, 0, 0}, 0, 5);
        return result.toByteArray();
    }

    public static byte[] svg(String text) {
        return text.getBytes(UTF_8);
    }

    private static void writeInt(ByteArrayOutputStream output, int value) {
        output.write(value >>> 24);
        output.write(value >>> 16);
        output.write(value >>> 8);
        output.write(value);
    }
}
