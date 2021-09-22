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
import com.esotericsoftware.kryo.kryo5.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.ImmutableSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.MapSerializer;
import com.esotericsoftware.kryo.kryo5.util.Pool;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.repo.DataSet;
import sdmxdl.repo.SdmxRepository;

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
@lombok.experimental.UtilityClass
public final class KryoSerialization {

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

    public static @NonNull FileParser<SdmxRepository> getRepositoryParser() {
        return KryoSerializer.REPOSITORY;
    }

    public static @NonNull FileFormatter<SdmxRepository> getRepositoryFormatter() {
        return KryoSerializer.REPOSITORY;
    }

    @lombok.AllArgsConstructor
    private static final class KryoSerializer<T> implements FileParser<T>, FileFormatter<T> {

        public static final KryoSerializer<SdmxRepository> REPOSITORY = new KryoSerializer<>(SdmxRepository.class);

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
    }

    private static Kryo newKryo() {
        Kryo result = new Kryo();
        result.setReferences(false);
        result.setRegistrationRequired(true);

        result.register(SdmxRepository.class, new SdmxRepositorySerializer());
        result.register(DataStructure.class, new DataStructureSerializer());
        result.register(DataStructureRef.class, new DataStructureRefSerializer());
        result.register(Dataflow.class, new DataflowSerializer());
        result.register(DataflowRef.class, new DataflowRefSerializer());
        result.register(Frequency.class, new FrequencySerializer());
        result.register(Key.class, new KeySerializer());
        result.register(DataSet.class, new DataSetSerializer());
        result.register(Series.class, new SeriesSerializer());
        result.register(Obs.class, new ObsSerializer());
        result.register(Dimension.class, new DimensionSerializer());
        result.register(Attribute.class, new AttributeSerializer());

        result.register(ArrayList.class, new CollectionSerializer<>());
        result.register(LocalDateTime.class);
        result.register(Instant.class);

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

    private static final class DataStructureRefSerializer extends ImmutableSerializer<DataStructureRef> {

        @Override
        public void write(Kryo kryo, Output output, DataStructureRef t) {
            output.writeString(t.toString());
        }

        @Override
        public DataStructureRef read(Kryo kryo, Input input, Class<? extends DataStructureRef> type) {
            return DataStructureRef.parse(input.readString());
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

    private static final class DataflowRefSerializer extends ImmutableSerializer<DataflowRef> {

        @Override
        public void write(Kryo kryo, Output output, DataflowRef t) {
            output.writeString(t.toString());
        }

        @Override
        public DataflowRef read(Kryo kryo, Input input, Class<? extends DataflowRef> type) {
            return DataflowRef.parse(input.readString());
        }
    }

    private static final class DataSetSerializer extends ImmutableSerializer<DataSet> {

        private final Serializer<Collection<Series>> data = new CustomCollectionSerializer<>(Series.class);

        @Override
        public void write(Kryo kryo, Output output, DataSet t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getKey());
            kryo.writeObject(output, t.getData(), data);
        }

        @SuppressWarnings("unchecked")
        @Override
        public DataSet read(Kryo kryo, Input input, Class<? extends DataSet> type) {
            return DataSet
                    .builder()
                    .ref(kryo.readObject(input, DataflowRef.class))
                    .key(kryo.readObject(input, Key.class))
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

    private static final class SeriesSerializer extends ImmutableSerializer<Series> {

        private final Serializer<Collection<Obs>> obs = new CustomCollectionSerializer<>(Obs.class);
        private final Serializer<Map<String, String>> meta = new StringMapSerializer();
        private final Series.Builder builder = Series.builder();

        @Override
        public void write(Kryo kryo, Output output, Series t) {
            kryo.writeObject(output, t.getKey());
            kryo.writeObject(output, t.getFreq());
            kryo.writeObject(output, t.getObs(), obs);
            kryo.writeObject(output, t.getMeta(), meta);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Series read(Kryo kryo, Input input, Class<? extends Series> type) {
            return builder
                    .clearMeta()
                    .clearObs()
                    .key(kryo.readObject(input, Key.class))
                    .freq(kryo.readObject(input, Frequency.class))
                    .obs(kryo.readObject(input, ArrayList.class, obs))
                    .meta(kryo.readObject(input, HashMap.class, meta))
                    .build();
        }
    }

    private static final class ObsSerializer extends ImmutableSerializer<Obs> {

        private final Serializer<Map<String, String>> meta = new StringMapSerializer();
        private final Obs.Builder builder = Obs.builder();

        @Override
        public void write(Kryo kryo, Output output, Obs t) {
            kryo.writeObjectOrNull(output, t.getPeriod(), LocalDateTime.class);
            kryo.writeObjectOrNull(output, t.getValue(), Double.class);
            kryo.writeObject(output, t.getMeta(), meta);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Obs read(Kryo kryo, Input input, Class<? extends Obs> type) {
            return builder
                    .clearMeta()
                    .period(kryo.readObjectOrNull(input, LocalDateTime.class))
                    .value(kryo.readObjectOrNull(input, Double.class))
                    .meta(kryo.readObject(input, HashMap.class, meta))
                    .build();
        }
    }

    private static final class DimensionSerializer extends ImmutableSerializer<Dimension> {

        private final Serializer<Map<String, String>> codes = new StringMapSerializer();

        @Override
        public void write(Kryo kryo, Output output, Dimension t) {
            output.writeString(t.getId());
            kryo.writeObject(output, t.getCodes(), codes);
            output.writeString(t.getLabel());
            output.writeInt(t.getPosition(), true);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Dimension read(Kryo kryo, Input input, Class<? extends Dimension> type) {
            return Dimension
                    .builder()
                    .id(input.readString())
                    .codes(kryo.readObject(input, HashMap.class, codes))
                    .label(input.readString())
                    .position(input.readInt(true))
                    .build();
        }
    }

    private static final class AttributeSerializer extends ImmutableSerializer<Attribute> {

        private final Serializer<Map<String, String>> codes = new StringMapSerializer();

        @Override
        public void write(Kryo kryo, Output output, Attribute t) {
            output.writeString(t.getId());
            kryo.writeObject(output, t.getCodes(), codes);
            output.writeString(t.getLabel());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Attribute read(Kryo kryo, Input input, Class<? extends Attribute> type) {
            return Attribute
                    .builder()
                    .id(input.readString())
                    .codes(kryo.readObject(input, HashMap.class, codes))
                    .label(input.readString())
                    .build();
        }
    }
}
