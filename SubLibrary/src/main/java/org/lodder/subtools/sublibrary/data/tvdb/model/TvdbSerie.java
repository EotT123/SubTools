package org.lodder.subtools.sublibrary.data.tvdb.model;

import com.tvdb.model.SearchResult;
import com.tvdb.model.SeriesBaseRecord;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

public class TvdbSerie extends SerieMapping {
    public TvdbSerie(String name, String providerId, String providerName) {
        super(name, providerId, providerName);
    }

    public TvdbSerie(String name, SearchResult searchResult) {
        this(name, searchResult.tvdbId, searchResult.name);
    }

    public TvdbSerie(String name, SeriesBaseRecord seriesBaseRecord) {
        this(name, String.valueOf(seriesBaseRecord.id), seriesBaseRecord.name);
    }
}

