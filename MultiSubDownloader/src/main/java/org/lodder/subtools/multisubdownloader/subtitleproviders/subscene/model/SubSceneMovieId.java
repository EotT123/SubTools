package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.Serial;
import java.time.YearMonth;

import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jspecify.annotations.Nullable;

public class SubSceneMovieId extends SubSceneId {

    @Serial
    private static final long serialVersionUID = 5858875211782260667L;

    @val @Nullable String title;
    @val @Nullable Integer year;

    public SubSceneMovieId(String text, String id, @Nullable String title=null, @Nullable Integer year=null) {
        super(text, id);
        this.title = StringUtils.trimToNull(title);
        this.year = year;
    }

    /**
     * The lower the score, the better the result
     *
     * @return the score
     */
    public int getScore(String title, @Nullable Integer year) {
        int score = 0;
        if (year != null && this.year != null) {
            score += Math.abs(year - this.year);
        } else if (this.year != null) {
            score += Math.abs(YearMonth.now().year - this.year);
        } else {
            score += 100;
        }
        int distance = new LevenshteinDistance(100).apply(this.title != null ? this.title : name, title);
        score += distance == -1 ? 100 : distance;
        return score;
    }
}
