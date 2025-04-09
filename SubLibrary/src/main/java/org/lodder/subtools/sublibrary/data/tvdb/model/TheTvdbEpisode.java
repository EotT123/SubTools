package org.lodder.subtools.sublibrary.data.tvdb.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.ToString;
import manifold.ext.props.rt.api.var;
import org.lodder.subtools.sublibrary.Language;

@ToString
public class TheTvdbEpisode implements Serializable {
    @Serial
    private static final long serialVersionUID = 913790243120597542L;
    @var String id;
    @var String combinedEpisodeNumber;
    @var String combinedSeason;
    @var String dvdChapter;
    @var String dvdDiscId;
    @var String dvdEpisodeNumber;
    @var String dvdSeason;
    @var List<String> directors = new ArrayList<>();
    @var String epImgFlag;
    @var String episodeName;
    @var int episodeNumber;
    @var String firstAired;
    @var List<String> guestStars = new ArrayList<>();
    @var String imdbId;
    @var Language language;
    // @var String overview;
    @var String productionCode;
    @var String rating;
    @var int seasonNumber;
    @var List<String> writers = new ArrayList<>();
    @var String absoluteNumber;
    @var int airsAfterSeason;
    @var int airsBeforeSeason;
    @var int airsBeforeEpisode;
    @var String filename;
    @var String lastUpdated;
    @var String seriesId;
    @var String seasonId;
}
