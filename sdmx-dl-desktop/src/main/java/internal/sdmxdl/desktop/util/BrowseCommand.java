package internal.sdmxdl.desktop.util;

import ec.util.various.swing.JCommand;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrowseCommand<C> extends JCommand<C> {

    @StaticFactoryMethod
    public static <T> @NonNull BrowseCommand<T> ofURI(@NonNull Function<? super T, ? extends URI> toURI) {
        return new BrowseCommand<>(toURI);
    }

    @StaticFactoryMethod
    public static <T> @NonNull BrowseCommand<T> ofURL(@NonNull Function<? super T, ? extends URL> toURL) {
        return new BrowseCommand<>(toURL.andThen(BrowseCommand::toURI));
    }

    private final @NonNull Function<? super C, ? extends URI> toURI;

    @Override
    public boolean isEnabled(@NonNull C component) {
        return BROWSING_ENABLED && toURI.apply(component) != null;
    }

    @Override
    public void execute(@NonNull C component) throws Exception {
        Desktop.getDesktop().browse(requireNonNull(toURI.apply(component)));
    }

    private static final boolean BROWSING_ENABLED =
            Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);

    private static URI toURI(URL url) {
        try {
            return url != null ? url.toURI() : null;
        } catch (URISyntaxException ignore) {
            return null;
        }
    }
}