package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.Serial;
import java.time.YearMonth;

import manifold.ext.props.rt.api.val;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jspecify.annotations.Nullable;

public class SubSceneSerieId extends SubSceneId {

    @Serial
    private static final long serialVersionUID = 5858875211782260667L;

    @val @Nullable String serieName;
    @val @Nullable Integer season;

    public SubSceneSerieId(String text, String id, @Nullable String serieName=null,
        @Nullable Integer season=null) {
        super(text, id);
        this.serieName = serieName;
        this.season = season;
    }

    /**
     * The lower the score, the better the result
     *
     * @return the score
     */
    public int getScore(String title, @Nullable Integer season) {
        int score = 0;
        if (season != null && this.season != null) {
            score += Math.abs(season - this.season);
        } else if (this.season != null) {
            score += Math.abs(YearMonth.now().year - this.season);
        } else {
            score += 100;
        }
        int distance = new LevenshteinDistance(100).apply(this.serieName != null ? this.serieName : name, title);
        score += distance == -1 ? 100 : distance;
        return score;
    }

}
