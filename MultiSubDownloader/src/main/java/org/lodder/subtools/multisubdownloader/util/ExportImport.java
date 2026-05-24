package org.lodder.subtools.multisubdownloader.util;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;

import com.google.gson.GsonBuilder;
import io.gsonfire.GsonFireBuilder;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.gui.dialog.MappingEpisodeNameDialog.MappingType;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.Manager.Value;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.cache.ProviderCacheKey;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler.MessageSeverity;
import org.lodder.subtools.sublibrary.util.filefilter.ExtensionFileFilter;
import org.lodder.subtools.sublibrary.util.filefilter.JsonFileFilter;
import org.lodder.subtools.sublibrary.util.filefilter.XmlFileFilter;

@NullMarked
public class ExportImport {

    private final Manager manager;
    private final UserInteractionHandler userInteractionHandler;
    private final Component parent;

    public ExportImport(Manager manager, UserInteractionHandler userInteractionHandler,
        Component parent) {
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
        this.parent = parent;
    }

    @NullMarked
    public enum SettingsType {
        PREFERENCES(FileType.XML),
        SERIE_MAPPING(FileType.JSON);

        @val FileType fileType;

        SettingsType(FileType fileType) {
            this.fileType = fileType;
        }
    }

    @NullMarked
    private enum FileType {
        XML(".xml", new XmlFileFilter()),
        JSON(".json", new JsonFileFilter());

        @val String extension;
        @val ExtensionFileFilter fileFilter;

        FileType(String extension, ExtensionFileFilter fileFilter) {
            this.extension = extension;
            this.fileFilter = fileFilter;
        }
    }

    public void importSettings(SettingsType listType) {
        chooseFile(listType.fileType).ifPresent(path -> {
            if (Files.notExists(path)) {
                userInteractionHandler.showMessage(getText("ImportExport.FileDoesNotExist"),
                    getText("ImportExport.ErrorWhileImporting"), MessageSeverity.WARNING);
                return;
            }
            try {
                switch (listType) {
                    case PREFERENCES -> ExportImportPreferences.importSettings(path, userInteractionHandler);
                    case SERIE_MAPPING ->
                        ExportImportSerieMapping.importSettings(path, userInteractionHandler, manager);
                    default -> throw new IllegalArgumentException("Unexpected value: " + listType);
                }
            } catch (CorruptSettingsFileException e) {
                userInteractionHandler.showMessage(getText("ImportExport.ImportCorruptFile"),
                    getText("ImportExport.ErrorWhileImporting"), MessageSeverity.ERROR);
            } catch (Exception e) {
                userInteractionHandler.showMessage(getText("ImportExport.ErrorWhileImporting"),
                    getText("ImportExport.ErrorWhileImporting"), MessageSeverity.ERROR);
            }
        });
    }

    public void exportSettings(SettingsType listType) {
        chooseFile(listType.fileType).map(path -> path.toString().endsWith(listType.fileType.extension) ? path :
                path.getParent().resolve(path.getFileName().toString() + listType.fileType.extension))
            .ifPresent(path -> {
                try {
                    switch (listType) {
                        case PREFERENCES -> ExportImportPreferences.exportSettings(path);
                        case SERIE_MAPPING -> ExportImportSerieMapping.exportSettings(path, manager);
                        default -> throw new IllegalArgumentException("Unexpected value: " + listType);
                    }
                } catch (Exception e) {
                    userInteractionHandler.showMessage(getText("ImportExport.ErrorWhileExporting"),
                        getText("ImportExport.ErrorWhileExporting"), MessageSeverity.ERROR);
                }
            });
    }

    @NullMarked
    public static class ExportImportPreferences {

        private ExportImportPreferences() {
            // hide utility class constructor
        }

        public static void exportSettings(Path path) throws Exception {
            SettingsControl.exportPreferences(path);
        }

        public static void importSettings(Path path, UserInteractionHandler userInteractionHandler)
            throws CorruptSettingsFileException {
            try {
                SettingsControl.importPreferences(path);
            } catch (IOException | BackingStoreException | InvalidPreferencesFormatException e) {
                throw new CorruptSettingsFileException(e);
            }
        }
    }

    @NullMarked
    public static class ExportImportSerieMapping {

        private ExportImportSerieMapping() {
            // hide utility class constructor
        }

        public static void exportSettings(Path path, Manager manager) throws IOException {
            List<SerieMappingWithKey> serieMappingsWithKey = MappingType.values().stream()
                .map(mappingType -> mappingType.getValues(manager)).flatMap(List::stream)
                .map(pair -> new SerieMappingWithKey(pair.getKey(), pair.getValue()))
                .toList();
            Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(serieMappingsWithKey));
        }

        public static void importSettings(Path path, UserInteractionHandler userInteractionHandler, Manager manager)
            throws CorruptSettingsFileException {
            SerieMappingWithKey[] serieMappings;
            try {
                serieMappings = new GsonFireBuilder().enableHooks(SerieMapping.class)
                    .createGson()
                    .fromJson(Files.readString(path), SerieMappingWithKey[].class);
            } catch (IOException e) {
                throw new CorruptSettingsFileException(e);
            }
            getImportStyle(userInteractionHandler).ifPresent(importStyle -> serieMappings.forEach(serieMapping -> {
                CacheKey cacheKey = new CacheKey(manager, CacheType.DISK, serieMapping.key);
                if (!cacheKey.isPresent() || importStyle == ImportStyle.OVERWRITE) {
                    cacheKey.store(Value.of(serieMapping.serieMapping));
                }
            }));
        }

        private static Optional<ImportStyle> getImportStyle(UserInteractionHandler userInteractionHandler) {
            return userInteractionHandler.selectFromList(Arrays.asList(ImportStyle.values()),
                getText("ImportExport.OverwriteOrAdd"),
                getText("ImportExport.OverwriteOrAddTitle"),
                option -> switch (option) {
                    case OVERWRITE -> getText("ImportExport.Overwrite");
                    case APPEND -> getText("ImportExport.Add");
                });
        }

        @NullMarked
        private record SerieMappingWithKey(ProviderCacheKey key, SerieMapping serieMapping) {
        }
    }

    private Optional<Path> chooseFile(ExportImport.FileType fileType) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(fileType.fileFilter);
        int returnVal = fc.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return Optional.of(fc.getSelectedFile().toPath());
        } else {
            return Optional.empty();
        }
    }

    @NullMarked
    private enum ImportStyle {
        OVERWRITE,
        APPEND
    }

    @NullMarked
    public static class CorruptSettingsFileException extends Exception {
        public CorruptSettingsFileException(Throwable cause) {
            super(cause);
        }
    }
}
