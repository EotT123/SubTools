package org.lodder.subtools.sublibrary.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import manifold.ext.props.rt.api.val;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class ProviderId implements Serializable {

    @Serial private static final long serialVersionUID = 1L;
    @val String name;
    @val String id;
    @val boolean autoSelectable;

    public ProviderId(String name, String id, boolean autoSelectable=false) {
        this.name = name;
        this.id = id;
        this.autoSelectable = autoSelectable;
    }

    public static int calculateLevenshteinDistance(String name, String otherName) {
        return new LevenshteinDistance(100).apply(name.keepLettersOnly(), otherName.keepLettersOnly());
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
