package org.lodder.subtools.multisubdownloader.gui.extra.table;

import module java.base;
import static org.lodder.subtools.multisubdownloader.gui.extra.table.SearchColumnName.*;

import javax.swing.table.DefaultTableModel;
import java.util.Map.Entry;

import manifold.ext.props.rt.api.get;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;

@NullMarked
public class VideoTableModel extends DefaultTableModel {

    @Serial private static final long serialVersionUID = 1L;

    private static final List<SearchColumnName> SHOW_COLUMNS =
        List.of(RELEASE, FILENAME, TITLE, SEASON, EPISODE, FOUND, SELECT, OBJECT);

    private static final List<SearchColumnName> SUBTITLE_COLUMNS = List.of(FILENAME, SOURCE, SCORE, SELECT, OBJECT);

    private static final Map<SearchColumnName, Integer> SHOW_COLUMNS_INDEX = IntStream.range(0, SHOW_COLUMNS.size())
        .collect(() -> new EnumMap<>(SearchColumnName.class), (map, i) -> map.put(SHOW_COLUMNS.get(i), i),
            (l, r) -> {
                throw new IllegalArgumentException("Duplicate keys [$l] and [$r]");
            });

    private final Class<?>[] columnTypes;
    private final Boolean[] columnEditables;
    private final Map<ReleaseWithPath, Row> rowMap = new LinkedHashMap<>();

    private boolean showOnlyFound = false;
    @var UserInteractionHandler userInteractionHandler;

    private VideoTableModel(List<SearchColumnName> searchColumnNames) {
        super(new Object[][]{}, searchColumnNames.stream().map(SearchColumnName::getColumnName).toArray(String[]::new));
        this.columnTypes = searchColumnNames.stream().map(SearchColumnName::getClazz).toArray(Class<?>[]::new);
        this.columnEditables = searchColumnNames.stream().map(SearchColumnName::isEditable).toArray(Boolean[]::new);
    }

    public static VideoTableModel getDefaultVideoTableModel() {
        return new VideoTableModel(SHOW_COLUMNS);
    }

    public static VideoTableModel getDefaultSubtitleTableModel() {
        return new VideoTableModel(SUBTITLE_COLUMNS);
    }

    public void addRows(List<ReleaseWithPath> l) {
        l.forEach(this::addRow);
    }

    public void addRow(ReleaseWithPath release) {
        /* If we try to add an existing release, we just have to update that row */
        synchronized (this) {
            if (rowMap.containsKey(release)) {
                Row row = this.rowMap.get(release);
                int rowNr = new ArrayList<>(rowMap.keySet()).indexOf(release);
                int subsFound = row.updateSubsFound();
                this.setValueAt(subsFound, rowNr, SHOW_COLUMNS_INDEX.get(SearchColumnName.FOUND));
                return;
            }

            if (!showOnlyFound || release.matchingSubCount != 0) {
                Row row = createRow(release);
                rowMap.put(release, row);
                this.addRow(row.rowObject);
            }
        }
    }

    private Row createRow(Release release) {
        return new Row(release, userInteractionHandler);
    }

    @NullMarked
    private static class Row {
        private final Release release;
        private final UserInteractionHandler userInteractionHandler;
        @get Vector<@Nullable Object> rowObject;

        public Row(Release release, UserInteractionHandler userInteractionHandler) {
            this.release = release;
            this.userInteractionHandler = userInteractionHandler;
            this.rowObject = SHOW_COLUMNS.stream().map(searchColumn -> switch (searchColumn) {
                case RELEASE -> switch (release) {
                    case TvRelease tvRelease -> tvRelease.originalName;
                    case MovieRelease movieRelease -> movieRelease.name;
                };
                case FILENAME -> release instanceof ReleaseWithPath r ? r.fileName : null;
                case FOUND -> calculateSubsFound();
                case SELECT -> false;
                case OBJECT -> release;
                case SEASON -> release instanceof TvReleaseWithoutPath tvRelease ? tvRelease.season : null;
                case EPISODE -> release instanceof TvReleaseWithoutPath tvRelease ? tvRelease.firstEpisode : null;
                case TITLE -> release instanceof TvReleaseWithoutPath tvRelease ? tvRelease.title : null;
                default -> throw new IllegalArgumentException("Unexpected value: " + searchColumn);
            }).collect(Collectors.toCollection(Vector::new));
        }

        private int calculateSubsFound() {
            return userInteractionHandler != null ?
                userInteractionHandler.getAutomaticSelection(release.matchingSubs).size() : release.matchingSubCount;
        }

        public int updateSubsFound() {
            synchronized (this) {
                int subsFound = calculateSubsFound();
                rowObject.set(SHOW_COLUMNS_INDEX.get(SearchColumnName.FOUND), subsFound);
                return subsFound;
            }
        }

        public boolean isSelected() {
            return (boolean) rowObject.get(SHOW_COLUMNS_INDEX.get(SearchColumnName.SELECT));
        }
    }

    public void addRow(Subtitle subtitle) {
        synchronized (this) {
            Vector<Object> row = SUBTITLE_COLUMNS.stream().map(searchColumn -> switch (searchColumn) {
                case FILENAME -> subtitle.fileName;
                case SELECT -> false;
                case OBJECT -> subtitle;
                case SOURCE -> subtitle.source;
                case SCORE -> subtitle.score;
                default -> throw new IllegalArgumentException("Unexpected value: " + searchColumn);
            }).collect(Collectors.toCollection(Vector::new));
            this.addRow(row);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return columnEditables[column];
    }

    public void processTable(Runnable runnable) {
        synchronized (this) {
            runnable.run();
        }
    }

    @Override
    public void removeRow(int i) {
        throw new IllegalStateException("Should not be used!)");
    }

    public void removeShow(Release selectedShow) {
        Iterator<ReleaseWithPath> iterator = rowMap.keySet().iterator();
        int idx = -1;
        while (iterator.hasNext()) {
            idx++;
            if (iterator.next() == selectedShow) {
                iterator.remove();
                super.removeRow(idx);
                return;
            }
        }
    }

    private void updateTable() {
        synchronized (this) {
            List<ReleaseWithPath> newRowList = new ArrayList<>(this.rowMap.keySet());
            clearTable();
            addRows(newRowList);
        }
    }

    public void clearTable() {
        synchronized (this) {
            while (getRowCount() > 0) {
                super.removeRow(0);
            }
            rowMap.clear();
        }
    }

    public void setShowOnlyFound(boolean showOnlyFound) {
        this.showOnlyFound = showOnlyFound;
        updateTable();
    }

    public void executedSynchronized(Runnable runnable) {
        synchronized (this) {
            runnable.run();
        }
    }

    public int getSelectedAmountOfShows() {
        return (int) rowMap.values().stream().filter(Row::isSelected).count();
    }

    public List<ReleaseWithPath> getSelectedShows() {
        return rowMap.entrySet().stream().filter(entry -> entry.getValue().isSelected()).map(Entry::getKey).toList();
    }
}
