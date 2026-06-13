package org.lodder.subtools.multisubdownloader.subtitleprovider.subdl.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ProviderId;
import subdl.Serie;
import subdl.Serie.ReleaseType;

@NullMarked
public class SubdlSerieId extends ProviderId {

    @Serial
    private static final long serialVersionUID = 1L;

    @val @Nullable Integer year;
    @val ReleaseType releaseType;

    public SubdlSerieId(Serie.ResultItem resultItem) {
        super(resultItem.name, String.valueOf(resultItem.sd_id));
        this.year = resultItem.year == null ? null : Integer.parseInt(resultItem.year);
        this.releaseType = resultItem.type;
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
