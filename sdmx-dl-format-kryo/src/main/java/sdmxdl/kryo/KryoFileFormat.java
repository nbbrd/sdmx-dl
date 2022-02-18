/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.kryo;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.*;
import com.esotericsoftware.kryo.kryo5.util.Pool;
import lombok.AccessLevel;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import sdmxdl.*;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.SdmxWebMonitorReport;
import sdmxdl.web.SdmxWebMonitorReports;
import sdmxdl.web.SdmxWebStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class KryoFileFormat<T> implements FileParser<T>, FileFormatter<T> {

    public static final KryoFileFormat<SdmxRepository> REPOSITORY = new KryoFileFormat<>(SdmxRepository.class);
    public static final KryoFileFormat<SdmxWebMonitorReports> MONITOR = new KryoFileFormat<>(SdmxWebMonitorReports.class);

    @lombok.NonNull
    private final Class<T> type;

    @Override
    public T parseStream(InputStream resource) throws IOException {
        Kryo kryo = KRYO_POOL.obtain();
        Input input = INPUT_POOL.obtain();
        try {
            input.setInputStream(resource);
            return kryo.readObject(input, type);
        } catch (RuntimeException ex) {
            throw new IOException(ex);
        } finally {
            input.setInputStream(null);
            INPUT_POOL.free(input);
            KRYO_POOL.free(kryo);
        }
    }

    @Override
    public void formatStream(T value, OutputStream resource) throws IOException {
        Kryo kryo = KRYO_POOL.obtain();
        Output output = OUTPUT_POOL.obtain();
        try {
            output.setOutputStream(resource);
            kryo.writeObject(output, value);
            output.flush();
        } catch (RuntimeException ex) {
            throw new IOException(ex);
        } finally {
            output.setOutputStream(null);
            OUTPUT_POOL.free(output);
            KRYO_POOL.free(kryo);
        }
    }

    static {
        disableUnsafeIfNotConfigured();
    }

    public static final String KRYO_UNSAFE_PROPERTY = "kryo.unsafe";

    private static void disableUnsafeIfNotConfigured() {
        if (System.getProperty(KRYO_UNSAFE_PROPERTY) == null) {
            System.setProperty(KRYO_UNSAFE_PROPERTY, Boolean.FALSE.toString());
        }
    }

    private static final Pool<Kryo> KRYO_POOL = new Pool<Kryo>(true, false, 8) {
        @Override
        protected Kryo create() {
            return newKryo();
        }
    };

    private static final Pool<Input> INPUT_POOL = new Pool<Input>(true, false, 16) {
        @Override
        protected Input create() {
            return new Input(4096);
        }
    };

    private static final Pool<Output> OUTPUT_POOL = new Pool<Output>(true, false, 16) {
        @Override
        protected Output create() {
            return new Output(4096, 4096);
        }
    };

    private static Kryo newKryo() {
        Kryo result = new Kryo();
        result.setReferences(false);
        result.setRegistrationRequired(true);

        result.register(SdmxRepository.class, new SdmxRepositorySerializer());
        result.register(DataStructure.class, new DataStructureSerializer());
        result.register(DataStructureRef.class, new DataStructureRefSerializer());
        result.register(Dataflow.class, new DataflowSerializer());
        result.register(DataflowRef.class, new DataflowRefSerializer());
        result.register(Codelist.class, new CodelistSerializer());
        result.register(CodelistRef.class, new CodelistRefSerializer());
        result.register(Frequency.class, new FrequencySerializer());
        result.register(Key.class, new KeySerializer());
        result.register(DataQuery.class, new DataQuerySerializer());
        result.register(DataDetail.class, new DefaultSerializers.EnumSerializer(DataDetail.class));
        result.register(DataSet.class, new DataSetSerializer());
        result.register(Series.class, new SeriesSerializer());
        result.register(Obs.class, new ObsSerializer());
        result.register(Dimension.class, new DimensionSerializer());
        result.register(Attribute.class, new AttributeSerializer());
        result.register(SdmxWebMonitorReports.class, new SdmxWebMonitorReportsSerializer());
        result.register(SdmxWebMonitorReport.class, new SdmxWebMonitorReportSerializer());
        result.register(SdmxWebStatus.class, new DefaultSerializers.EnumSerializer(SdmxWebStatus.class));

        result.register(ArrayList.class, new CollectionSerializer<>());
        result.register(LocalDateTime.class, new TimeSerializers.LocalDateTimeSerializer());
        result.register(Instant.class, new TimeSerializers.InstantSerializer());

        return result;
    }

    private static final class CustomCollectionSerializer<T> extends CollectionSerializer<Collection<T>> {

        CustomCollectionSerializer(Class<T> elementType) {
            setImmutable(true);
            setAcceptsNull(false);
            setElementsCanBeNull(false);
            setElementClass(elementType);
        }
    }

    private static final class StringMapSerializer extends MapSerializer<Map<String, String>> {

        StringMapSerializer() {
            setImmutable(true);
            setAcceptsNull(false);
            setKeysCanBeNull(false);
            setValuesCanBeNull(false);
            setKeyClass(String.class);
            setValueClass(String.class);
        }
    }

    private static abstract class ResourceRefSerializer<T extends ResourceRef<T>> extends ImmutableSerializer<T> {

        protected abstract T read(String input);

        @Override
        public void write(Kryo kryo, Output output, T t) {
            output.writeString(t.toString());
        }

        @Override
        public T read(Kryo kryo, Input input, Class<? extends T> type) {
            return read(input.readString());
        }
    }

    private static final class SdmxRepositorySerializer extends ImmutableSerializer<SdmxRepository> {

        private final Serializer<Collection<DataStructure>> structures = new CustomCollectionSerializer<>(DataStructure.class);
        private final Serializer<Collection<Dataflow>> flows = new CustomCollectionSerializer<>(Dataflow.class);
        private final Serializer<Collection<DataSet>> dataSets = new CustomCollectionSerializer<>(DataSet.class);

        @Override
        public void write(Kryo kryo, Output output, SdmxRepository t) {
            output.writeString(t.getName());
            kryo.writeObject(output, t.getStructures(), structures);
            kryo.writeObject(output, t.getFlows(), flows);
            kryo.writeObject(output, t.getDataSets(), dataSets);
            output.writeBoolean(t.isDetailSupported());
            kryo.writeObjectOrNull(output, t.getCreationTime(), Instant.class);
            kryo.writeObjectOrNull(output, t.getExpirationTime(), Instant.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        public SdmxRepository read(Kryo kryo, Input input, Class<? extends SdmxRepository> type) {
            return SdmxRepository
                    .builder()
                    .name(input.readString())
                    .structures(kryo.readObject(input, ArrayList.class, structures))
                    .flows(kryo.readObject(input, ArrayList.class, flows))
                    .dataSets(kryo.readObject(input, ArrayList.class, dataSets))
                    .detailSupported(input.readBoolean())
                    .creationTime(kryo.readObjectOrNull(input, Instant.class))
                    .expirationTime(kryo.readObjectOrNull(input, Instant.class))
                    .build();
        }
    }

    private static final class DataStructureSerializer extends ImmutableSerializer<DataStructure> {

        private final Serializer<Collection<Dimension>> dimensions = new CustomCollectionSerializer<>(Dimension.class);
        private final Serializer<Collection<Attribute>> attributes = new CustomCollectionSerializer<>(Attribute.class);

        @Override
        public void write(Kryo kryo, Output output, DataStructure t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getDimensions(), dimensions);
            kryo.writeObject(output, t.getAttributes(), attributes);
            output.writeString(t.getTimeDimensionId());
            output.writeString(t.getPrimaryMeasureId());
            output.writeString(t.getLabel());
        }

        @SuppressWarnings("unchecked")
        @Override
        public DataStructure read(Kryo kryo, Input input, Class<? extends DataStructure> type) {
            return DataStructure
                    .builder()
                    .ref(kryo.readObject(input, DataStructureRef.class))
                    .dimensions(kryo.readObject(input, ArrayList.class, dimensions))
                    .attributes(kryo.readObject(input, ArrayList.class, attributes))
                    .timeDimensionId(input.readString())
                    .primaryMeasureId(input.readString())
                    .label(input.readString())
                    .build();
        }
    }

    private static final class DataStructureRefSerializer extends ResourceRefSerializer<DataStructureRef> {

        @Override
        protected DataStructureRef read(String input) {
            return DataStructureRef.parse(input);
        }
    }

    private static final class DataflowSerializer extends ImmutableSerializer<Dataflow> {

        @Override
        public void write(Kryo kryo, Output output, Dataflow t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getStructureRef());
            output.writeString(t.getLabel());
        }

        @Override
        public Dataflow read(Kryo kryo, Input input, Class<? extends Dataflow> type) {
            return Dataflow.of(
                    kryo.readObject(input, DataflowRef.class),
                    kryo.readObject(input, DataStructureRef.class),
                    input.readString()
            );
        }
    }

    private static final class DataflowRefSerializer extends ResourceRefSerializer<DataflowRef> {

        @Override
        protected DataflowRef read(String input) {
            return DataflowRef.parse(input);
        }
    }

    private static final class CodelistSerializer extends ImmutableSerializer<Codelist> {

        private final Serializer<Map<String, String>> codes = new StringMapSerializer();

        @Override
        public void write(Kryo kryo, Output output, Codelist t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getCodes(), codes);
        }

        @Override
        public Codelist read(Kryo kryo, Input input, Class<? extends Codelist> type) {
            return Codelist
                    .builder()
                    .ref(kryo.readObject(input, CodelistRef.class))
                    .codes(kryo.readObject(input, HashMap.class, this.codes))
                    .build();
        }
    }

    private static final class CodelistRefSerializer extends ResourceRefSerializer<CodelistRef> {

        @Override
        protected CodelistRef read(String input) {
            return CodelistRef.parse(input);
        }
    }

    private static final class DataSetSerializer extends ImmutableSerializer<DataSet> {

        private final Serializer<Collection<Series>> data = new CustomCollectionSerializer<>(Series.class);

        @Override
        public void write(Kryo kryo, Output output, DataSet t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getQuery());
            kryo.writeObject(output, t.getData(), data);
        }

        @SuppressWarnings("unchecked")
        @Override
        public DataSet read(Kryo kryo, Input input, Class<? extends DataSet> type) {
            return DataSet
                    .builder()
                    .ref(kryo.readObject(input, DataflowRef.class))
                    .query(kryo.readObject(input, DataQuery.class))
                    .data(kryo.readObject(input, ArrayList.class, data))
                    .build();
        }
    }

    private static final class FrequencySerializer extends ImmutableSerializer<Frequency> {

        private final Frequency[] freqs = Frequency.values();

        @Override
        public void write(Kryo kryo, Output output, Frequency t) {
            output.writeInt(t.ordinal(), true);
        }

        @Override
        public Frequency read(Kryo kryo, Input input, Class<? extends Frequency> type) {
            return freqs[input.readInt(true)];
        }
    }

    private static final class KeySerializer extends ImmutableSerializer<Key> {

        @Override
        public void write(Kryo kryo, Output output, Key t) {
            output.writeString(t.toString());
        }

        @Override
        public Key read(Kryo kryo, Input input, Class<? extends Key> type) {
            return Key.parse(input.readString());
        }
    }

    private static final class DataQuerySerializer extends ImmutableSerializer<DataQuery> {

        @Override
        public void write(Kryo kryo, Output output, DataQuery t) {
            kryo.writeObject(output, t.getKey());
            kryo.writeObject(output, t.getDetail());
        }

        @Override
        public DataQuery read(Kryo kryo, Input input, Class<? extends DataQuery> type) {
            return DataQuery.of(kryo.readObject(input, Key.class), kryo.readObject(input, DataDetail.class));
        }
    }

    private static final class SeriesSerializer extends ImmutableSerializer<Series> {

        private final Serializer<Collection<Obs>> obs = new CustomCollectionSerializer<>(Obs.class);
        private final Serializer<Map<String, String>> seriesMeta = new StringMapSerializer();

        @Override
        public void write(Kryo kryo, Output output, Series t) {
            kryo.writeObject(output, t.getKey());
            kryo.writeObject(output, t.getFreq());
            kryo.writeObject(output, t.getObs(), obs);
            kryo.writeObject(output, t.getMeta(), seriesMeta);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Series read(Kryo kryo, Input input, Class<? extends Series> type) {
            return Series
                    .builder()
                    .key(kryo.readObject(input, Key.class))
                    .freq(kryo.readObject(input, Frequency.class))
                    .obs(kryo.readObject(input, ArrayList.class, obs))
                    .meta(kryo.readObject(input, HashMap.class, seriesMeta))
                    .build();
        }
    }

    private static final class ObsSerializer extends ImmutableSerializer<Obs> {

        private final Serializer<Map<String, String>> obsMeta = new StringMapSerializer();

        @Override
        public void write(Kryo kryo, Output output, Obs t) {
            kryo.writeObjectOrNull(output, t.getPeriod(), LocalDateTime.class);
            kryo.writeObjectOrNull(output, t.getValue(), Double.class);
            kryo.writeObject(output, t.getMeta(), obsMeta);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Obs read(Kryo kryo, Input input, Class<? extends Obs> type) {
            return Obs
                    .builder()
                    .period(kryo.readObjectOrNull(input, LocalDateTime.class))
                    .value(kryo.readObjectOrNull(input, Double.class))
                    .meta(kryo.readObject(input, HashMap.class, obsMeta))
                    .build();
        }
    }

    private static final class DimensionSerializer extends ImmutableSerializer<Dimension> {

        @Override
        public void write(Kryo kryo, Output output, Dimension t) {
            output.writeString(t.getId());
            output.writeString(t.getLabel());
            kryo.writeObject(output, t.getCodelist());
            output.writeInt(t.getPosition(), true);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Dimension read(Kryo kryo, Input input, Class<? extends Dimension> type) {
            return Dimension
                    .builder()
                    .id(input.readString())
                    .label(input.readString())
                    .codelist(kryo.readObject(input, Codelist.class))
                    .position(input.readInt(true))
                    .build();
        }
    }

    private static final class AttributeSerializer extends ImmutableSerializer<Attribute> {

        @Override
        public void write(Kryo kryo, Output output, Attribute t) {
            output.writeString(t.getId());
            output.writeString(t.getLabel());
            kryo.writeObjectOrNull(output, t.getCodelist(), Codelist.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Attribute read(Kryo kryo, Input input, Class<? extends Attribute> type) {
            return Attribute
                    .builder()
                    .id(input.readString())
                    .label(input.readString())
                    .codelist(kryo.readObjectOrNull(input, Codelist.class))
                    .build();
        }
    }

    private static final class SdmxWebMonitorReportsSerializer extends ImmutableSerializer<SdmxWebMonitorReports> {

        private final Serializer<Collection<SdmxWebMonitorReport>> reports = new CustomCollectionSerializer<>(SdmxWebMonitorReport.class);

        @Override
        public void write(Kryo kryo, Output output, SdmxWebMonitorReports t) {
            output.writeString(t.getUriScheme());
            kryo.writeObject(output, t.getReports(), reports);
            kryo.writeObject(output, t.getCreationTime());
            kryo.writeObject(output, t.getExpirationTime());
        }

        @Override
        public SdmxWebMonitorReports read(Kryo kryo, Input input, Class<? extends SdmxWebMonitorReports> type) {
            return SdmxWebMonitorReports
                    .builder()
                    .uriScheme(input.readString())
                    .reports(kryo.readObject(input, ArrayList.class, reports))
                    .creationTime(kryo.readObject(input, Instant.class))
                    .expirationTime(kryo.readObject(input, Instant.class))
                    .build();
        }
    }

    private static final class SdmxWebMonitorReportSerializer extends ImmutableSerializer<SdmxWebMonitorReport> {

        @Override
        public void write(Kryo kryo, Output output, SdmxWebMonitorReport t) {
            output.writeString(t.getSource());
            kryo.writeObject(output, t.getStatus());
            kryo.writeObjectOrNull(output, t.getUptimeRatio(), Double.class);
            kryo.writeObjectOrNull(output, t.getAverageResponseTime(), Long.class);
        }

        @Override
        public SdmxWebMonitorReport read(Kryo kryo, Input input, Class<? extends SdmxWebMonitorReport> type) {
            return SdmxWebMonitorReport
                    .builder()
                    .source(input.readString())
                    .status(kryo.readObject(input, SdmxWebStatus.class))
                    .uptimeRatio(kryo.readObjectOrNull(input, Double.class))
                    .averageResponseTime(kryo.readObjectOrNull(input, Long.class))
                    .build();
        }
    }
}
