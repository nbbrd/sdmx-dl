package tests.sdmxdl.ext;

import lombok.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.Series;
import sdmxdl.ext.SeriesMeta;
import sdmxdl.ext.spi.Dialect;

import java.util.function.Function;

@lombok.RequiredArgsConstructor
public final class MockedDialect implements Dialect {

    @lombok.Getter
    private final String name;

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public @NonNull Function<Series, SeriesMeta> getMetaFactory(DataStructure dsd) {
        return series -> SeriesMeta.EMPTY;
    }
}
