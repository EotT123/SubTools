package org.lodder.subtools.multisubdownloader.lib.control.subtitles.sorting;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.Subtitle;

@NullMarked
public class ScoreCalculator {

    private final SortWeight weights;

    public ScoreCalculator(SortWeight weights) {
        this.weights = weights;
    }

    public int calculate(Subtitle subtitle) {
        if (weights.maxScore <= 0) {
            return 0;
        }
        String subtitleInfo =
                "%s %s %s".formatted(subtitle.fileName, subtitle.quality, subtitle.releaseGroup).trim().toLowerCase();
        int score =
                weights.weights.keySet().stream().filter(subtitleInfo::contains).mapToInt(weights.weights::get).sum();
        return (int) Math.ceil((float) score / weights.maxScore * 100);
    }
}
