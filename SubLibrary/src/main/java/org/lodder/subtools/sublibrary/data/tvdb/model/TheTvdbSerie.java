package org.lodder.subtools.sublibrary.data.tvdb.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.ToString;
import manifold.ext.props.rt.api.var;
import org.lodder.subtools.sublibrary.Language;

@ToString
public class TheTvdbSerie implements Serializable {
    @Serial
    private static final long serialVersionUID = -4036836377513152443L;
    @var int id;
    //@var String serieId;
    @var Language language;
    @var String serieName;
    @var String banner;
    //@var String overview;
    @var String firstAired;
    @var String imdbId;
    @var String zap2ItId;
    @var List<String> actors = new ArrayList<>();
    @var String airsDayOfWeek;
    @var String airsTime;
    @var String contentRating;
    @var List<String> genres = new ArrayList<>();
    @var String network;
    @var String rating;
    @var String runtime;
    @var String status;
    @var String fanArt;
    @var String lastUpdated;
    @var String poster;
}
