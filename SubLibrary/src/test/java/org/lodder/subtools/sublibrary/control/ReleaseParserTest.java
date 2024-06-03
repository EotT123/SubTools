package org.lodder.subtools.sublibrary.control;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.lodder.subtools.sublibrary.exception.ReleaseParseException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.model.VideoType;

class ReleaseParserTest {

    @Test
    void testReleaseGroup() {
        String releaseGroup = ReleaseParser.extractReleaseGroup("The.Following.S03E01.HDTV.XviD-AFG", false);
        assertThat(releaseGroup).isEqualTo("AFG");

        releaseGroup = ReleaseParser.extractReleaseGroup("The.Following.S03E01.HDTV.XviD-AFG", true);
        assertThat(releaseGroup).isEqualTo("A");

        releaseGroup = ReleaseParser.extractReleaseGroup("The.Following.S03E01.HDTV.XviD-AFG.srt", false);
        assertThat(releaseGroup).isEmpty();

        releaseGroup = ReleaseParser.extractReleaseGroup("The.Following.S03E01.HDTV.XviD-AFG.srt", true);
        assertThat(releaseGroup).isEqualTo("AFG");
    }

    @Test
    void testListGetQualityKeyWords() throws Exception {
        ReleaseParser releaseparser = new ReleaseParser();

        Path file = Path.of("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.mkv");
        Release release = releaseparser.parse(file);

        List<String> q = ReleaseParser.getQualityKeyWords(release.quality);

        assertThat(q).containsExactly("720p", "hdtv", "x264");

        file = Path.of("The.Drop.2014.1080p.WEB-DL.DD5.1.H264-RARBG.mkv");
        release = releaseparser.parse(file);

        q = ReleaseParser.getQualityKeyWords(release.quality);

        assertThat(q).containsExactly("1080p", "web-dl", "dd5 1", "h264");
    }

    @Test
    void testTV() throws Exception {
        ReleaseParser releaseparser = new ReleaseParser();

        Path file = Path.of("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.mkv");
        Release release = releaseparser.parse(file);

        assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
        assertThat(release.extension).isEqualTo("mkv");
        assertThat(release.fileName).isEqualTo("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.mkv");
        assertThat(release.releaseGroup).isEqualTo("DIMENSION");
        assertThat(release.quality).isEqualTo("720p hdtv x264");

        TvRelease tvrelease = (TvRelease) release;

        assertThat(tvrelease.season).isEqualTo(10);
        assertThat(tvrelease.episodeNumbers).hasSize(1);
        assertThat(tvrelease.firstEpisodeNumber).isEqualTo(12);

        file = Path.of("S04E02 - White Collar - Most Wanted.mkv");
        release = releaseparser.parse(file);

        assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
        assertThat(release.extension).isEqualTo("mkv");
        assertThat(release.fileName).isEqualTo("S04E02 - White Collar - Most Wanted.mkv");
        assertThat(release.releaseGroup).isEmpty();
        assertThat(release.quality).isEmpty();

        tvrelease = (TvRelease) release;

        assertThat(tvrelease.season).isEqualTo(4);
        assertThat(tvrelease.episodeNumbers).hasSize(1);
        assertThat(tvrelease.firstEpisodeNumber).isEqualTo(2);

        file = Path.of("Spartacus.Gods.of.The.Arena.Pt.I.720p.HDTV.X264-DIMENSION.mkv");
        release = releaseparser.parse(file);

        assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
        assertThat(release.extension).isEqualTo("mkv");
        assertEquals(release.fileName, "Spartacus.Gods.of.The.Arena.Pt.I.720p.HDTV.X264-DIMENSION.mkv");
        assertThat(release.releaseGroup).isEqualTo("DIMENSION");
        assertThat(release.quality).isEqualTo("720p hdtv x264");

        tvrelease = (TvRelease) release;

        assertThat(tvrelease.season).isEqualTo(1);
        assertThat(tvrelease.episodeNumbers).hasSize(1);
        assertThat(tvrelease.firstEpisodeNumber).isEqualTo(1);

        file = Path.of("hawaii.five-0.2010.410.hdtv-lol.mp4");
        release = releaseparser.parse(file);

        assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
        assertThat(release.extension).isEqualTo("mp4");
        assertThat(release.fileName).isEqualTo("hawaii.five-0.2010.410.hdtv-lol.mp4");
        assertThat(release.releaseGroup).isEqualTo("lol");
        assertThat(release.quality).isEqualTo("hdtv");

        tvrelease = (TvRelease) release;

        assertThat(tvrelease.season).isEqualTo(4);
        assertThat(tvrelease.episodeNumbers).hasSize(1);
        assertThat(tvrelease.firstEpisodeNumber).isEqualTo(10);

        file = Path.of("Greys.Anatomy.S10E01E02.720p.HDTV.X264-DIMENSION.mkv");
        release = releaseparser.parse(file);

        assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
        assertThat(release.extension).isEqualTo("mkv");
        assertThat(release.fileName).isEqualTo("Greys.Anatomy.S10E01E02.720p.HDTV.X264-DIMENSION.mkv");
        assertThat(release.releaseGroup).isEqualTo("DIMENSION");
        assertThat(release.quality).isEqualTo("720p hdtv x264");

        tvrelease = (TvRelease) release;

        assertThat(tvrelease.season).isEqualTo(10);
        assertThat(tvrelease.episodeNumbers).hasSize(2);
        assertThat(tvrelease.firstEpisodeNumber).isEqualTo(1);
        assertThat(tvrelease.episodeNumbers.get(1)).isEqualTo(2);
    }

    @Test
    void testReleaseParseExceptionMessage() {
        Path file = Path.of("exceptiontesting.mkv");

        assertThatExceptionOfType(ReleaseParseException.class)
                .isThrownBy(() -> new ReleaseParser().parse(file))
                .withMessage("Unknown format, can't be parsed: " + file.toAbsolutePath());
    }

    @Test
    void testMovie() throws Exception {
        ReleaseParser releaseparser = new ReleaseParser();

        Path file = Path.of("Back.to.the.Future.Part.II.1989.720p.BluRay.X264-AMIABLE.mkv");
        Release release = releaseparser.parse(file);

        assertThat(release.videoType).isEqualTo(VideoType.MOVIE);
        assertThat(release.extension).isEqualTo("mkv");
        assertThat(release.fileName).isEqualTo("Back.to.the.Future.Part.II.1989.720p.BluRay.X264-AMIABLE.mkv");
        assertThat(release.releaseGroup).isEqualTo("AMIABLE");
        assertThat(release.quality).isEqualTo("720p bluray x264");

        MovieRelease movieRelease = (MovieRelease) release;

        assertThat(movieRelease.year).isEqualTo(1989);
        assertThat(movieRelease.name).isEqualTo("Back to the Future Part II");

        file = Path.of("The.Equalizer.2014.720p.BluRay.x264-SPARKS.mkv");
        release = releaseparser.parse(file);

        assertThat(release.videoType).isEqualTo(VideoType.MOVIE);
        assertThat(release.extension).isEqualTo("mkv");
        assertThat(release.fileName).isEqualTo("The.Equalizer.2014.720p.BluRay.x264-SPARKS.mkv");
        assertThat(release.releaseGroup).isEqualTo("SPARKS");
        assertThat(release.quality).isEqualTo("720p bluray x264");

        movieRelease = (MovieRelease) release;

        assertThat(movieRelease.year).isEqualTo(2014);
        assertThat(movieRelease.name).isEqualTo("The Equalizer");

        file = Path.of("The.Trip.to.Italy.2014.LIMITED.720p.BluRay.x264-GECKOS.mkv");
        release = releaseparser.parse(file);

        assertThat(release.videoType).isEqualTo(VideoType.MOVIE);
        assertThat(release.extension).isEqualTo("mkv");
        assertEquals(release.getFileName(), "The.Trip.to.Italy.2014.LIMITED.720p.BluRay.x264-GECKOS.mkv");
        assertThat(release.releaseGroup).isEqualTo("GECKOS");
        assertThat(release.quality).isEqualTo("720p bluray x264");

        movieRelease = (MovieRelease) release;

        assertThat(movieRelease.year).isEqualTo(2014);
        assertThat(movieRelease.name).isEqualTo("The Trip to Italy");
    }
}
