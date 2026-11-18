package sdmxdl.grpc;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import io.quarkus.runtime.ImageMode;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.value.registry.ValueRegistry;
import io.quarkus.vertx.http.HttpServer;
import io.quarkus.vertx.http.HttpServerStart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jspecify.annotations.Nullable;
import sdmxdl.About;

/**
 * Adds a system tray icon to open the web UI and the search UI, and to quit the application, if a desktop is available.
 */
@ApplicationScoped
public class SystemTrayMenu {

    private static final Logger LOGGER = Logger.getLogger(SystemTrayMenu.class);

    private static final int ICON_SIZE = 16;
    private static final String LOGO = "sdmxdl/grpc/sdmx-logo.svg";

    private static final String WEB_UI_PATH = "index.html";
    private static final String SEARCH_UI_PATH = "search.html";
    private static final String API_DOCS_PATH = "q/swagger-ui";
    private static final String MCP_PATH = "mcp";

    private static final Pattern WINDOWS_DARK_THEME = Pattern.compile("SystemUsesLightTheme\\s+REG_DWORD\\s+0x0\\b");

    @ConfigProperty(name = "sdmxdl.tray.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "quarkus.http.host", defaultValue = "localhost")
    String host;

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    int port;

    @Inject
    ValueRegistry registry;

    private final AtomicBoolean installed = new AtomicBoolean();

    private volatile @Nullable TrayIcon trayIcon;

    private volatile @Nullable JDialog popupOwner;

    // Fired once the HTTP server listens, so that its actual port is known
    void onStart(@ObservesAsync HttpServerStart event) {
        if (!enabled) {
            LOGGER.debug("System tray disabled by config");
            return;
        }
        if (ImageMode.current() == ImageMode.NATIVE_RUN) {
            LOGGER.debug("System tray not available in native mode");
            return;
        }
        if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
            LOGGER.debug("System tray not supported");
            return;
        }
        if (installed.getAndSet(true)) {
            return;
        }
        URI baseUri = getActualBaseUri();
        LOGGER.debugf("System tray for %s", baseUri);
        EventQueue.invokeLater(() -> install(baseUri));
    }

    void onStop(@Observes ShutdownEvent event) {
        TrayIcon icon = trayIcon;
        if (icon != null) {
            trayIcon = null;
            SystemTray.getSystemTray().remove(icon);
        }
        JDialog owner = popupOwner;
        if (owner != null) {
            popupOwner = null;
            EventQueue.invokeLater(owner::dispose);
        }
    }

    // Uses the port the server actually listens on, which differs from the config with a random port
    private URI getActualBaseUri() {
        try {
            HttpServer server = registry.getOrDefault(HttpServer.HTTP_SERVER, null);
            if (server != null) {
                return getBaseUri(host, server.getPort());
            }
        } catch (RuntimeException ex) {
            LOGGER.debug("Failed to get actual HTTP port", ex);
        }
        return getBaseUri(host, port);
    }

    private void install(URI baseUri) {
        try {
            applyTheme(isSystemDarkTheme());

            URI webUi = baseUri.resolve(WEB_UI_PATH);
            JPopupMenu menu = createMenu(baseUri);
            JDialog owner = createPopupOwner(menu);

            // multi-resolution image, sharp at any display scaling
            TrayIcon icon =
                    new TrayIcon(new FlatSVGIcon(LOGO, ICON_SIZE, ICON_SIZE).getImage(), About.NAME + " - " + webUi);
            icon.setImageAutoSize(true);
            icon.addActionListener(e -> browse(webUi));
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    showIfPopupTrigger(e, menu, owner);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    showIfPopupTrigger(e, menu, owner);
                }
            });

            SystemTray.getSystemTray().add(icon);
            trayIcon = icon;
            popupOwner = owner;
        } catch (AWTException | RuntimeException ex) {
            LOGGER.warn("Failed to add system tray icon", ex);
        }
    }

    private static JPopupMenu createMenu(URI baseUri) {
        URI webUi = baseUri.resolve(WEB_UI_PATH);
        URI searchUi = baseUri.resolve(SEARCH_UI_PATH);
        URI apiDocs = baseUri.resolve(API_DOCS_PATH);
        URI mcp = baseUri.resolve(MCP_PATH);

        FlatSVGIcon logo = new FlatSVGIcon(LOGO, ICON_SIZE, ICON_SIZE);
        JMenuItem statusItem = new JMenuItem(About.NAME + " " + About.VERSION + " - " + baseUri.getAuthority(), logo);
        statusItem.setDisabledIcon(logo);
        statusItem.setEnabled(false);

        JMenuItem openItem = new JMenuItem("Open web UI", getBootstrapIconOrNull("globe"));
        openItem.setFont(openItem.getFont().deriveFont(Font.BOLD));
        openItem.addActionListener(e -> browse(webUi));

        JMenuItem searchItem = new JMenuItem("Open search UI", getBootstrapIconOrNull("search"));
        searchItem.addActionListener(e -> browse(searchUi));

        JMenuItem apiDocsItem = new JMenuItem("Open API docs", getBootstrapIconOrNull("book"));
        apiDocsItem.addActionListener(e -> browse(apiDocs));

        JMenuItem copyUrlItem = new JMenuItem("Copy web UI URL", getBootstrapIconOrNull("link-45deg"));
        copyUrlItem.addActionListener(e -> copyToClipboard(webUi.toString()));

        JMenuItem copyMcpItem = new JMenuItem("Copy MCP endpoint", getBootstrapIconOrNull("robot"));
        copyMcpItem.addActionListener(e -> copyToClipboard(mcp.toString()));

        JMenuItem quitItem = new JMenuItem("Quit", getBootstrapIconOrNull("power"));
        quitItem.addActionListener(e -> Quarkus.asyncExit());

        JPopupMenu result = new JPopupMenu();
        result.add(statusItem);
        result.addSeparator();
        result.add(openItem);
        result.add(searchItem);
        result.add(apiDocsItem);
        result.addSeparator();
        result.add(copyUrlItem);
        result.add(copyMcpItem);
        result.addSeparator();
        result.add(quitItem);
        return result;
    }

    // A Swing popup needs a focused window to be dismissed on outside clicks and Escape key
    private static JDialog createPopupOwner(JPopupMenu menu) {
        JDialog result = new JDialog();
        result.setUndecorated(true);
        result.setType(Window.Type.UTILITY);
        result.setSize(1, 1);
        if (GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
            result.setBackground(new Color(0, 0, 0, 0));
        }
        result.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                menu.setVisible(false);
            }
        });
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                result.setVisible(false);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                result.setVisible(false);
            }
        });
        return result;
    }

    private static void showIfPopupTrigger(MouseEvent e, JPopupMenu menu, JDialog owner) {
        if (!e.isPopupTrigger()) {
            return;
        }
        boolean dark = isSystemDarkTheme();
        if (dark != FlatLaf.isLafDark()) {
            applyTheme(dark);
            SwingUtilities.updateComponentTreeUI(menu);
            SwingUtilities.updateComponentTreeUI(owner);
        }
        PointerInfo pointer = MouseInfo.getPointerInfo();
        if (pointer != null) {
            owner.setLocation(pointer.getLocation());
        }
        owner.setVisible(true);
        owner.toFront();
        menu.show(owner, 0, 0);
    }

    private static void applyTheme(boolean dark) {
        if (dark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }

    private static boolean isSystemDarkTheme() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.startsWith("windows")) {
            return isWindowsDarkTheme(execute(
                    "reg",
                    "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v",
                    "SystemUsesLightTheme"));
        }
        if (os.startsWith("mac")) {
            return execute("defaults", "read", "-g", "AppleInterfaceStyle")
                    .trim()
                    .equalsIgnoreCase("dark");
        }
        if (os.startsWith("linux")) {
            return execute("gsettings", "get", "org.gnome.desktop.interface", "color-scheme")
                    .contains("prefer-dark");
        }
        return false;
    }

    // The taskbar and its menus follow the system theme, not the apps theme
    static boolean isWindowsDarkTheme(String regQueryOutput) {
        return WINDOWS_DARK_THEME.matcher(regQueryOutput).find();
    }

    private static String execute(String... command) {
        try {
            Process process = new ProcessBuilder(command).start();
            try (InputStream stream = process.getInputStream()) {
                String result = new String(stream.readAllBytes(), Charset.defaultCharset());
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroy();
                }
                return result;
            }
        } catch (IOException ex) {
            LOGGER.debugf(ex, "Failed to execute %s", command[0]);
            return "";
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    // Reuses the icons of the web UI, located in a versioned folder of the bootstrap-icons webjar
    private static @Nullable Icon getBootstrapIconOrNull(String name) {
        ClassLoader loader = SystemTrayMenu.class.getClassLoader();
        try (InputStream stream =
                loader.getResourceAsStream("META-INF/maven/org.mvnpm/bootstrap-icons/pom.properties")) {
            if (stream == null) {
                return null;
            }
            Properties properties = new Properties();
            properties.load(stream);
            URL resource = loader.getResource("META-INF/resources/_static/bootstrap-icons/"
                    + properties.getProperty("version") + "/icons/" + name + ".svg");
            return resource != null ? new FlatSVGIcon(resource).derive(ICON_SIZE, ICON_SIZE) : null;
        } catch (IOException ex) {
            LOGGER.debugf(ex, "Failed to load icon %s", name);
            return null;
        }
    }

    private static void browse(URI uri) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
            } else {
                LOGGER.warnf("Cannot open browser, go to %s", uri);
            }
        } catch (IOException | RuntimeException ex) {
            LOGGER.warnf(ex, "Failed to open browser at %s", uri);
        }
    }

    private static void copyToClipboard(String text) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        } catch (IllegalStateException ex) {
            LOGGER.warnf(ex, "Failed to copy %s to clipboard", text);
        }
    }

    static URI getBaseUri(String host, int port) {
        String browsableHost = switch (host) {
            case "0.0.0.0", "::", "[::]" -> "localhost";
            default -> host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
        };
        return URI.create("http://" + browsableHost + ":" + port + "/");
    }
}
