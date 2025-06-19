package org.lodder.subtools.multisubdownloader.gui.dialog;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.Serial;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Function;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandlerGUI;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

public class MappingEpisodeNameDialog extends MultiSubDialog {

    @Serial private static final long serialVersionUID = 1L;

    private final MappingTableModel mappingTableModel;
    private final SubtitleProviderStore subtitleProviderStore;
    private final JButton btnAddCustomMapping;
    private final JTable table;
    private Optional<SubtitleProvider<? extends Subtitle>> selectedSubtitleProvider;
    private MappingType selectedMappingType;

    public MappingEpisodeNameDialog(@Nullable JFrame frame=null, Manager manager,
        SubtitleProviderStore subtitleProviderStore,
        UserInteractionHandlerGUI userInteractionHandler) {
        super(frame, getText("MappingEpisodeNameDialog.Title"), true);
        this.subtitleProviderStore = subtitleProviderStore;
        this.mappingTableModel = new MappingTableModel(manager);
        setResizable(true);
        setBounds(150, 150, 650, 400);

        table = new JTable().model(mappingTableModel).rowSorter(TableRowSorter::new);

        contentPane
            .layout(new BorderLayout())
            .addComponent(BorderLayout.CENTER, new JPanel()
                .border(new EmptyBorder(5, 5, 5, 5))
                .layout(new GridBagLayout()
                    .columnWidths(new int[]{0, 0})
                    .rowHeights(new int[]{0, 40, 0})
                    .columnWeights(new double[]{1.0, Double.MIN_VALUE})
                    .rowWeights(new double[]{0.0, 1.0, Double.MIN_VALUE}))
                // select provider panel
                .addComponent(new JPanel()
                    .addComponent(new JLabel(getText("MappingEpisodeNameDialog.SelectProvider")))
                    .addComponent(new JComboBox<>()
                        .model(new DefaultComboBoxModel<>(MappingType.values()))
                        .itemListener(event -> selectMappingType((MappingType) event.getItem()))))
                .addComponent(new JPanel(),
                    new GridBagConstraints().insets(new Insets(0, 0, 5, 0))
                        .fill(GridBagConstraints.BOTH).gridx(0).gridy(0))
                .addComponent(new JScrollPane().viewportView(table),
                    new GridBagConstraints().fill(GridBagConstraints.BOTH).gridx(0).gridy(1)))
            // button panel
            .addComponent(BorderLayout.SOUTH, new JPanel()
                .layout(new MigLayout("", "[25px][50px][grow][50px][grow][50px][25px]",
                    "[][25px,grow,fill]"))
                .addComponent("skip", new JButton(getText("MappingEpisodeNameDialog.DeleteRow"))
                    .actionListener(_ -> {
                        int rowNbr = table.convertRowIndexToModel(table.getSelectedRow());
                        MappingTableModel model = (MappingTableModel) table.getModel();
                        Row row = (Row) model.getDataVector().get(rowNbr);
                        new CacheKey(manager, CacheType.DISK, row.key).remove();
                        if (row.selectionForKeyPrefix.deleteOtherFunction() != null) {
                            new CacheKey(manager, CacheType.DISK,
                                row.selectionForKeyPrefix.deleteOtherFunction().apply(row.key)).remove();
                        }
                        model.removeRow(rowNbr);
                    }))
                .addComponent("skip", btnAddCustomMapping =
                    new JButton(getText("MappingEpisodeNameDialog.ChangeMapping"))
                        .actionListener(() -> {
                            int rowNbr = table.convertRowIndexToModel(table.getSelectedRow());
                            MappingTableModel model = (MappingTableModel) table.getModel();

                            Row row = (Row) model.getDataVector().get(rowNbr);
                            String currentName = row.serieMapping.name;

                            String message = getText("MappingEpisodeNameDialog.enterNewNameForSerie",
                                currentName);
                            selectedSubtitleProvider.ifPresent(provider ->
                                userInteractionHandler.enter(message).ifPresent(newName -> {
                                    TvRelease tvRelease = new TvRelease(
                                        name:currentName,
                                        season:row.serieMapping.season,
                                        episode:1,
                                        originalName:currentName,
                                        customName:newName);
                                    try {
                                        provider.getProviderSerieMapping(tvRelease).ifPresentOrElse(serieId -> {
                                            row.serieMapping =
                                                new SerieMapping(currentName, serieId.providerId, serieId.providerName,
                                                    serieId.season);
                                            List<? extends SortKey> sortKeys = table.rowSorter.sortKeys;
                                            selectMappingType(selectedMappingType);
                                            table.rowSorter.sortKeys = sortKeys;
                                        }, () -> userInteractionHandler.message(
                                            getText("MappingEpisodeNameDialog.NoResultsFoundForSerieName", newName),
                                            getText("App.Info")));
                                    } catch (Exception e) {
                                        userInteractionHandler.message(getText("App.ErrorOccurred", e.getMessage()),
                                            getText("App.Error"));
                                    }
                                }));
                        }))
                .addComponent("skip", new JButton(getText("App.Close"))
                    .defaultButtonFor(getRootPane())
                    .actionListener(() -> setVisible(false))
                    .actionCommand(getText("App.Close"))));
        selectMappingType(MappingType.values()[0]);
    }

    private void selectMappingType(MappingType mappingType) {
        this.selectedMappingType = mappingType;
        this.selectedSubtitleProvider = subtitleProviderStore.getAllProviders()
            .stream()
            .filter(subtitleProvider -> subtitleProvider.source.name().equals(mappingType.providerName))
            .findAny();
        btnAddCustomMapping.enabled = selectedSubtitleProvider.isPresent();
        mappingTableModel.mappingType = mappingType;
        repaint();
    }

    public enum MappingType {
        TVDB("TVDB", "TVDB",
            new SelectionForKeyPrefix("", "tvdb-seriemapping-", k -> k.replace("-providerid-", "-serie-"))),
        IMDB("IMDB", "IMDB",
            new SelectionForKeyPrefix("", "imdb-releasemapping-")),
        ADDIC7ED("Addic7ed", SubtitleSource.ADDIC7ED, new SelectionForKeyPrefix("", "addic7ed-releasemapping-name:"),
            new SelectionForKeyPrefix("", "addic7ed-releasemapping-tvdbid:")),
        ADDIC7ED_PROXY("Addic7ed (Proxy)", SubtitleSource.ADDIC7ED.name() + "-GESTDOWN",
            new SelectionForKeyPrefix("", "addic7ed-releasemapping-name:"),
            new SelectionForKeyPrefix("", "addic7ed-releasemapping-tvdbid:")),
        SUBSCENE("Subscene", SubtitleSource.SUBSCENE, new SelectionForKeyPrefix("", "subscene-releasemapping-name:"),
            new SelectionForKeyPrefix("", "subscene-releasemapping-tvdbid:")),
        TV_SUBTITLES("TVSubtitles", SubtitleSource.TVSUBTITLES,
            new SelectionForKeyPrefix("", "tvsubtitles-releasemapping-name:"),
            new SelectionForKeyPrefix("", "tvsubtitles-releasemapping-tvdbid:")),
        OPEN_SUBTITLES("OpenSubtitles", SubtitleSource.OPENSUBTITLES,
            new SelectionForKeyPrefix("", "opensubtitles-releasemapping-name:"),
            new SelectionForKeyPrefix("", "opensubtitles-releasemapping-tvdbid:")),
        PODNAPISI("Podnapisi", SubtitleSource.PODNAPISI,
            new SelectionForKeyPrefix("", "podnapisi-releasemapping-name:"),
            new SelectionForKeyPrefix("", "podnapisi-releasemapping-tvdbid:"));

        public static final BiFunction<Manager, SelectionForKeyPrefix, List<Pair<String, SerieMapping>>>
            MAPPING_SUPPLIER;
        @val String name;
        @val String providerName;
        @val String nameColumn;
        @val String mappingColumn;
        @val String providerNameColumn;
        @val SelectionForKeyPrefix[] selectionForKeyPrefixList;

        @Override
        public String toString() {
            return name;
        }

        static {
            MAPPING_SUPPLIER = (manager, selectionForKeyPrefix) ->
                manager.getCache(CacheType.DISK, k -> k.startsWith(selectionForKeyPrefix.keyPrefix)).getEntries();
        }

        MappingType(String name, SubtitleSource subtitleSource, SelectionForKeyPrefix... selectionForKeyPrefixList) {
            this(name, subtitleSource.name(), selectionForKeyPrefixList);
        }

        MappingType(String name, String providerName, SelectionForKeyPrefix... selectionForKeyPrefixList) {
            this.name = name;
            this.providerName = providerName;
            this.nameColumn = getText("MappingEpisodeNameDialog.SceneShowName");
            this.mappingColumn = getText("MappingEpisodeNameDialog.ProviderId");
            this.providerNameColumn = getText("MappingEpisodeNameDialog.ProviderName");
            this.selectionForKeyPrefixList = selectionForKeyPrefixList;
        }
    }

    public record SelectionForKeyPrefix(String name, String keyPrefix, Function<String, String> deleteOtherFunction) {
        public SelectionForKeyPrefix(String name, String keyPrefix) {
            this(name, keyPrefix, null);
        }
    }

    private static class Row extends Vector<String> {
        @Serial private static final long serialVersionUID = 1L;
        @val String key;
        @val SelectionForKeyPrefix selectionForKeyPrefix;
        @var SerieMapping serieMapping;

        public Row(String key, String name, String providerId, String providerName, SerieMapping serieMapping,
            SelectionForKeyPrefix selectionForKeyPrefix) {
            this.key = key;
            this.serieMapping = serieMapping;
            this.selectionForKeyPrefix = selectionForKeyPrefix;
            add(name);
            add(providerId);
            add(providerName);
        }
    }

    private static class MappingTableModel extends DefaultTableModel {
        @Serial private static final long serialVersionUID = 1L;
        @val Manager manager;

        public MappingTableModel(Manager manager) {
            this.manager = manager;
        }

        void setMappingType(MappingType mappingType) {
            setDataVector(null,
                new String[]{mappingType.nameColumn, mappingType.mappingColumn, mappingType.providerNameColumn});
            Arrays.stream(mappingType.selectionForKeyPrefixList)
                .flatMap(selectionForKeyPrefix -> MappingType.MAPPING_SUPPLIER.apply(manager, selectionForKeyPrefix)
                    .stream()
                    .map(serieMappingPair -> {
                        SerieMapping serieMapping = serieMappingPair.getValue();
                        String providerId = serieMapping.providerId == null ? "" : serieMapping.providerId;
                        if (providerId.contains("/")) {
                            providerId = providerId.substring(providerId.lastIndexOf("/") + 1);
                        }
                        providerId = providerId.replace(".html", "");
                        return new Row(serieMappingPair.getKey(), serieMapping.name, providerId,
                            serieMapping.providerName, serieMapping, selectionForKeyPrefix);
                    }))
                .sorted(Comparator.comparing(
                    row -> row.serieMapping == null || row.serieMapping.providerName == null ? "zzz" :
                        row.serieMapping.name))
                .forEach(this::addRow);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
}
