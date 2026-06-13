package org.lodder.subtools.multisubdownloader.subtitleprovider.addic7ed.model;

import java.io.Serial;
import java.time.YearMonth;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ProviderId;

@NullMarked
public class Addic7edMovieSubtitleId extends ProviderId {

    @Serial private static final long serialVersionUID = 1L;
    @val @Nullable String title;
    @val @Nullable Integer year;

    public Addic7edMovieSubtitleId(String text, String id, @Nullable String title=null, @Nullable Integer year=null) {
        super(text, id);
        this.title = title;
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
            score += 10;
        }
        int distance = calculateLevenshteinDistance(this.title != null ? this.title : name, title);
        score += distance == -1 ? 100 : distance;
        return score;
    }
}
