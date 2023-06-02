package internal.sdmxdl.cli.ext;

import picocli.CommandLine;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.format.spi.FileFormatProviderLoader;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

@lombok.Getter
@lombok.Setter
public class CacheOptions {

    @CommandLine.Option(
            names = {"--no-cache"},
            defaultValue = "false",
            descriptionKey = "cli.noCache"
    )
    private boolean noCache;

    @CommandLine.Option(
            names = {"--no-cache-compression"},
            defaultValue = "false",
            descriptionKey = "cli.noCacheCompression",
            hidden = true
    )
    private boolean noCacheCompression;

    @CommandLine.Option(
            names = {"--cache-folder"},
            paramLabel = "<folder>",
            descriptionKey = "cli.cacheFolder",
            hidden = true
    )
    private File cacheFolder;

    @CommandLine.Option(
            names = {"--cache-format"},
            paramLabel = "<format>",
            descriptionKey = "cli.cacheFormat",
            defaultValue = "PROTOBUF",
            converter = FileFormatters.class,
            hidden = true
    )
    private FileFormatProvider cacheFormat = PROVIDERS.stream()
            .filter(provider -> provider.getId().equals("PROTOBUF"))
            .findFirst()
            .orElseThrow(NoSuchElementException::new);

    private static final List<FileFormatProvider> PROVIDERS = FileFormatProviderLoader.load();

    private static final class FileFormatters implements CommandLine.ITypeConverter<FileFormatProvider> {

        @Override
        public FileFormatProvider convert(String value) throws Exception {
            return PROVIDERS.stream()
                    .filter(provider -> provider.getId().equals(value))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
        }
    }
}
