package org.lodder.subtools.sublibrary.data;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import manifold.ext.props.rt.api.val;
import org.apache.commons.text.similarity.LevenshteinDistance;

@AllArgsConstructor
@EqualsAndHashCode
public class ProviderId implements Serializable {

    @Serial
    private static final long serialVersionUID = -120703658294502220L;
    @val String name;
    @val String id;

    public static int calculateLevenshteinDistance(String name, String otherName) {
        return new LevenshteinDistance(100).apply(name.keepLettersOnly(), otherName.keepLettersOnly());
    }

    public int calculateLevenshteinDistance(String name) {
        return calculateLevenshteinDistance(name, this.name);
    }
}
