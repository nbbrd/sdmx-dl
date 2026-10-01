package sdmxdl.script;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

/**
 * Target of a generated script, defined by a language and a transport.
 * <p>
 * The language is the one of the generated script (e.g. {@code python}, {@code r},
 * {@code powershell}) while the transport is the way the script reaches sdmx-dl (e.g.
 * {@link #CLI_TRANSPORT}, {@link #REST_TRANSPORT}).
 */
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class ScriptTarget {

    /**
     * Transport that calls the command-line tool.
     */
    public static final String CLI_TRANSPORT = "cli";

    /**
     * Transport that calls the web service.
     */
    public static final String REST_TRANSPORT = "rest";

    /**
     * Transport that calls the Java library.
     */
    public static final String JAVA_TRANSPORT = "java";

    private static final char SEPARATOR = '/';

    @StaticFactoryMethod
    public static @NonNull ScriptTarget parse(@NonNull CharSequence text) throws IllegalArgumentException {
        String value = text.toString();
        int index = value.indexOf(SEPARATOR);
        if (index <= 0 || index == value.length() - 1 || value.indexOf(SEPARATOR, index + 1) != -1) {
            throw new IllegalArgumentException("Invalid script target: '" + value + "'");
        }
        return of(value.substring(0, index), value.substring(index + 1));
    }

    @NonNull String language;

    @NonNull String transport;

    @Override
    public String toString() {
        return language + SEPARATOR + transport;
    }
}
