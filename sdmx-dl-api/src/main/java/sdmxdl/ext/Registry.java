package sdmxdl.ext;

import internal.util.DialectLoader;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.DataStructure;
import sdmxdl.SdmxManager;
import sdmxdl.SdmxSource;
import sdmxdl.Series;
import sdmxdl.ext.spi.Dialect;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Registry {

    @StaticFactoryMethod
    public static @NonNull Registry ofServiceLoader() {
        return Registry
                .builder()
                .dialects(DialectLoader.load())
                .build();
    }


    @StaticFactoryMethod
    public static @NonNull Registry noOp() {
        return Registry.builder().build();
    }

    @lombok.NonNull
    @lombok.Singular
    List<Dialect> dialects;

    public @NonNull Function<Series, SeriesMeta> getFactory(@NonNull SdmxFileManager manager, @NonNull SdmxFileSource source, @NonNull DataStructure dsd) throws IOException {
        return get(manager, source, dsd);
    }

    public @NonNull Function<Series, SeriesMeta> getFactory(@NonNull SdmxWebManager manager, @NonNull String sourceName, @NonNull DataStructure dsd) throws IOException {
        SdmxWebSource source = manager.getSources().get(sourceName);
        return source != null ? get(manager, source, dsd) : series -> SeriesMeta.EMPTY;
    }

    private <S extends SdmxSource, M extends SdmxManager<S>> @NonNull Function<Series, SeriesMeta> get(@NonNull M manager, @NonNull S source, @NonNull DataStructure dsd) throws IOException {
        String dialectName = manager.getDialect(source).orElse(null);
        if (dialectName == null) {
            return series -> SeriesMeta.EMPTY;
        }
        return getDialects()
                .stream()
                .filter(dialect -> dialect.getName().equals(dialectName))
                .findFirst()
                .orElseThrow(() -> new IOException("Failed to find a suitable dialect for '" + source + "'"))
                .getMetaFactory(dsd);
    }
}
