package internal.util.credentials;

import java.io.IOException;
import java.net.PasswordAuthentication;

class WindowsPasswordPromptTest {

    public static void main(String[] args) throws IOException {
        WindowsPasswordPrompt x = new WindowsPasswordPrompt();
        PasswordAuthentication result = x.promptCredentials("Some resource", "Select your credentials for accessing the resource");
        System.out.println(result != null ? result.getUserName() + ":" + new String(result.getPassword()) : "No credentials provided");
    }
}