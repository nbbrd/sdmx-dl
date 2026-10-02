package sdmxdl.script;

import java.util.regex.Pattern;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

/**
 * Target of a generated script, defined by a language and a transport.
 * <p>
 * The language is the one of the generated script (e.g. {@link #PYTHON_LANGUAGE},
 * {@link #R_LANGUAGE}) while the transport is the way the script reaches sdmx-dl (e.g.
 * {@link #CLI_TRANSPORT}, {@link #REST_TRANSPORT}).
 * <p>
 * Both parts are lowercase identifiers made of letters, digits, {@code -} and {@code _} so that
 * {@link #toString()} can always be parsed back with {@link #parse(CharSequence)}.
 */
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
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

    public static final String BASH_LANGUAGE = "bash";

    public static final String BATCH_LANGUAGE = "batch";

    public static final String JBANG_LANGUAGE = "jbang";

    public static final String JUPYTER_LANGUAGE = "jupyter";

    public static final String POWERQUERY_LANGUAGE = "powerquery";

    public static final String POWERSHELL_LANGUAGE = "powershell";

    public static final String PYTHON_LANGUAGE = "python";

    public static final String R_LANGUAGE = "r";

    private static final char SEPARATOR = '/';

    private static final Pattern PART = Pattern.compile("[a-z0-9][a-z0-9_-]*");

    @StaticFactoryMethod
    public static @NonNull ScriptTarget of(@NonNull String language, @NonNull String transport)
            throws IllegalArgumentException {
        checkPart("language", language);
        checkPart("transport", transport);
        return new ScriptTarget(language, transport);
    }

    @StaticFactoryMethod
    public static @NonNull ScriptTarget parse(@NonNull CharSequence text) throws IllegalArgumentException {
        String value = text.toString();
        int index = value.indexOf(SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid script target: '" + value + "'");
        }
        try {
            return of(value.substring(0, index), value.substring(index + 1));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid script target: '" + value + "'", ex);
        }
    }

    private static void checkPart(String name, String value) throws IllegalArgumentException {
        if (!PART.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Invalid script " + name + ": '" + value + "', expecting " + PART.pattern());
        }
    }

    @NonNull String language;

    @NonNull String transport;

    @Override
    public String toString() {
        return language + SEPARATOR + transport;
    }
}
