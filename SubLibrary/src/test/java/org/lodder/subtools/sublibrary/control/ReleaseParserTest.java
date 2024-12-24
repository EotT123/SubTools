package org.lodder.subtools.sublibrary.control;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Nested;
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
        assertThat(releaseGroup).isEmpty();

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

        assertThat(ReleaseParser.getQualityKeyWords(release.quality)).containsExactly("720p", "hdtv", "x264");

        file = Path.of("The.Drop.2014.1080p.WEB-DL.DD5.1.H264-RARBG.mkv");
        release = releaseparser.parse(file);

        assertThat(ReleaseParser.getQualityKeyWords(release.quality))
            .containsExactly("1080p", "web-dl", "dd5.1", "x264");
    }

    @Nested
    class testTV {

        private final ReleaseParser releaseparser = new ReleaseParser();

        @Nested
        class StartsWithSeasonEpisode {


            @Test
            void StartsWithSeasonEpisode1() throws Exception {
                Path file = Path.of("S04E02 - White Collar - Most Wanted.mkv");
                Release release = releaseparser.parse(file);

                assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
                assertThat(release.extension).isEqualTo("mkv");
                assertThat(release.fileName).isEqualTo("S04E02 - White Collar - Most Wanted.mkv");
                assertThat(release.releaseGroup).isEmpty();
                assertThat(release.quality).isEmpty();

                TvRelease tvrelease = (TvRelease) release;

                assertThat(tvrelease.season).isEqualTo(4);
                assertThat(tvrelease.episodeNumbers).containsExactly(2);
                assertThat(tvrelease.name).isEqualTo("White Collar");
                assertThat(tvrelease.title).isEqualTo("Most Wanted");
            }

            @Test
            void StartsWithSeasonEpisode2() throws Exception {
                Path file = Path.of("(S04E02) - White Collar - Most Wanted.mkv");
                Release release = releaseparser.parse(file);

                assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
                assertThat(release.extension).isEqualTo("mkv");
                assertThat(release.fileName).isEqualTo("(S04E02) - White Collar - Most Wanted.mkv");
                assertThat(release.releaseGroup).isEmpty();
                assertThat(release.quality).isEmpty();

                TvRelease tvrelease = (TvRelease) release;

                assertThat(tvrelease.season).isEqualTo(4);
                assertThat(tvrelease.episodeNumbers).containsExactly(2);
                assertThat(tvrelease.name).isEqualTo("White Collar");
                assertThat(tvrelease.title).isEqualTo("Most Wanted");
            }

            @Test
            void StartsWithSeasonEpisode3() throws Exception {
                Path file = Path.of("(S04E02) White Collar - Most Wanted.mkv");
                Release release = releaseparser.parse(file);

                assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
                assertThat(release.extension).isEqualTo("mkv");
                assertThat(release.fileName).isEqualTo("(S04E02) White Collar - Most Wanted.mkv");
                assertThat(release.releaseGroup).isEmpty();
                assertThat(release.quality).isEmpty();

                TvRelease tvrelease = (TvRelease) release;

                assertThat(tvrelease.season).isEqualTo(4);
                assertThat(tvrelease.episodeNumbers).containsExactly(2);
                assertThat(tvrelease.name).isEqualTo("White Collar");
                assertThat(tvrelease.title).isEqualTo("Most Wanted");
            }
        }


        @Test
        void testTV1() throws Exception {
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
            assertThat(tvrelease.name).isEqualTo("Criminal Minds");
            assertThat(tvrelease.title).isNull();
        }

        @Test
        void testTV4() throws Exception {
            Path file = Path.of("Spartacus.Gods.of.The.Arena.Pt.IV.720p.HDTV.X264-DIMENSION.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.fileName).isEqualTo("Spartacus.Gods.of.The.Arena.Pt.IV.720p.HDTV.X264-DIMENSION.mkv");
            assertThat(release.releaseGroup).isEqualTo("DIMENSION");
            assertThat(release.quality).isEqualTo("720p hdtv x264");

            TvRelease tvrelease = (TvRelease) release;

            assertThat(tvrelease.season).isEqualTo(1);
            assertThat(tvrelease.episodeNumbers).containsExactly(4);
            assertThat(tvrelease.name).isEqualTo("Spartacus Gods of The Arena");
            assertThat(tvrelease.title).isNull();
        }

        @Test
        void testTV5() throws Exception {
            Path file = Path.of("hawaii.five-0.2010.410.hdtv-lol.mp4");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
            assertThat(release.extension).isEqualTo("mp4");
            assertThat(release.fileName).isEqualTo("hawaii.five-0.2010.410.hdtv-lol.mp4");
            assertThat(release.releaseGroup).isEqualTo("lol");
            assertThat(release.quality).isEqualTo("hdtv");

            TvRelease tvrelease = (TvRelease) release;

            assertThat(tvrelease.season).isEqualTo(4);
            assertThat(tvrelease.episodeNumbers).hasSize(1);
            assertThat(tvrelease.firstEpisodeNumber).isEqualTo(10);
            assertThat(tvrelease.name).isEqualTo("hawaii five-0");
            assertThat(tvrelease.title).isNull();
        }

        @Test
        void testTV6() throws Exception {
            Path file = Path.of("Greys.Anatomy.S10E01E02.720p.HDTV.X264-DIMENSION.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.fileName).isEqualTo("Greys.Anatomy.S10E01E02.720p.HDTV.X264-DIMENSION.mkv");
            assertThat(release.releaseGroup).isEqualTo("DIMENSION");
            assertThat(release.quality).isEqualTo("720p hdtv x264");

            TvRelease tvrelease = (TvRelease) release;

            assertThat(tvrelease.season).isEqualTo(10);
            assertThat(tvrelease.episodeNumbers).containsExactly(1, 2);
            assertThat(tvrelease.name).isEqualTo("Greys Anatomy");
            assertThat(tvrelease.title).isNull();
        }

        @Test
        void testTV7() throws Exception {
            Path file = Path.of("Greys.Anatomy.S10E01E02 Seal Our Fate 720p.HDTV.X264-DIMENSION.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.fileName).isEqualTo(
                "Greys.Anatomy.S10E01E02 Seal Our Fate 720p.HDTV.X264-DIMENSION.mkv");
            assertThat(release.releaseGroup).isEqualTo("DIMENSION");
            assertThat(release.quality).isEqualTo("720p hdtv x264");

            TvRelease tvrelease = (TvRelease) release;

            assertThat(tvrelease.season).isEqualTo(10);
            assertThat(tvrelease.episodeNumbers).containsExactly(1, 2);
            assertThat(tvrelease.name).isEqualTo("Greys Anatomy");
            assertThat(tvrelease.title).isEqualTo("Seal Our Fate");
        }


        @Test
        void testTV8() throws Exception {
            Path file = Path.of("(2-11) Joey and the High School Friend.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.fileName).isEqualTo("(2-11) Joey and the High School Friend.mkv");
            assertThat(release.releaseGroup).isEmpty();
            assertThat(release.quality).isEmpty();

            TvRelease tvrelease = (TvRelease) release;

            assertThat(tvrelease.season).isEqualTo(2);
            assertThat(tvrelease.episodeNumbers).containsExactly(11);
            assertThat(tvrelease.name).isEqualTo("Joey and the High School Friend");
            assertThat(tvrelease.title).isNull();
        }

        @Test
        void testTV9() throws Exception {
            Path file = Path.of("The.Boys.S04E05.Beware.the.jabberwock.my.son.1080p.web.dl.hevc.x265.rmteam.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.EPISODE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.fileName).isEqualTo(
                "The.Boys.S04E05.Beware.the.jabberwock.my.son.1080p.web.dl.hevc.x265.rmteam.mkv");
            assertThat(release.releaseGroup).isEqualTo("rmteam");
            assertThat(release.quality).isEqualTo("1080p web-dl x265");

            TvRelease tvrelease = (TvRelease) release;

            assertThat(tvrelease.season).isEqualTo(4);
            assertThat(tvrelease.episodeNumbers).containsExactly(5);
            assertThat(tvrelease.name).isEqualTo("The Boys");
            assertThat(tvrelease.title).isEqualTo("Beware the jabberwock my son");
        }
    }

    @Test
    void testReleaseParseExceptionMessage() {
        Path file = Path.of("exceptiontesting.mkv");

        assertThatExceptionOfType(ReleaseParseException.class).isThrownBy(() -> new ReleaseParser().parse(file))
            .withMessage("Unknown format, can't be parsed: " + file.toAbsolutePath());
    }

    @Nested
    class TestMovie {
        private final ReleaseParser releaseparser = new ReleaseParser();

        @Test
        void testMovie1() throws Exception {
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
        }

        @Test
        void testMovie2() throws Exception {
            Path file = Path.of("Back.to.the.Future.Part.21.1989.720p.BluRay.X264-AMIABLE.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.MOVIE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.fileName).isEqualTo("Back.to.the.Future.Part.21.1989.720p.BluRay.X264-AMIABLE.mkv");
            assertThat(release.releaseGroup).isEqualTo("AMIABLE");
            assertThat(release.quality).isEqualTo("720p bluray x264");

            MovieRelease movieRelease = (MovieRelease) release;

            assertThat(movieRelease.year).isEqualTo(1989);
            assertThat(movieRelease.name).isEqualTo("Back to the Future Part 21");
        }

        @Test
        void testMovie3() throws Exception {
            Path file = Path.of("The.Equalizer.2014.720p.BluRay.x264-SPARKS.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.MOVIE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.fileName).isEqualTo("The.Equalizer.2014.720p.BluRay.x264-SPARKS.mkv");
            assertThat(release.releaseGroup).isEqualTo("SPARKS");
            assertThat(release.quality).isEqualTo("720p bluray x264");

            MovieRelease movieRelease = (MovieRelease) release;

            assertThat(movieRelease.year).isEqualTo(2014);
            assertThat(movieRelease.name).isEqualTo("The Equalizer");
        }

        @Test
        void testMovie4() throws Exception {
            Path file = Path.of("The.Trip.to.Italy.2014.LIMITED.720p.BluRay.x264-GECKOS.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.MOVIE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.getFileName()).isEqualTo("The.Trip.to.Italy.2014.LIMITED.720p.BluRay.x264-GECKOS.mkv");
            assertThat(release.releaseGroup).isEqualTo("GECKOS");
            assertThat(release.quality).isEqualTo("720p bluray x264");

            MovieRelease movieRelease = (MovieRelease) release;

            assertThat(movieRelease.year).isEqualTo(2014);
            assertThat(movieRelease.name).isEqualTo("The Trip to Italy");
        }

        @Test
        void testMovie5() throws Exception {
            Path file = Path.of("Final.Destination.5.720p.Bluray.x264-TWiZTED.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.MOVIE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.getFileName()).isEqualTo("Final.Destination.5.720p.Bluray.x264-TWiZTED.mkv");
            assertThat(release.releaseGroup).isEqualTo("TWiZTED");
            assertThat(release.quality).isEqualTo("720p bluray x264");

            MovieRelease movieRelease = (MovieRelease) release;

            assertThat(movieRelease.year).isZero();
            assertThat(movieRelease.name).isEqualTo("Final Destination 5");
        }

        @Test
        void testMovie6() throws Exception {
            Path file = Path.of("Final.Destination.5.2011.720p.Bluray.x264-TWiZTED.mkv");
            Release release = releaseparser.parse(file);

            assertThat(release.videoType).isEqualTo(VideoType.MOVIE);
            assertThat(release.extension).isEqualTo("mkv");
            assertThat(release.getFileName()).isEqualTo("Final.Destination.5.2011.720p.Bluray.x264-TWiZTED.mkv");
            assertThat(release.releaseGroup).isEqualTo("TWiZTED");
            assertThat(release.quality).isEqualTo("720p bluray x264");

            MovieRelease movieRelease = (MovieRelease) release;

            assertThat(movieRelease.year).isEqualTo(2011);
            assertThat(movieRelease.name).isEqualTo("Final Destination 5");
        }
    }
}
