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

import ec.util.completion.AutoCompletionSource;
import ec.util.completion.ExtAutoCompletionSource;
import ec.util.completion.swing.CustomListCellRenderer;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.FaviconSupport;
import nbbrd.desktop.favicon.URLConnectionFactory;
import nbbrd.io.Resource;
import sdmxdl.Dimension;
import sdmxdl.*;
import sdmxdl.desktop.MainComponent;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.SSLFactory;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static ec.util.completion.AutoCompletionSource.Behavior.*;
import static java.util.stream.Collectors.toList;

/**
 * @author Philippe Charles
 */
public abstract class SdmxAutoCompletion {

    public abstract AutoCompletionSource getSource();

    public abstract ListCellRenderer getRenderer();

    public static SdmxAutoCompletion onWebSource(SdmxWebManager manager, Languages languages) {
        return new WebSourceCompletion(manager, languages);
    }

    public static <S extends Source> SdmxAutoCompletion onDatabase(SdmxManager<S> manager, Languages languages, Supplier<S> source, ConcurrentMap cache) {
        return new DatabaseCompletion<>(manager, languages, source, cache);
    }

    public static <S extends Source> SdmxAutoCompletion onDataflow(SdmxManager<S> manager, Languages languages, Supplier<S> source, Supplier<DatabaseRef> database, ConcurrentMap cache) {
        return new DataflowCompletion<>(manager, languages, source, database, cache);
    }

    public static <S extends Source> SdmxAutoCompletion onDimension(SdmxManager<S> manager, Languages languages, Supplier<S> source, Supplier<DatabaseRef> database, Supplier<FlowRef> flowRef, ConcurrentMap cache) {
        return new DimensionCompletion<>(manager, languages, source, database, flowRef, cache);
    }

    public static <S extends Source> SdmxAutoCompletion onAttribute(SdmxManager<S> manager, Languages languages, Supplier<S> source, Supplier<DatabaseRef> database, Supplier<FlowRef> flowRef, ConcurrentMap cache) {
        return new AttributeCompletion<>(manager, languages, source, database, flowRef, cache);
    }

    @lombok.AllArgsConstructor
    private static final class WebSourceCompletion extends SdmxAutoCompletion {

        @lombok.NonNull
        private final SdmxWebManager manager;

        @lombok.NonNull
        private final Languages languages;

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource
                    .builder(this::load)
                    .behavior(SYNC)
                    .postProcessor(this::filterAndSort)
                    .valueToString(WebSource::getId)
                    .build();
        }

        @Override
        public ListCellRenderer getRenderer() {
            return new CustomListCellRenderer<WebSource>() {
                @Override
                protected String getValueAsString(WebSource value) {
                    return value.getId() + ": " + languages.select(value.getNames());
                }

                @Override
                protected Icon toIcon(String term, JList list, WebSource value, int index, boolean isSelected, boolean cellHasFocus) {
                    return getFavicon(value.getWebsite(), list::repaint);
                }
            };
        }

        private List<WebSource> load(String term) {
            return manager
                    .getSources()
                    .values()
                    .stream()
                    .filter(source -> !source.isAlias())
                    .collect(toList());
        }

        private List<WebSource> filterAndSort(List<WebSource> list, String term) {
            return list.stream().filter(getFilter(term)).collect(toList());
        }

        private Predicate<WebSource> getFilter(String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return value -> filter.test(languages.select(value.getNames()))
                    || filter.test(value.getId())
                    || value.getAliases().stream().anyMatch(filter);
        }
    }

    @lombok.AllArgsConstructor
    private static final class DatabaseCompletion<S extends Source> extends SdmxAutoCompletion {

        @lombok.NonNull
        private final SdmxManager<S> manager;

        @lombok.NonNull
        private final Languages languages;

        @lombok.NonNull
        private final Supplier<S> source;

        @lombok.NonNull
        private final ConcurrentMap cache;

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource
                    .builder(this::load)
                    .behavior(SYNC)
                    .postProcessor(this::filterAndSort)
                    .valueToString(database -> database.getId().toString())
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public ListCellRenderer getRenderer() {
            return new CustomListCellRenderer<Database>() {
                @Override
                protected String getValueAsString(Database value) {
                    return value.getId() + ": " + value.getName();
                }

                @Override
                protected Icon toIcon(String term, JList list, Database value, int index, boolean isSelected, boolean cellHasFocus) {
                    return null;
                }
            };
        }

        private List<Database> load(String term) throws IOException {
            try (Connection c = manager.getConnection(source.get(), languages)) {
                return new ArrayList<>(c.getDatabases());
            }
        }

        private List<Database> filterAndSort(List<Database> list, String term) {
            return list.stream().filter(getFilter(term)).collect(toList());
        }

        private Predicate<Database> getFilter(String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return value -> filter.test(value.getName())
                    || filter.test(value.getId().toString());
        }

        private String getCacheKey(String term) {
            return "Database" + source.get() + languages;
        }
    }

    @lombok.AllArgsConstructor
    private static final class DataflowCompletion<S extends Source> extends SdmxAutoCompletion {

        @lombok.NonNull
        private final SdmxManager<S> manager;

        @lombok.NonNull
        private final Languages languages;

        @lombok.NonNull
        private final Supplier<S> source;

        @lombok.NonNull
        private final Supplier<DatabaseRef> database;

        @lombok.NonNull
        private final ConcurrentMap cache;

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource
                    .builder(this::load)
                    .behavior(this::getBehavior)
                    .postProcessor(this::filterAndSort)
                    .valueToString(o -> o.getRef().toString())
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public ListCellRenderer getRenderer() {
            return CustomListCellRenderer.<Flow>of(flow -> flow.getRef() + "<br><i>" + flow.getName(), flow -> flow.getRef().toString());
        }

        private List<Flow> load(String term) throws Exception {
            try (Connection c = manager.getConnection(source.get(), languages)) {
                return new ArrayList<>(c.getFlows(database.get()));
            }
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null ? ASYNC : NONE;
        }

        private List<Flow> filterAndSort(List<Flow> values, String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return values.stream()
                    .filter(o -> filter.test(o.getName()) || filter.test(o.getRef().getId()) || filter.test(o.getDescription()))
                    .sorted(Comparator.comparing(Flow::getName))
                    .collect(toList());
        }

        private String getCacheKey(String term) {
            return "Dataflow" + source.get() + database.get() + languages;
        }
    }

    @lombok.AllArgsConstructor
    private static final class DimensionCompletion<S extends Source> extends SdmxAutoCompletion {

        @lombok.NonNull
        private final SdmxManager<S> manager;

        @lombok.NonNull
        private final Languages languages;

        @lombok.NonNull
        private final Supplier<S> source;

        @lombok.NonNull
        private final Supplier<DatabaseRef> database;

        @lombok.NonNull
        private final Supplier<FlowRef> flowRef;

        @lombok.NonNull
        private final ConcurrentMap cache;

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource
                    .builder(this::load)
                    .behavior(this::getBehavior)
                    .postProcessor(this::filterAndSort)
                    .valueToString(Dimension::getId)
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public ListCellRenderer getRenderer() {
            return CustomListCellRenderer.of(Dimension::getId, Dimension::getName);
        }

        private List<Dimension> load(String term) throws Exception {
            try (Connection c = manager.getConnection(source.get(), languages)) {
                return new ArrayList<>(c.getStructure(database.get(), flowRef.get()).getDimensions());
            }
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null && flowRef.get() != null ? ASYNC : NONE;
        }

        private List<Dimension> filterAndSort(List<Dimension> values, String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return values.stream()
                    .filter(o -> filter.test(o.getId()) || filter.test(o.getName()) || filter.test(String.valueOf(o.getPosition())))
                    .sorted(Comparator.comparing(Dimension::getId))
                    .collect(toList());
        }

        private String getCacheKey(String term) {
            return "Dimension" + source.get() + database.get() + flowRef.get() + languages;
        }
    }

    @lombok.AllArgsConstructor
    private static final class AttributeCompletion<S extends Source> extends SdmxAutoCompletion {

        @lombok.NonNull
        private final SdmxManager<S> manager;

        @lombok.NonNull
        private final Languages languages;

        @lombok.NonNull
        private final Supplier<S> source;

        @lombok.NonNull
        private final Supplier<DatabaseRef> database;

        @lombok.NonNull
        private final Supplier<FlowRef> flowRef;

        @lombok.NonNull
        private final ConcurrentMap cache;

        @Override
        public AutoCompletionSource getSource() {
            return ExtAutoCompletionSource
                    .builder(o -> load(o))
                    .behavior(this::getBehavior)
                    .postProcessor(this::filterAndSort)
                    .valueToString(Attribute::getId)
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public ListCellRenderer getRenderer() {
            return CustomListCellRenderer.of(Attribute::getId, Attribute::getName);
        }

        private List<Attribute> load(String term) throws Exception {
            try (Connection c = manager.getConnection(source.get(), languages)) {
                return new ArrayList<>(c.getStructure(database.get(), flowRef.get()).getAttributes());
            }
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null && flowRef.get() != null ? ASYNC : NONE;
        }

        private List<Attribute> filterAndSort(List<Attribute> values, String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return values.stream()
                    .filter(o -> filter.test(o.getId()) || filter.test(o.getName()))
                    .sorted(Comparator.comparing(Attribute::getId))
                    .collect(toList());
        }

        private String getCacheKey(String term) {
            return "Attribute" + source.get() + database.get() + flowRef.get() + languages;
        }
    }

    public static ImageIcon getDefaultIcon() {
        return sdmxIcon;
    }

    private static final ImageIcon sdmxIcon = new ImageIcon(loadImage());

    private static Image loadImage() {
        try (InputStream stream = Resource.newInputStream(MainComponent.class, "sdmx-logo.png")) {
            return ImageIO.read(stream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Icon getFavicon(URL website) {
        return website != null
                ? FAVICONS.getOrDefault(FaviconRef.of(DomainName.of(website), 16), getDefaultIcon())
                : getDefaultIcon();
    }

    public static Icon getFavicon(URL website, Runnable callback) {
        return website != null
                ? FAVICONS.getOrDefault(FaviconRef.of(DomainName.of(website), 16), callback, getDefaultIcon())
                : getDefaultIcon();
    }

    public static final FaviconSupport FAVICONS = FaviconSupport
            .ofServiceLoader()
            .toBuilder()
            .client(new ClientOverCustomNetwork())
            .cache(new HashMap<>())
            //            .cache(IOCacheFactoryLoader.get().ofTtl(Duration.ofHours(1)))
            .build();

    private static final class ClientOverCustomNetwork implements URLConnectionFactory {

        @Override
        public URLConnection openConnection(URL url) throws IOException {
            Network network = getNetwork();
            Proxy proxy = selectProxy(network, url);
            URLConnection result = network.getURLConnectionFactory().openConnection(url, proxy);
            applyHttps(result, network);
            return result;
        }

        private void applyHttps(URLConnection result, Network network) {
            if (result instanceof HttpsURLConnection) {
                SSLFactory sslFactory = network.getSSLFactory();
                HttpsURLConnection https = (HttpsURLConnection) result;
                https.setHostnameVerifier(sslFactory.getHostnameVerifier());
                https.setSSLSocketFactory(sslFactory.getSSLSocketFactory());
            }
        }

        private Proxy selectProxy(Network network, URL url) throws IOException {
            try {
                return network.getProxySelector().select(url.toURI()).stream().findFirst().orElse(Proxy.NO_PROXY);
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
    }

    private static Network getNetwork() {
        return Network.getDefault();
//        return TsManager
//                .get()
//                .getProvider(SdmxWebProvider.class)
//                .map(SdmxWebProvider::getSdmxManager)
//                .map(SdmxWebManager::getNetwork)
//                .orElse(Network.getDefault());
    }
}
