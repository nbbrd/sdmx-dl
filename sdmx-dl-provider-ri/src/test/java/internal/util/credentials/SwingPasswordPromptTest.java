package internal.util.credentials;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.swing.*;
import org.junit.jupiter.api.Test;

class SwingPasswordPromptTest {

    @Test
    void testInterruptClosesDialog() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless());

        AtomicReference<Throwable> error = new AtomicReference<>();
        Thread caller = new Thread(() -> {
            try {
                new SwingPasswordPrompt().promptCredentials("Some resource", "Some message");
            } catch (Throwable ex) {
                error.set(ex);
            }
        });
        caller.start();
        assertThat(waitFor(SwingPasswordPromptTest::isDialogShowing)).isTrue();

        caller.interrupt();
        caller.join(10_000);

        assertThat(caller.isAlive()).isFalse();
        assertThat(error.get()).isInstanceOf(InterruptedIOException.class);
        assertThat(waitFor(() -> !isDialogShowing())).isTrue();
    }

    private static boolean isDialogShowing() {
        return Arrays.stream(Window.getWindows()).anyMatch(window -> window instanceof JDialog && window.isShowing());
    }

    private static boolean waitFor(BooleanSupplier condition) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        SwingPasswordPrompt x = new SwingPasswordPrompt();
        PasswordAuthentication result =
                x.promptCredentials("Some resource", "Select your credentials for accessing the resource");
        System.out.println(
                result != null
                        ? result.getUserName() + ":" + new String(result.getPassword())
                        : "No credentials provided");
    }
}
