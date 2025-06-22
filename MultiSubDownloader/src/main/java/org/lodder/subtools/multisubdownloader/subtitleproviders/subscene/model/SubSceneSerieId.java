package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.Serial;
import java.time.YearMonth;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ProviderId;

public class SubSceneSerieId extends ProviderId {

    @Serial
    private static final long serialVersionUID = 1L;

    @val @Nullable String serieName;
    @val @Nullable Integer season;

    public SubSceneSerieId(String text, String id, @Nullable String serieName=null, @Nullable Integer season=null) {
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
        int distance = calculateLevenshteinDistance(this.serieName != null ? this.serieName : name, title);
        score += distance == -1 ? 100 : distance;
        return score;
    }
}
