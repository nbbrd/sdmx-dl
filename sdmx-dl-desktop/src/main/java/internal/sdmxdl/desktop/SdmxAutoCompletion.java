/*
 * Copyright 2017 National Bank of Belgium
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
package internal.sdmxdl.desktop;

import static ec.util.completion.AutoCompletionSource.Behavior.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import ec.util.completion.AutoCompletionSource;
import ec.util.completion.ExtAutoCompletionSource;
import ec.util.completion.swing.CustomListCellRenderer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.*;
import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSourcesRequest;

/**
 * @author Philippe Charles
 */
public abstract class SdmxAutoCompletion {

    public abstract AutoCompletionSource getSource();

    public abstract ListCellRenderer<?> getRenderer();

    public static SdmxAutoCompletion onWebSource(SdmxWebManager manager, Languages languages) {
        return new WebSourceCompletion(manager, languages);
    }

    public static <S extends Source> SdmxAutoCompletion onDatabase(
            SdmxManager<S> manager, Languages languages, Supplier<S> source, ConcurrentMap<?, ?> cache) {
        return new DatabaseCompletion<>(manager, languages, source, cache);
    }

    public static <S extends Source> SdmxAutoCompletion onFlow(
            SdmxManager<S> manager,
            Languages languages,
            Supplier<S> source,
            Supplier<DatabaseRef> database,
            ConcurrentMap<?, ?> cache) {
        return new FlowCompletion<>(manager, languages, source, database, cache);
    }

    public static <S extends Source> SdmxAutoCompletion onDimension(
            SdmxManager<S> manager,
            Languages languages,
            Supplier<S> source,
            Supplier<DatabaseRef> database,
            Supplier<FlowRef> flowRef,
            ConcurrentMap<?, ?> cache) {
        return new DimensionCompletion<>(manager, languages, source, database, flowRef, cache);
    }

    public static <S extends Source> SdmxAutoCompletion onAttribute(
            SdmxManager<S> manager,
            Languages languages,
            Supplier<S> source,
            Supplier<DatabaseRef> database,
            Supplier<FlowRef> flowRef,
            ConcurrentMap<?, ?> cache) {
        return new AttributeCompletion<>(manager, languages, source, database, flowRef, cache);
    }

    private static final class WebSourceCompletion extends SdmxAutoCompletion {

        private final SdmxWebManager manager;

        private final Languages languages;

        private final SdmxIconSupport icons;

        private WebSourceCompletion(@NonNull SdmxWebManager manager, @NonNull Languages languages) {
            this.manager = manager;
            this.languages = languages;
            this.icons = SdmxIconSupport.of(manager);
        }

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(SYNC)
                    .valueToString(WebSource::getId)
                    .build();
        }

        @Override
        public ListCellRenderer<?> getRenderer() {
            return new CustomListCellRenderer<WebSource>() {
                @Override
                protected @NonNull String getValueAsString(@NonNull WebSource value) {
                    return value.getId() + ": " + languages.select(value.getNames());
                }

                @Override
                protected Icon toIcon(
                        String term, JList list, WebSource value, int index, boolean isSelected, boolean cellHasFocus) {
                    return icons.getIcon(value, 16);
                }
            };
        }

        private List<WebSource> load(String term) {
            return manager.listSources(WebSourcesRequest.builder()
                            .languages(languages)
                            .query(term)
                            .build())
                    .getItems();
        }
    }

    @lombok.AllArgsConstructor
    private static final class DatabaseCompletion<S extends Source> extends SdmxAutoCompletion {

        @lombok.NonNull private final SdmxManager<S> manager;

        @lombok.NonNull private final Languages languages;

        @lombok.NonNull private final Supplier<S> source;

        @lombok.NonNull private final ConcurrentMap<?, ?> cache;

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(SYNC)
                    .valueToString(database -> database.getRef().toString())
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public ListCellRenderer<?> getRenderer() {
            return new CustomListCellRenderer<Database>() {
                @Override
                protected @NonNull String getValueAsString(@NonNull Database value) {
                    return value.getRef() + ": " + value.getName();
                }
            };
        }

        private List<Database> load(String term) throws IOException {
            DatabasesRequest request = DatabasesRequest.builder()
                    .languages(languages)
                    .query(term != null ? term : HasSearch.NO_QUERY)
                    .build();
            return manager.using(source.get()).listDatabases(request).getItems();
        }

        private String getCacheKey(String term) {
            return "Database" + source.get() + languages;
        }
    }

    @lombok.AllArgsConstructor
    private static final class FlowCompletion<S extends Source> extends SdmxAutoCompletion {

        @lombok.NonNull private final SdmxManager<S> manager;

        @lombok.NonNull private final Languages languages;

        @lombok.NonNull private final Supplier<S> source;

        @lombok.NonNull private final Supplier<DatabaseRef> database;

        @lombok.NonNull private final ConcurrentMap<?, ?> cache;

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(this::getBehavior)
                    .valueToString(o -> o.getRef().toShortString())
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public ListCellRenderer<?> getRenderer() {
            return CustomListCellRenderer.<Flow>of(
                    flow -> flow.getRef().toShortString() + "<br><i>" + flow.getName(),
                    flow -> flow.getRef().toString());
        }

        private List<Flow> load(String term) throws Exception {
            FlowsRequest request = FlowsRequest.builder()
                    .languages(languages)
                    .database(database.get())
                    .query(term != null ? term : HasSearch.NO_QUERY)
                    .build();
            return manager.using(source.get()).listFlows(request).getItems();
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null ? ASYNC : NONE;
        }

        private String getCacheKey(String term) {
            return "Dataflow" + source.get() + database.get() + languages;
        }
    }

    @lombok.AllArgsConstructor
    private static final class DimensionCompletion<S extends Source> extends SdmxAutoCompletion {

        @lombok.NonNull private final SdmxManager<S> manager;

        @lombok.NonNull private final Languages languages;

        @lombok.NonNull private final Supplier<S> source;

        @lombok.NonNull private final Supplier<DatabaseRef> database;

        @lombok.NonNull private final Supplier<FlowRef> flowRef;

        @lombok.NonNull private final ConcurrentMap<?, ?> cache;

        private MetaRequest toRequest() {
            return MetaRequest.builder()
                    .languages(languages)
                    .database(database.get())
                    .flow(flowRef.get())
                    .build();
        }

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(this::getBehavior)
                    .postProcessor(this::filterAndSort)
                    .valueToString(Dimension::getId)
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public ListCellRenderer<?> getRenderer() {
            return CustomListCellRenderer.of(Dimension::getId, Dimension::getName);
        }

        private List<Dimension> load(String term) throws Exception {
            return manager.using(source.get())
                    .getMeta(toRequest())
                    .getStructure()
                    .getDimensions();
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null && flowRef.get() != null ? ASYNC : NONE;
        }

        private List<Dimension> filterAndSort(List<Dimension> values, String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return values.stream()
                    .filter(o -> filter.test(o.getId()) || filter.test(o.getName()))
                    .sorted(comparing(Dimension::getId))
                    .collect(toList());
        }

        private String getCacheKey(String term) {
            return "Dimension" + source.get() + database.get() + flowRef.get() + languages;
        }
    }

    @lombok.AllArgsConstructor
    private static final class AttributeCompletion<S extends Source> extends SdmxAutoCompletion {

        @lombok.NonNull private final SdmxManager<S> manager;

        @lombok.NonNull private final Languages languages;

        @lombok.NonNull private final Supplier<S> source;

        @lombok.NonNull private final Supplier<DatabaseRef> database;

        @lombok.NonNull private final Supplier<FlowRef> flowRef;

        @lombok.NonNull private final ConcurrentMap<?, ?> cache;

        private MetaRequest toRequest() {
            return MetaRequest.builder()
                    .languages(languages)
                    .database(database.get())
                    .flow(flowRef.get())
                    .build();
        }

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(this::getBehavior)
                    .postProcessor(this::filterAndSort)
                    .valueToString(Attribute::getId)
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public ListCellRenderer<?> getRenderer() {
            return CustomListCellRenderer.of(Attribute::getId, Attribute::getName);
        }

        private List<Attribute> load(String term) throws Exception {
            return new ArrayList<>(manager.using(source.get())
                    .getMeta(toRequest())
                    .getStructure()
                    .getAttributes());
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null && flowRef.get() != null ? ASYNC : NONE;
        }

        private List<Attribute> filterAndSort(List<Attribute> values, String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return values.stream()
                    .filter(o -> filter.test(o.getId()) || filter.test(o.getName()))
                    .sorted(comparing(Attribute::getId))
                    .collect(toList());
        }

        private String getCacheKey(String term) {
            return "Attribute" + source.get() + database.get() + flowRef.get() + languages;
        }
    }
}
