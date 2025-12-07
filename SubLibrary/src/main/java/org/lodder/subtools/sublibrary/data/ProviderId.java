package org.lodder.subtools.sublibrary.data;

import static org.lodder.subtools.sublibrary.xml.StringUtils.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import manifold.ext.props.rt.api.val;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ProviderId implements Serializable {

    @Serial private static final long serialVersionUID = 1L;
    @val String name;
    @val String id;

    public ProviderId(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public static int calculateLevenshteinDistance(String name, String otherName) {
        return new LevenshteinDistance(100).apply(normalize(name.keepLettersOnly()),
            normalize(otherName.keepLettersOnly()));
    }

    public int calculateLevenshteinDistance(String name) {
        return calculateLevenshteinDistance(name, this.name);
    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProviderId that = (ProviderId) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id);
    }

    @Override public int hashCode() {
        return Objects.hash(name, id);
    }
}
