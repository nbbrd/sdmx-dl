package sdmxdl.provider.ri.http;

import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.DumpingDecorator;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import sdmxdl.EventListener;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.File;

import static sdmxdl.web.spi.Driver.DRIVER_PROPERTY_PREFIX;

/**
 * Decorator for {@link HttpClient} that enables dumping of downloaded HTTP assets to a specified folder.
 * <p>
 * This decorator wraps an HTTP client and optionally copies all downloaded assets to a local directory
 * for inspection and debugging purposes. The dumping behavior is controlled by the
 * {@link #DUMP_FOLDER_PROPERTY} property.
 * </p>
 */
public final class DumpingDecoration implements HttpDecoration {

    /**
     * Defines a folder where downloaded assets are copied. Default value is <code>null</code> and disables copying.
     */
    @PropertyDefinition
    public static final Property<File> DUMP_FOLDER_PROPERTY =
            Property.of(DRIVER_PROPERTY_PREFIX + ".dumpFolder", null, Parser.onFile(), Formatter.onFile());

    /**
     * Delegates HTTP client decoration to the support implementation.
     */
    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Dumping")
            .property(DUMP_FOLDER_PROPERTY)
            .superFactory(DumpingDecoration::decorate)
            .build();

    /**
     * Decorates an HTTP client with dumping capabilities.
     * <p>
     * If a dump folder is configured via {@link #DUMP_FOLDER_PROPERTY}, wraps the client with
     * a {@link DumpingDecorator} that copies downloaded content to disk. Events are reported
     * via the provided event listener if available.
     * </p>
     *
     * @param d the HTTP client factory to create the base client
     * @param s the web source providing configuration and properties
     * @param c the web context containing event listeners and other runtime configuration
     * @return an HTTP client, optionally wrapped with dumping functionality
     */
    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        HttpClient original = d.create(s, c);
        File dumpFolder = DUMP_FOLDER_PROPERTY.get(s.getProperties());
        if (dumpFolder != null) {
            EventListener onEvent = c.getEventListener(s);
            return new DumpingDecorator(original, dumpFolder.toPath(), onEvent != null ? file -> onEvent.accept(MARKER, "Dumping " + file.toUri()) : ignore -> {
            });
        }
        return original;
    }
}
