/*
 * Copyright 2019 National Bank of Belgium
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

import lombok.NonNull;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class JFileChoosers {

    private final String IS_CLOSING_PROPERTY = "JFileChooserDialogIsClosingProperty";

    public void autoPersistUserNodeForClass(@NonNull JFileChooser fileChooser, @NonNull Class<?> type) {
        autoPersist(fileChooser, Preferences.userNodeForPackage(type).node(type.getSimpleName()));
    }

    public void autoPersist(@NonNull JFileChooser fileChooser, @NonNull Preferences prefs) {
        Objects.requireNonNull(fileChooser);
        Objects.requireNonNull(prefs);
        loadCurrentDir(fileChooser, prefs);
        fileChooser.addPropertyChangeListener(IS_CLOSING_PROPERTY, event -> storeCurrentDir(fileChooser, prefs));
    }

    public File getSelectedFileWithExtension(@NonNull JFileChooser fileChooser) {
        File file = fileChooser.getSelectedFile();
        if (file != null) {
            FileFilter filter = fileChooser.getFileFilter();
            if (filter instanceof FileNameExtensionFilter) {
                String[] exts = ((FileNameExtensionFilter) filter).getExtensions();
                if (exts.length > 0 && !anyMatch(file, exts)) {
                    return new File(file.getPath() + "." + exts[0]);
                }
            }
        }
        return file;
    }

    public boolean canOverride(@NonNull File file, Component parent) {
        return !file.exists() || JOptionPane.showConfirmDialog(parent, "File exists already. Delete it anyway?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public Optional<File> getOpenFile(@NonNull JFileChooser fileChooser, Component parent) {
        return JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(parent)
                ? Optional.of(fileChooser.getSelectedFile())
                : Optional.empty();
    }

    public Optional<File> getSaveFile(@NonNull JFileChooser fileChooser, Component parent) {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(parent)) {
            File result = getSelectedFileWithExtension(fileChooser);
            if (canOverride(result, parent)) {
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    private final static String CURRENT_DIRECTORY_KEY = "currentDirectory";

    private void loadCurrentDir(JFileChooser fileChooser, Preferences prefs) {
        Optional.ofNullable(prefs.get(CURRENT_DIRECTORY_KEY, null))
                .filter(Objects::nonNull)
                .map(File::new)
                .ifPresent(fileChooser::setCurrentDirectory);
    }

    private void storeCurrentDir(JFileChooser fileChooser, Preferences prefs) {
        prefs.put(CURRENT_DIRECTORY_KEY, fileChooser.getCurrentDirectory().toString());
    }

    private boolean anyMatch(File file, String[] exts) {
        String normalizedFile = file.getName().toLowerCase(Locale.ROOT);
        return Stream.of(exts)
                .map(ext -> "." + ext.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedFile::endsWith);
    }

    public static void autoSelectOnFilter(JFileChooser fileChooser) {
        File[] files = fileChooser.getCurrentDirectory().listFiles(file -> !file.isDirectory() && fileChooser.getFileFilter().accept(file));
        if (fileChooser.isMultiSelectionEnabled()) {
            fileChooser.setSelectedFiles(files);
        } else {
            if (files != null && files.length == 1) {
                fileChooser.setSelectedFile(files[0]);
            }
        }
    }
}
