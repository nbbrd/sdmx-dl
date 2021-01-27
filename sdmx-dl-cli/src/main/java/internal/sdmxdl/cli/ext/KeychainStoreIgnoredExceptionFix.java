package internal.sdmxdl.cli.ext;

import nbbrd.io.sys.OS;

import java.io.PrintStream;
import java.util.logging.Logger;

public final class KeychainStoreIgnoredExceptionFix extends PrintStream {

    public static void register() {
        if (OS.NAME.equals(OS.Name.MACOS)) {
            PrintStream err = System.err;
            if (!(err instanceof KeychainStoreIgnoredExceptionFix))
                System.setErr(new KeychainStoreIgnoredExceptionFix(err));
        }
    }

    public static void unregister() {
        if (OS.NAME.equals(OS.Name.MACOS)) {
            PrintStream err = System.err;
            if (err instanceof KeychainStoreIgnoredExceptionFix) {
                System.setErr(((KeychainStoreIgnoredExceptionFix) err).getOriginal());
            }
        }
    }

    private KeychainStoreIgnoredExceptionFix(PrintStream original) {
        super(original);
    }

    @Override
    public void println(String x) {
        if (isIgnoredException(x)) {
            Logger.getLogger("apple.security.KeychainStore").warning(x);
        } else {
            super.println(x);
        }
    }

    private boolean isIgnoredException(String x) {
        return x != null && x.startsWith("KeychainStore Ignored Exception: ");
    }

    private PrintStream getOriginal() {
        return (PrintStream) out;
    }
}
