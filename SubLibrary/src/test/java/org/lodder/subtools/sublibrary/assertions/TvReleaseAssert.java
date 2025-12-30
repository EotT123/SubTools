package org.lodder.subtools.sublibrary.assertions;

import static org.assertj.core.api.Assertions.*;

import manifold.ext.rt.api.Self;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.model.VideoType;

@NullMarked
public class TvReleaseAssert extends ReleaseAssert<TvRelease> {

    public TvReleaseAssert(TvRelease actual) {
        super(actual);
    }

    public @Self TvReleaseAssert hasSeason(int season) {
        isNotNull();
        assertThat(actual.season).isEqualTo(season);
        return this;
    }

    public @Self TvReleaseAssert hasEpisodes(Integer... episodeNumbers) {
        isNotNull();
        assertThat(actual.episodes).containsExactlyInAnyOrder(episodeNumbers);
        return this;
    }

    public @Self TvReleaseAssert hasName(String name) {
        isNotNull();
        assertThat(actual.name).isEqualTo(name);
        return this;
    }

    public @Self TvReleaseAssert hasTitle(String title) {
        isNotNull();
        assertThat(actual.title).isEqualTo(title);
        return this;
    }

    public @Self TvReleaseAssert withoutTitle() {
        isNotNull();
        assertThat(actual.title).isEmpty();
        return this;
    }

    public @Self TvReleaseAssert isSpecial() {
        isNotNull();
        assertThat(actual.special).isTrue();
        return this;
    }

    public @Self TvReleaseAssert isNotSpecial() {
        isNotNull();
        assertThat(actual.special).isFalse();
        return this;
    }

    public @Self TvReleaseAssert hasEpisodeVideoType() {
        isNotNull();
        assertThat(actual.videoType).isEqualTo(VideoType.EPISODE);
        return this;
    }
}
