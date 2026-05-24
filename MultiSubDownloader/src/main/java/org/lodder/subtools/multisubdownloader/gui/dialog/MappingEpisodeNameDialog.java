package org.lodder.subtools.multisubdownloader.gui.dialog;

import static org.lodder.subtools.multisubdownloader.Messages.*;
import static util.Utils.*;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.Serial;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.function.BiFunction;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandlerGUI;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.cache.ProviderCacheKey;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

@NullMarked
public class MappingEpisodeNameDialog extends MultiSubDialog {

    @Serial private static final long serialVersionUID = 1L;

    private final MappingTableModel mappingTableModel;
    private final JButton btnAddCustomMapping;
    private final JTable table;
    private @Nullable SubtitleProvider selectedSubtitleProvider;
    private MappingType selectedMappingType;

    public MappingEpisodeNameDialog(@Nullable JFrame frame=null, UserInteractionHandlerGUI userInteractionHandler) {
        super(frame, getText("MappingEpisodeNameDialog.Title"), true);
        this.mappingTableModel = new MappingTableModel();
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
                        new CacheKey(CacheType.DISK, row.key).remove();
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
                            if (selectedSubtitleProvider != null) {
                                userInteractionHandler.enter(message).ifPresent(newName -> {
                                    TvReleaseWithoutPath tvRelease = new TvReleaseWithoutPath(
                                        name:currentName,
                                        season:row.serieMapping.season,
                                        episode:1,
                                        originalName:currentName,
                                        customName:newName,
                                        completeName:currentName);
                                    try {
                                        ifNotNullOrElseDo(selectedSubtitleProvider.getProviderSerieMapping(tvRelease),
                                            serieId -> {
                                                row.serieMapping =
                                                    new SerieMapping(currentName, serieId.providerId,
                                                        serieId.providerName,
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
                                });
                            }
                        }))
                .addComponent("skip", new JButton(getText("App.Close"))
                    .defaultButtonFor(getRootPane())
                    .actionListener(() -> setVisible(false))
                    .actionCommand(getText("App.Close"))));
        selectMappingType(MappingType.values()[0]);
    }

    private void selectMappingType(MappingType mappingType) {
        this.selectedMappingType = mappingType;
        this.selectedSubtitleProvider = SubtitleProviderStore.allProviders.stream()
            .filter(subtitleProvider -> subtitleProvider.source.name.equals(mappingType.provider))
            .findAny().orElse(null);
        btnAddCustomMapping.enabled = selectedSubtitleProvider != null;
        mappingTableModel.mappingType = mappingType;
        repaint();
    }

    @NullMarked
    public enum MappingType {
        TVDB("TVDB", "TVDB", "EPISODEmapping"),
        IMDB("IMDB", "IMDB", "EPISODEmapping"),
        ADDIC7ED(SubtitleProviderFrontEnd.ADDIC7ED, "EPISODEmapping"),
        ADDIC7ED_PROXY(SubtitleProviderFrontEnd.ADDIC7ED_GESTDOWN, "EPISODEmapping"),
        SUBSCENE(SubtitleProviderFrontEnd.SUBSCENE, "EPISODEmapping"),
        TV_SUBTITLES(SubtitleProviderFrontEnd.TVSUBTITLES, "EPISODEmapping"),
        OPEN_SUBTITLES(SubtitleProviderFrontEnd.OPENSUBTITLES, "EPISODEmapping"),
        PODNAPISI(SubtitleProviderFrontEnd.PODNAPISI, "EPISODEmapping"),
        SUBDL(SubtitleProviderFrontEnd.SUBDL, "EPISODEmapping");

        private static final BiFunction<String, String, List<Pair<ProviderCacheKey, SerieMapping>>>
            MAPPING_SUPPLIER = (provider, type) -> Manager.getInstance()
            .getEntries(CacheType.DISK, key -> provider.equals(key.provider) && type.equals(key.type));

        @val String providerDisplayName;
        @val String provider;
        @val String type;
        @val String nameColumn;
        @val String mappingColumn;
        @val String providerNameColumn;

        @Override
        public String toString() {
            return providerDisplayName;
        }

        MappingType(SubtitleProviderFrontEnd subtitleProviderFrontEnd, String type) {
            this(subtitleProviderFrontEnd.name, subtitleProviderFrontEnd.subtitleSource.name(), type);
        }

        MappingType(String providerDisplayName, String provider, String type) {
            this.providerDisplayName = providerDisplayName;
            this.provider = provider;
            this.type = type;
            this.nameColumn = getText("MappingEpisodeNameDialog.SceneShowName");
            this.mappingColumn = getText("MappingEpisodeNameDialog.ProviderId");
            this.providerNameColumn = getText("MappingEpisodeNameDialog.ProviderName");
        }

        public List<Pair<ProviderCacheKey, @Nullable SerieMapping>> getValues() {
            return MAPPING_SUPPLIER.apply(provider, type);
        }
    }

    @NullMarked
    private static class Row extends Vector<String> {
        @Serial private static final long serialVersionUID = 1L;
        @val ProviderCacheKey key;
        @var @Nullable SerieMapping serieMapping;

        public Row(ProviderCacheKey key, String name, String providerId, @Nullable String providerName,
            SerieMapping serieMapping) {
            this.key = key;
            this.serieMapping = serieMapping;
            add(name);
            add(providerId);
            add(providerName);
        }
    }

    @NullMarked
    private static class MappingTableModel extends DefaultTableModel {
        @Serial private static final long serialVersionUID = 1L;

        void setMappingType(MappingType mappingType) {
            setDataVector(null,
                new String[]{mappingType.nameColumn, mappingType.mappingColumn, mappingType.providerNameColumn});
            mappingType.getValues().stream()
                .mapFilterNonNull(serieMappingPair -> {
                    SerieMapping serieMapping = serieMappingPair.getValue();
                    if (serieMapping == null) {
                        return null;
                    }
                    String providerId = serieMapping.providerId == null ? "" : serieMapping.providerId;
                    if (providerId.contains("/")) {
                        providerId = providerId.substring(providerId.lastIndexOf("/") + 1);
                    }
                    providerId = providerId.replace(".html", "");
                    return new Row(serieMappingPair.getKey(), serieMapping.name, providerId,
                        serieMapping.providerName, serieMapping);
                })
                .sorted(Comparator.comparing(row -> row.serieMapping.name))
                .forEach(this::addRow);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
}
