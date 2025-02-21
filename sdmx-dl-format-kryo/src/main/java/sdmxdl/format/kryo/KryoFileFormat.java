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
package sdmxdl.format.kryo;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.*;
import com.esotericsoftware.kryo.kryo5.util.Pool;
import lombok.AccessLevel;
import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.ext.FileFormat;
import sdmxdl.web.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
final class KryoFileFormat<T extends HasPersistence> implements FileFormat<T> {

    @lombok.NonNull
    private final Class<T> type;

    @Override
    public @NonNull T parsePath(@NonNull Path source) throws IOException {
        try (InputStream stream = Files.newInputStream(source)) {
            return parseStream(stream);
        }
    }

    @Override
    public @NonNull T parseStream(@NonNull InputStream resource) throws IOException {
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
    public void formatPath(@NonNull T value, @NonNull Path target) throws IOException {
        try (OutputStream stream = Files.newOutputStream(target)) {
            formatStream(value, stream);
        }
    }

    @Override
    public void formatStream(@NonNull T value, @NonNull OutputStream resource) throws IOException {
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

    @Override
    public @NonNull String getFileExtension() {
        return ".kryo";
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

        result.register(Feature.class, new DefaultSerializers.EnumSerializer(Feature.class));
        result.register(DataRepository.class, new DataRepositorySerializer());
        result.register(Database.class, new DatabaseSerializer());
        result.register(DatabaseRef.class, new DatabaseRefSerializer());
        result.register(Structure.class, new StructureSerializer());
        result.register(StructureRef.class, new StructureRefSerializer());
        result.register(Flow.class, new FlowSerializer());
        result.register(FlowRef.class, new FlowRefSerializer());
        result.register(Codelist.class, new CodelistSerializer());
        result.register(CodelistRef.class, new CodelistRefSerializer());
        result.register(Key.class, new KeySerializer());
        result.register(Query.class, new QuerySerializer());
        result.register(Detail.class, new DefaultSerializers.EnumSerializer(Detail.class));
        result.register(DataSet.class, new DataSetSerializer());
        result.register(Series.class, new SeriesSerializer());
        result.register(Obs.class, new ObsSerializer());
        result.register(TimeInterval.class, new TimeIntervalSerializer());
        result.register(Duration.class, new DurationSerializer());
        result.register(Dimension.class, new DimensionSerializer());
        result.register(Attribute.class, new AttributeSerializer());
        result.register(AttributeRelationship.class, new DefaultSerializers.EnumSerializer(AttributeRelationship.class));
        result.register(MonitorReports.class, new MonitorReportsSerializer());
        result.register(MonitorReport.class, new MonitorReportSerializer());
        result.register(MonitorStatus.class, new DefaultSerializers.EnumSerializer(MonitorStatus.class));
        result.register(WebSources.class, new WebSourcesSerializer());
        result.register(WebSource.class, new WebSourceSerializer());
        result.register(Confidentiality.class, new DefaultSerializers.EnumSerializer(Confidentiality.class));

        result.register(ArrayList.class, new CollectionSerializer<>());
        result.register(LocalDateTime.class, new TimeSerializers.LocalDateTimeSerializer());
        result.register(Instant.class, new TimeSerializers.InstantSerializer());
        result.register(HashSet.class, new CollectionSerializer<>());
        result.register(URL.class, new DefaultSerializers.URLSerializer());
        result.register(URI.class, new DefaultSerializers.URISerializer());

        return result;
    }

    private static final class WebSourcesSerializer extends ImmutableSerializer<WebSources> {

        private final Serializer<Collection<WebSource>> sources = new CustomCollectionSerializer<>(WebSource.class);

        @Override
        public void write(Kryo kryo, Output output, WebSources t) {
            kryo.writeObject(output, t.getSources(), sources);
        }

        @Override
        public WebSources read(Kryo kryo, Input input, Class<? extends WebSources> type) {
            return WebSources
                    .builder()
                    .sources(kryo.readObject(input, ArrayList.class, sources))
                    .build();
        }
    }

    private static final class WebSourceSerializer extends ImmutableSerializer<WebSource> {
        private final Serializer<Map<String, String>> names = new StringMapSerializer();
        private final Serializer<Map<String, String>> properties = new StringMapSerializer();
        private final Serializer<Collection<String>> aliases = new CustomCollectionSerializer<>(String.class);

        @Override
        public void write(Kryo kryo, Output output, WebSource t) {
            output.writeString(t.getId());
            kryo.writeObject(output, t.getNames(), names);
            output.writeString(t.getDriver());
            kryo.writeObject(output, t.getConfidentiality());
            kryo.writeObject(output, t.getEndpoint());
            kryo.writeObject(output, t.getProperties(), properties);
            kryo.writeObject(output, t.getAliases(), aliases);
            kryo.writeObjectOrNull(output, t.getWebsite(), URL.class);
            kryo.writeObjectOrNull(output, t.getMonitor(), URI.class);
            kryo.writeObjectOrNull(output, t.getMonitorWebsite(), URL.class);
        }

        @Override
        public WebSource read(Kryo kryo, Input input, Class<? extends WebSource> type) {
            return WebSource
                    .builder()
                    .id(input.readString())
                    .names(kryo.readObject(input, HashMap.class, names))
                    .driver(input.readString())
                    .confidentiality(kryo.readObject(input, Confidentiality.class))
                    .endpoint(kryo.readObject(input, URI.class))
                    .properties(kryo.readObject(input, HashMap.class, properties))
                    .aliases(kryo.readObject(input, HashSet.class, aliases))
                    .website(kryo.readObjectOrNull(input, URL.class))
                    .monitor(kryo.readObjectOrNull(input, URI.class))
                    .monitorWebsite(kryo.readObjectOrNull(input, URL.class))
                    .build();
        }
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

    private static final class DataRepositorySerializer extends ImmutableSerializer<DataRepository> {

        private final Serializer<Collection<Database>> databases = new CustomCollectionSerializer<>(Database.class);
        private final Serializer<Collection<Structure>> structures = new CustomCollectionSerializer<>(Structure.class);
        private final Serializer<Collection<Flow>> flows = new CustomCollectionSerializer<>(Flow.class);
        private final Serializer<Collection<DataSet>> dataSets = new CustomCollectionSerializer<>(DataSet.class);
        private final Serializer<Collection<Feature>> features = new CustomCollectionSerializer<>(Feature.class);

        @Override
        public void write(Kryo kryo, Output output, DataRepository t) {
            output.writeString(t.getName());
            kryo.writeObject(output, t.getDatabases(), databases);
            kryo.writeObject(output, t.getStructures(), structures);
            kryo.writeObject(output, t.getFlows(), flows);
            kryo.writeObject(output, t.getDataSets(), dataSets);
            kryo.writeObject(output, t.getCreationTime());
            kryo.writeObject(output, t.getExpirationTime());
        }

        @SuppressWarnings("unchecked")
        @Override
        public DataRepository read(Kryo kryo, Input input, Class<? extends DataRepository> type) {
            return DataRepository
                    .builder()
                    .name(input.readString())
                    .databases(kryo.readObject(input, ArrayList.class, databases))
                    .structures(kryo.readObject(input, ArrayList.class, structures))
                    .flows(kryo.readObject(input, ArrayList.class, flows))
                    .dataSets(kryo.readObject(input, ArrayList.class, dataSets))
                    .creationTime(kryo.readObject(input, Instant.class))
                    .expirationTime(kryo.readObject(input, Instant.class))
                    .build();
        }
    }

    private static final class DatabaseSerializer extends ImmutableSerializer<Database> {

        @Override
        public void write(Kryo kryo, Output output, Database t) {
            kryo.writeObject(output, t.getRef());
            output.writeString(t.getName());
        }

        @Override
        public Database read(Kryo kryo, Input input, Class<? extends Database> type) {
            return new Database(
                    kryo.readObject(input, DatabaseRef.class),
                    input.readString()
            );
        }
    }

    private static final class DatabaseRefSerializer extends ImmutableSerializer<DatabaseRef> {

        @Override
        public void write(Kryo kryo, Output output, DatabaseRef t) {
            output.writeString(t.getId());
        }

        @Override
        public DatabaseRef read(Kryo kryo, Input input, Class<? extends DatabaseRef> type) {
            return DatabaseRef.parse(input.readString());
        }
    }

    private static final class StructureSerializer extends ImmutableSerializer<Structure> {

        private final Serializer<Collection<Dimension>> dimensions = new CustomCollectionSerializer<>(Dimension.class);
        private final Serializer<Collection<Attribute>> attributes = new CustomCollectionSerializer<>(Attribute.class);

        @Override
        public void write(Kryo kryo, Output output, Structure t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getDimensions(), dimensions);
            kryo.writeObject(output, t.getAttributes(), attributes);
            output.writeString(t.getTimeDimensionId());
            output.writeString(t.getPrimaryMeasureId());
            output.writeString(t.getName());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Structure read(Kryo kryo, Input input, Class<? extends Structure> type) {
            return Structure
                    .builder()
                    .ref(kryo.readObject(input, StructureRef.class))
                    .dimensions(kryo.readObject(input, ArrayList.class, dimensions))
                    .attributes(kryo.readObject(input, ArrayList.class, attributes))
                    .timeDimensionId(input.readString())
                    .primaryMeasureId(input.readString())
                    .name(input.readString())
                    .build();
        }
    }

    private static final class StructureRefSerializer extends ResourceRefSerializer<StructureRef> {

        @Override
        protected StructureRef read(String input) {
            return StructureRef.parse(input);
        }
    }

    private static final class FlowSerializer extends ImmutableSerializer<Flow> {

        @Override
        public void write(Kryo kryo, Output output, Flow t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getStructureRef());
            output.writeString(t.getName());
            output.writeString(t.getDescription());
        }

        @Override
        public Flow read(Kryo kryo, Input input, Class<? extends Flow> type) {
            return Flow
                    .builder()
                    .ref(kryo.readObject(input, FlowRef.class))
                    .structureRef(kryo.readObject(input, StructureRef.class))
                    .name(input.readString())
                    .description(input.readString())
                    .build();
        }
    }

    private static final class FlowRefSerializer extends ResourceRefSerializer<FlowRef> {

        @Override
        protected FlowRef read(String input) {
            return FlowRef.parse(input);
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
                    .ref(kryo.readObject(input, FlowRef.class))
                    .query(kryo.readObject(input, Query.class))
                    .data(kryo.readObject(input, ArrayList.class, data))
                    .build();
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

    private static final class QuerySerializer extends ImmutableSerializer<Query> {

        @Override
        public void write(Kryo kryo, Output output, Query t) {
            kryo.writeObject(output, t.getKey());
            kryo.writeObject(output, t.getDetail());
        }

        @Override
        public Query read(Kryo kryo, Input input, Class<? extends Query> type) {
            return Query
                    .builder()
                    .key(kryo.readObject(input, Key.class))
                    .detail(kryo.readObject(input, Detail.class))
                    .build();
        }
    }

    private static final class SeriesSerializer extends ImmutableSerializer<Series> {

        private final Serializer<Collection<Obs>> obs = new CustomCollectionSerializer<>(Obs.class);
        private final Serializer<Map<String, String>> seriesMeta = new StringMapSerializer();

        @Override
        public void write(Kryo kryo, Output output, Series t) {
            kryo.writeObject(output, t.getKey());
            kryo.writeObject(output, t.getObs(), obs);
            kryo.writeObject(output, t.getMeta(), seriesMeta);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Series read(Kryo kryo, Input input, Class<? extends Series> type) {
            return Series
                    .builder()
                    .key(kryo.readObject(input, Key.class))
                    .obs(kryo.readObject(input, ArrayList.class, obs))
                    .meta(kryo.readObject(input, HashMap.class, seriesMeta))
                    .build();
        }
    }

    private static final class ObsSerializer extends ImmutableSerializer<Obs> {

        private final Serializer<Map<String, String>> obsMeta = new StringMapSerializer();

        @Override
        public void write(Kryo kryo, Output output, Obs t) {
            kryo.writeObject(output, t.getPeriod());
            output.writeDouble(t.getValue());
            kryo.writeObject(output, t.getMeta(), obsMeta);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Obs read(Kryo kryo, Input input, Class<? extends Obs> type) {
            return Obs
                    .builder()
                    .period(kryo.readObject(input, TimeInterval.class))
                    .value(input.readDouble())
                    .meta(kryo.readObject(input, HashMap.class, obsMeta))
                    .build();
        }
    }

    private static final class TimeIntervalSerializer extends ImmutableSerializer<TimeInterval> {

        @Override
        public void write(Kryo kryo, Output output, TimeInterval t) {
            kryo.writeObject(output, t.getStart());
            kryo.writeObject(output, t.getDuration());
        }

        @SuppressWarnings("unchecked")
        @Override
        public TimeInterval read(Kryo kryo, Input input, Class<? extends TimeInterval> type) {
            return TimeInterval.of(kryo.readObject(input, LocalDateTime.class), kryo.readObject(input, Duration.class));
        }
    }

    private static final class DurationSerializer extends ImmutableSerializer<Duration> {

        @Override
        public void write(Kryo kryo, Output output, Duration t) {
            output.writeString(t.toString());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Duration read(Kryo kryo, Input input, Class<? extends Duration> type) {
            return Duration.parse(input.readString());
        }
    }

    private static final class DimensionSerializer extends ImmutableSerializer<Dimension> {

        @Override
        public void write(Kryo kryo, Output output, Dimension t) {
            output.writeString(t.getId());
            output.writeString(t.getName());
            kryo.writeObject(output, t.getCodelist());
            output.writeInt(t.getPosition(), true);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Dimension read(Kryo kryo, Input input, Class<? extends Dimension> type) {
            return Dimension
                    .builder()
                    .id(input.readString())
                    .name(input.readString())
                    .codelist(kryo.readObject(input, Codelist.class))
                    .position(input.readInt(true))
                    .build();
        }
    }

    private static final class AttributeSerializer extends ImmutableSerializer<Attribute> {

        @Override
        public void write(Kryo kryo, Output output, Attribute t) {
            output.writeString(t.getId());
            output.writeString(t.getName());
            kryo.writeObjectOrNull(output, t.getCodelist(), Codelist.class);
            kryo.writeObject(output, t.getRelationship());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Attribute read(Kryo kryo, Input input, Class<? extends Attribute> type) {
            return Attribute
                    .builder()
                    .id(input.readString())
                    .name(input.readString())
                    .codelist(kryo.readObjectOrNull(input, Codelist.class))
                    .relationship(kryo.readObject(input, AttributeRelationship.class))
                    .build();
        }
    }

    private static final class MonitorReportsSerializer extends ImmutableSerializer<MonitorReports> {

        private final Serializer<Collection<MonitorReport>> reports = new CustomCollectionSerializer<>(MonitorReport.class);

        @Override
        public void write(Kryo kryo, Output output, MonitorReports t) {
            output.writeString(t.getUriScheme());
            kryo.writeObject(output, t.getReports(), reports);
            kryo.writeObject(output, t.getCreationTime());
            kryo.writeObject(output, t.getExpirationTime());
        }

        @Override
        public MonitorReports read(Kryo kryo, Input input, Class<? extends MonitorReports> type) {
            return MonitorReports
                    .builder()
                    .uriScheme(input.readString())
                    .reports(kryo.readObject(input, ArrayList.class, reports))
                    .creationTime(kryo.readObject(input, Instant.class))
                    .expirationTime(kryo.readObject(input, Instant.class))
                    .build();
        }
    }

    private static final class MonitorReportSerializer extends ImmutableSerializer<MonitorReport> {

        @Override
        public void write(Kryo kryo, Output output, MonitorReport t) {
            output.writeString(t.getSource());
            kryo.writeObject(output, t.getStatus());
            kryo.writeObjectOrNull(output, t.getUptimeRatio(), Double.class);
            kryo.writeObjectOrNull(output, t.getAverageResponseTime(), Long.class);
        }

        @Override
        public MonitorReport read(Kryo kryo, Input input, Class<? extends MonitorReport> type) {
            return MonitorReport
                    .builder()
                    .source(input.readString())
                    .status(kryo.readObject(input, MonitorStatus.class))
                    .uptimeRatio(kryo.readObjectOrNull(input, Double.class))
                    .averageResponseTime(kryo.readObjectOrNull(input, Long.class))
                    .build();
        }
    }
}
