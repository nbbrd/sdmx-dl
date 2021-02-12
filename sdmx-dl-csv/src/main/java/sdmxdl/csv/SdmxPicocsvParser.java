package sdmxdl.csv;

import nbbrd.io.text.Parser;
import nbbrd.io.text.TextParser;
import nbbrd.picocsv.Csv;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.ObsParser;
import sdmxdl.repo.DataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@lombok.Builder(toBuilder = true)
public final class SdmxPicocsvParser implements TextParser<DataSet> {

    @lombok.NonNull
    private final DataStructure dsd;

    @lombok.NonNull
    private final ObsFactory factory;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Csv.Format format = Csv.Format.RFC4180;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Csv.Parsing parsing = Csv.Parsing.STRICT;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Locale encoding = Locale.ROOT;

    @Override
    public @NonNull DataSet parseReader(@NonNull Reader reader) throws IOException {
        try (Csv.Reader csv = Csv.Reader.of(reader, format, parsing)) {
            return parse(csv);
        }
    }

    @Override
    public @NonNull DataSet parseStream(@NonNull InputStream inputStream, @NonNull Charset charset) throws IOException {
        try (Csv.Reader csv = Csv.Reader.of(inputStream, charset, format, parsing)) {
            return parse(csv);
        }
    }

    @NonNull
    public DataSet parse(Csv.@NonNull Reader reader) throws IOException {
        List<String> header = readHeader(reader);

        int minHeaderSize = 3 + dsd.getDimensions().size();
        if (header.size() < minHeaderSize) {
            throw new IOException("Invalid header size");
        }

        if (!SdmxCsv.DATAFLOW.equals(header.get(0))) {
            throw new IOException("Invalid dataflow header");
        }

        if (!dsd.getTimeDimensionId().equals(header.get(1 + dsd.getDimensions().size()))) {
            throw new IOException("Invalid time dimension header");
        }

        if (!SdmxCsv.OBS_VALUE.equals(header.get(1 + dsd.getDimensions().size() + 1))) {
            throw new IOException("Invalid obs value header");
        }

        int seriesKeyIndex = header.subList(minHeaderSize, header.size()).indexOf(SdmxCsv.SERIESKEY);
        if (seriesKeyIndex != -1) {
            Map<String, Attribute> attributes = dsd.getAttributes().stream().collect(Collectors.toMap(Attribute::getId, Function.identity()));
            for (int i = minHeaderSize; i < seriesKeyIndex; i++) {
                String attributeId = header.get(i);
                if (!attributes.containsKey(attributeId)) {
                    throw new IOException("Unknown attribute header");
                }
            }
        }

        ObsParser obsParser = factory.getObsParser(dsd);
        Parser<DataflowRef> refParser = SdmxCsv.getDataflowRefParser();

        DataflowRef dataflowRef = DataflowRef.of(null, "", null);

        Map<Key, Series.Builder> data = new HashMap<>();
        Key.Builder keyBuilder = Key.builder(dsd);
        while (reader.readLine()) {
            if (!reader.readField()) {
                throw new IOException("Missing dataflow field");
            }
            dataflowRef = refParser.parse(reader);

            keyBuilder.clear();
            for (int i = 0; i < keyBuilder.size(); i++) {
                if (!reader.readField()) {
                    throw new IOException("Missing dimension field");
                }
                keyBuilder.put(header.get(1 + i), reader.toString());
            }

            if (!reader.readField()) {
                throw new IOException("Missing time dimension field");
            }
            obsParser.period(reader.toString());

            if (!reader.readField()) {
                throw new IOException("Missing obs value field");
            }
            obsParser.value(reader.toString());

            Series.Builder series = data.computeIfAbsent(keyBuilder.build(), z -> Series.builder().key(z).freq(obsParser.frequency(keyBuilder, o -> null).getFrequency()));
            series.obs(Obs.of(obsParser.parsePeriod(), obsParser.parseValue()));
        }

        return DataSet
                .builder()
                .ref(dataflowRef)
                .data(data.values().stream().map(Series.Builder::build).collect(Collectors.toList()))
                .build();
    }

    private List<String> readHeader(Csv.Reader reader) throws IOException {
        List<String> result = new ArrayList<>();
        if (!reader.readLine()) {
            throw new IOException("Missing header");
        }
        while (reader.readField()) {
            result.add(reader.toString());
        }
        return result;
    }
}
