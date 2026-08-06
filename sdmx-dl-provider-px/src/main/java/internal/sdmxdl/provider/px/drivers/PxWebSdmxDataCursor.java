package internal.sdmxdl.provider.px.drivers;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.time.ObservationalTimePeriod;
import sdmxdl.format.xml.SdmxXmlStreams;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@VisibleForTesting
@lombok.AllArgsConstructor
public final class PxWebSdmxDataCursor implements DataCursor {

    public static @NonNull FileParser<DataCursor> parserOf(@NonNull Structure dsd) {
        return SdmxXmlStreams
                .genericData20(fixStructureDimensions(dsd), ObsParser::newDefault)
                .andThen(PxWebSdmxDataCursor::new);
    }

    private final @NonNull DataCursor delegate;

    @Override
    public boolean nextSeries() throws IOException {
        return delegate.nextSeries();
    }

    @Override
    public @NonNull Key getSeriesKey() throws IOException, IllegalStateException {
        String keyAsString = delegate.getSeriesKey().toString();
        return Key.parse(keyAsString.substring(keyAsString.indexOf('.') + 1));
    }

    @Override
    @Nullable
    public String getSeriesAttribute(@NonNull String key) throws IOException, IllegalStateException {
        return delegate.getSeriesAttribute(key);
    }

    @Override
    @NonNull
    public Map<String, String> getSeriesAttributes() throws IOException, IllegalStateException {
        return delegate.getSeriesAttributes();
    }

    @Override
    public boolean nextObs() throws IOException, IllegalStateException {
        return delegate.nextObs();
    }

    @Override
    public @Nullable ObservationalTimePeriod getObsPeriod() throws IOException, IllegalStateException {
        return delegate.getObsPeriod();
    }

    @Override
    public @Nullable Double getObsValue() throws IOException, IllegalStateException {
        return delegate.getObsValue();
    }

    @Override
    @NonNull
    public Map<String, String> getObsAttributes() throws IllegalStateException {
        return Collections.emptyMap();
    }

    @Override
    public @Nullable String getObsAttribute(@NonNull String key) throws IllegalStateException {
        return null;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    private static Structure fixStructureDimensions(Structure dsd) {
        return dsd
                .toBuilder()
                .clearDimensions()
                .dimension(MANDATORY_FREQ_AS_FIRST_DIMENSION)
                .dimensions(dsd.getDimensions()
                        .stream()
                        .map(dimension -> dimension
                                .toBuilder()
                                .id(convertDimensionNameToId(dimension.getName()))
                                .build())
                        .collect(toList()))
                .build();
    }

    /**
     * Convert a PxWeb variable text to an SDMX dimension ID.
     * <p>
     * Surprisingly, PxWeb variable code is not used as SDMX dimension ID when getting data.
     * The PxWeb variable text is used instead after being normalized to a valid SDMX ID.
     * Note that the PxWeb variable text is dependent of the requested language.
     *
     * @param name the name to be converted
     * @return the converted ID
     */
//    @VisibleForTesting
    public static String convertDimensionNameToId(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "");
    }

    private static final Dimension MANDATORY_FREQ_AS_FIRST_DIMENSION = Dimension
            .builder()
            .id("FREQ")
            .name("")
            .codelist(Codelist
                    .builder()
                    .ref(CodelistRef.parse("FREQ"))
                    .build())
            .build();
}
