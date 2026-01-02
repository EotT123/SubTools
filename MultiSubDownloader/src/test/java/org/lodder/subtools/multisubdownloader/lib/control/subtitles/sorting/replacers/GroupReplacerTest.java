package org.lodder.subtools.multisubdownloader.lib.control.subtitles.sorting.replacers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lodder.subtools.sublibrary.model.ReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;

class GroupReplacerTest {

    protected GroupReplacer replacer;

    @BeforeEach
    public void setUp() {
        replacer = new GroupReplacer();
    }

    @Test
    void test_it_replaces_the_keyword_group_to_a_releasename() {
        ReleaseWithoutPath release = mock(TvReleaseWithoutPath.class);
        when(release.releaseGroup).thenReturn("Acme");

        HashMap<String, Integer> definedWeights = new HashMap<>();
        definedWeights.put("%GROUP%", 5);

        replacer.replace(release, definedWeights);

        /* check if the weight there is only one weight */
        assertThat(definedWeights).hasSize(1);

        /* check if the weight is acme */
        assertThat(definedWeights).containsKey("acme");
    }

    @Test
    void testEmptyWeights() {
        ReleaseWithoutPath release = mock(TvReleaseWithoutPath.class);
        when(release.releaseGroup).thenReturn("Acme");

        HashMap<String, Integer> definedWeights = new HashMap<>();

        replacer.replace(release, definedWeights);

        /* check if the weight there is only one weight */
        assertThat(definedWeights).isEmpty();

        /* check if the weight is acme */
        assertThat(definedWeights).doesNotContainKey("acme");
    }
}
