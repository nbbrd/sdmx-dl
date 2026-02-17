package internal.util.credentials;

import java.net.PasswordAuthentication;

class SwingPasswordPromptTest {

    public static void main(String[] args) {
        SwingPasswordPrompt x = new SwingPasswordPrompt();
        PasswordAuthentication result = x.promptCredentials("Some resource", "Select your credentials for accessing the resource");
        System.out.println(result != null ? result.getUserName() + ":" + new String(result.getPassword()) : "No credentials provided");
    }
}