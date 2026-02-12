package internal.util.credentials;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.PasswordPrompt;

import javax.swing.*;
import java.awt.*;
import java.net.PasswordAuthentication;

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
    public @Nullable PasswordAuthentication promptCredentials(@NonNull String caption, @NonNull String message) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception ignore) {
//        }
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        int result = JOptionPane.showConfirmDialog(null, createCredentialsPanel(message, usernameField, passwordField), caption, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            return new PasswordAuthentication(usernameField.getText(), passwordField.getPassword());
        }
        return null;
    }

    private static JPanel createCredentialsPanel(String message, JTextField usernameField, JPasswordField passwordField) {
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
