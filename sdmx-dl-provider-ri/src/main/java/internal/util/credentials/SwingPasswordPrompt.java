package internal.util.credentials;

import java.awt.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.PasswordAuthentication;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.PasswordPrompt;

@DirectImpl
@ServiceProvider
public final class SwingPasswordPrompt implements PasswordPrompt {

    @Override
    public @NonNull String getPromptId() {
        return "SWING_PROMPT";
    }

    @Override
    public boolean isPromptAvailable() {
        return !GraphicsEnvironment.isHeadless();
    }

    @Override
    public int getPromptRank() {
        return 100;
    }

    @Override
    public @Nullable PasswordAuthentication promptCredentials(@NonNull String caption, @NonNull String message)
            throws IOException {
        AtomicReference<JDialog> dialog = new AtomicReference<>();
        if (SwingUtilities.isEventDispatchThread()) {
            return showDialog(caption, message, dialog);
        }
        // NB: Swing components must be used on the event dispatch thread
        FutureTask<PasswordAuthentication> task = new FutureTask<>(() -> showDialog(caption, message, dialog));
        SwingUtilities.invokeLater(task);
        try {
            return task.get(PROMPT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            close(task, dialog);
            Thread.currentThread().interrupt();
            InterruptedIOException result = new InterruptedIOException("Interrupted while waiting for credentials");
            result.initCause(ex);
            throw result;
        } catch (TimeoutException ex) {
            close(task, dialog);
            throw new IOException("No credentials within " + PROMPT_TIMEOUT, ex);
        } catch (ExecutionException ex) {
            throw new IOException(ex.getCause());
        }
    }

    private static final Duration PROMPT_TIMEOUT = Duration.ofMinutes(2);

    private static @Nullable PasswordAuthentication showDialog(
            String caption, String message, AtomicReference<JDialog> dialog) {
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JOptionPane pane = new JOptionPane(
                createCredentialsPanel(message, usernameField, passwordField),
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        JDialog result = pane.createDialog(caption);
        dialog.set(result);
        try {
            result.setVisible(true);
        } finally {
            result.dispose();
        }
        Object value = pane.getValue();
        return value instanceof Integer && (Integer) value == JOptionPane.OK_OPTION
                ? new PasswordAuthentication(usernameField.getText(), passwordField.getPassword())
                : null;
    }

    private static void close(FutureTask<?> task, AtomicReference<JDialog> dialog) {
        task.cancel(false);
        SwingUtilities.invokeLater(() -> {
            JDialog result = dialog.get();
            if (result != null) {
                result.dispose();
            }
        });
    }

    private static JPanel createCredentialsPanel(
            String message, JTextField usernameField, JPasswordField passwordField) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbUsername = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(usernameField, cs);

        JLabel lbPassword = new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(passwordField, cs);

        return panel;
    }
}
