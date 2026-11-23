/*
 * Copyright 2026 National Bank of Belgium
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
package sdmxdl.provider.web;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.function.IOFunction;
import sdmxdl.format.design.ServiceSupport;
import sdmxdl.web.SourceIcon;
import sdmxdl.web.spi.IconProvider;

/**
 * An {@link IconProvider} backed by bundled resources.
 *
 * <p>Icons are listed in a manifest file named {@value #MANIFEST} that lives next to them. Each non-blank line that
 * does not start with {@code #} is a CSV record whose first column is a file name; the other columns (origin,
 * retrieval date, …) are documentation only. The first record is a header. File names follow the pattern {@code
 * <name>.svg} or {@code <name>-<size>.png}.
 *
 * <p>Resources are opened through a function that must be created in the module that owns them (for example {@code
 * MyIconProvider.class::getResourceAsStream}) so that they can stay encapsulated.
 */
@ServiceSupport
@lombok.Builder(toBuilder = true)
public final class IconProviderSupport implements IconProvider {

    public static final String MANIFEST = "icons.csv";

    private static final Pattern FILE_PATTERN =
            Pattern.compile("^(?:([A-Za-z0-9_-]+)\\.svg|([A-Za-z0-9_-]+)-(\\d+)\\.png)$");

    @NonNull private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_ICON_PROVIDER_RANK;

    /** Opens a resource by its file name and returns {@code null} if it doesn't exist. */
    @NonNull private final IOFunction<String, InputStream> resources;

    @lombok.Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Map<String, List<IconFile>> lazyFiles = loadFiles(resources);

    @Override
    public @NonNull String getIconProviderId() {
        return id;
    }

    @Override
    public int getIconProviderRank() {
        return rank;
    }

    @Override
    public @NonNull Collection<String> getIconNames() {
        return Collections.unmodifiableSet(getLazyFiles().keySet());
    }

    @Override
    public @NonNull List<SourceIcon> getIcons(@NonNull String name) throws IOException {
        List<IconFile> files;
        try {
            files = getLazyFiles().getOrDefault(name, Collections.emptyList());
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
        List<SourceIcon> result = new ArrayList<>(files.size());
        for (IconFile file : files) {
            result.add(loadIcon(resources, file));
        }
        return result;
    }

    @lombok.Value
    private static class IconFile {
        String fileName;
        Integer size;
    }

    private static Map<String, List<IconFile>> loadFiles(IOFunction<String, InputStream> resources) {
        try {
            return parseManifest(readResource(resources, MANIFEST));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Map<String, List<IconFile>> parseManifest(byte[] content) throws IOException {
        Map<String, List<IconFile>> result = new TreeMap<>();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(new java.io.ByteArrayInputStream(content), UTF_8))) {
            boolean header = true;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (header) {
                    header = false;
                    continue;
                }
                String fileName = line.split(",", -1)[0].trim();
                Matcher matcher = FILE_PATTERN.matcher(fileName);
                if (!matcher.matches()) {
                    throw new IOException("Invalid icon file name '" + fileName + "' in " + MANIFEST);
                }
                String name = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                Integer size = matcher.group(3) != null ? Integer.valueOf(matcher.group(3)) : null;
                result.computeIfAbsent(name, ignore -> new ArrayList<>()).add(new IconFile(fileName, size));
            }
        }
        result.replaceAll((name, files) -> Collections.unmodifiableList(files));
        return Collections.unmodifiableMap(result);
    }

    private static SourceIcon loadIcon(IOFunction<String, InputStream> resources, IconFile file) throws IOException {
        byte[] content = readResource(resources, file.getFileName());
        try {
            if (file.getSize() == null) {
                return SourceIcon.ofSvg(content);
            }
            SourceIcon result = SourceIcon.ofPng(content);
            if (!file.getSize().equals(result.getSize())) {
                throw new IOException("Invalid icon size in '" + file.getFileName() + "': expected " + file.getSize()
                        + ", found " + result.getSize());
            }
            return result;
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid icon '" + file.getFileName() + "'", ex);
        }
    }

    private static byte[] readResource(IOFunction<String, InputStream> resources, String fileName) throws IOException {
        try (InputStream stream = resources.applyWithIO(fileName)) {
            if (stream == null) {
                throw new IOException("Missing resource '" + fileName + "'");
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = stream.read(buffer)) != -1) {
                result.write(buffer, 0, n);
            }
            return result.toByteArray();
        }
    }
}
