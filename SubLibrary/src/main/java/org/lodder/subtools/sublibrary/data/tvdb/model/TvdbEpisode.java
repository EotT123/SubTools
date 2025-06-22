package org.lodder.subtools.sublibrary.data.tvdb.model;

import java.io.Serializable;

import com.tvdb.model.SeriesBaseRecord;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record TvdbEpisode(@Nullable Integer id, @Nullable Long seriesId, @Nullable String name,
    @Nullable Integer number, @Nullable Integer seasonNumber, @Nullable String year) implements Serializable {

    public TvdbEpisode(SeriesBaseRecord seriesBaseRecord) {
        this(seriesBaseRecord.id, seriesBaseRecord.getEpisodes().first().seriesId, seriesBaseRecord.name,
            seriesBaseRecord.getEpisodes().first().number, seriesBaseRecord.getEpisodes().first().seasonNumber,
            seriesBaseRecord.getEpisodes().first().year);
    }
}
