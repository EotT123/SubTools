package org.lodder.subtools.multisubdownloader.lib.control.subtitles.sorting.replacer;

import static util.Utils.*;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.Release;

@NullMarked
public class GroupReplacer implements KeywordReplacer {

    /**
     * Replaces the special {@code %GROUP%} weight entry with the release group's name.
     * <p>
     * If the provided {@code weights} map contains a {@code %GROUP%} key, its value is
     * removed and reinserted using the lower-cased value of {@code release.releaseGroup}
     * as the new key. If either the placeholder entry does not exist or the release group
     * is {@code null}, the map remains unchanged.
     *
     * @param release the release whose group name is used as the replacement key
     * @param weights the weight mappings to update
     */
    @Override
    public void replace(Release release, Map<String, Integer> weights) {
        ifNotNullDo(weights.remove("%GROUP%"),
            weight -> ifNotNullDo(StringUtils.lowerCase(release.releaseGroup), group -> weights.put(group, weight)));
    }
}
