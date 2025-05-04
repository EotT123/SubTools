package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ProviderId;
import subdl.Serie;
import subdl.Serie.ReleaseType;

public class SubdlSerieId extends ProviderId {

    @Serial
    private static final long serialVersionUID = 5858875211782260667L;

    @val @Nullable Integer year;
    @val ReleaseType releaseType;
//    @val ReleaseType type;
//    @val Optional<String> imdbId;
//    @val OptionalInt tmdbId;

    public SubdlSerieId(Serie.ResultItem resultItem) {
        super(resultItem.name, String.valueOf(resultItem.sd_id));
        this.year = resultItem.year == null ? null : Integer.parseInt(resultItem.year);
//        this.type = resultItem.type;
//        this.imdbId = Optional.ofNullable(resultItem.imdb_id);
//        this.tmdbId = resultItem.tmdb_id == 0 ? OptionalInt.empty() : OptionalInt.of(resultItem.tmdb_id);
    }

    /**
     * The lower the score, the better the result
     *
     * @return the score
     */
    public int getScore(String name) {
        return calculateLevenshteinDistance(this.name , name);
    }
}
