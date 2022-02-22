package internal.sdmxdl.web.spi;

import java.util.logging.Level;
import java.util.logging.Logger;

@lombok.AllArgsConstructor
public final class FailsafeLogging {

    public static FailsafeLogging of(Class<?> type) {
        return new FailsafeLogging(Logger.getLogger(type.getName()), Level.WARNING);
    }

    private final Logger log;
    private final Level level;

    public void logUnexpectedError(String msg, RuntimeException ex) {
        if (log.isLoggable(level)) {
            log.log(level, msg, ex);
        }
    }

    public void logUnexpectedNull(String msg) {
        if (log.isLoggable(level)) {
            log.log(level, msg);
        }
    }
}
