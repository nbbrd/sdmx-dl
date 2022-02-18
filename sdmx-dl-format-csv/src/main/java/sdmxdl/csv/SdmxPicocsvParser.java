package sdmxdl.csv;

import nbbrd.design.MightBePromoted;
import nbbrd.io.text.Parser;
import nbbrd.io.text.TextBuffers;
import nbbrd.io.text.TextParser;
import nbbrd.picocsv.Csv;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.ObsParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nbbrd.io.text.TextResource.newBufferedReader;
import static sdmxdl.DataSet.toDataSet;

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
    private final Csv.ReaderOptions options = Csv.ReaderOptions.DEFAULT;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Locale locale = Locale.ROOT;

    @Override
    public @NonNull DataSet parseReader(@NonNull Reader reader) throws IOException {
        try (Csv.Reader csv = newCsvReader(reader, TextBuffers.UNKNOWN)) {
            return parseCsv(csv);
        }
    }

    @Override
    public @NonNull DataSet parseStream(@NonNull InputStream stream, @NonNull Charset charset) throws IOException {
        CharsetDecoder decoder = charset.newDecoder();
        try (Csv.Reader csv = newCsvReader(newBufferedReader(stream, decoder), TextBuffers.of(stream, decoder))) {
            return parseCsv(csv);
        }
    }

    @NonNull
    public DataSet parseCsv(Csv.@NonNull Reader reader) throws IOException {
        List<String> header = readHeader(reader);

        int minHeaderSize = 3 + dsd.getDimensions().size();
        if (header.size() < minHeaderSize) {
            throw new IOException("Invalid header size");
        }

        if (!SdmxCsvFields.DATAFLOW.equals(header.get(0))) {
            throw new IOException("Invalid dataflow header");
        }

        if (!dsd.getTimeDimensionId().equals(header.get(1 + dsd.getDimensions().size()))) {
            throw new IOException("Invalid time dimension header");
        }

        if (!SdmxCsvFields.OBS_VALUE.equals(header.get(1 + dsd.getDimensions().size() + 1))) {
            throw new IOException("Invalid obs value header");
        }

        int seriesKeyIndex = header.subList(minHeaderSize, header.size()).indexOf(SdmxCsvFields.SERIESKEY);
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
        Parser<DataflowRef> refParser = SdmxCsvFields.getDataflowRefParser();

        DataflowRef dataflowRef = DataflowRef.of(null, "", null);

        Map<Key, Series.Builder> data = new HashMap<>();
        Key.Builder keyBuilder = Key.builder(dsd);
        Obs.Builder obs = Obs.builder();
        while (skipComments(reader)) {
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

            Series.Builder series = data.computeIfAbsent(keyBuilder.build(), z -> Series.builder().key(z).freq(obsParser.head(keyBuilder, o -> null).getFrequency()));
            series.obs(obs
                    .clearMeta()
                    .period(obsParser.parsePeriod(o -> null))
                    .value(obsParser.parseValue())
                    .build()
            );
        }

        return data.values()
                .stream()
                .map(Series.Builder::build)
                .collect(toDataSet(DataRef.of(dataflowRef)));
    }

    private List<String> readHeader(Csv.Reader reader) throws IOException {
        List<String> result = new ArrayList<>();
        if (!skipComments(reader)) {
            throw new IOException("Missing header");
        }
        while (reader.readField()) {
            result.add(reader.toString());
        }
        return result;
    }

    private Csv.Reader newCsvReader(Reader charReader, TextBuffers buffers) throws IOException {
        return Csv.Reader.of(format, options, charReader, buffers.getCharBufferSize());
    }

    @MightBePromoted
    private static boolean skipComments(Csv.Reader reader) throws IOException {
        while (reader.readLine()) {
            if (!reader.isComment()) {
                return true;
            }
        }
        return false;
    }
}
