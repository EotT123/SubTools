package org.lodder.subtools.sublibrary.data.tvdb.model;

import static java.util.Objects.*;

import com.tvdb.model.SearchResult;
import com.tvdb.model.SeriesBaseRecord;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

@NullMarked
public class TvdbSerie extends SerieMapping {
    public TvdbSerie(String name, String providerId, String providerName) {
        super(name, providerId, providerName);
    }

    public TvdbSerie(String name, SearchResult searchResult) {
        this(name, requireNonNull(searchResult.tvdbId), requireNonNull(searchResult.name));
    }

    public TvdbSerie(String name, SeriesBaseRecord seriesBaseRecord) {
        this(name, String.valueOf(seriesBaseRecord.id), requireNonNull(seriesBaseRecord.name));
    }
}

