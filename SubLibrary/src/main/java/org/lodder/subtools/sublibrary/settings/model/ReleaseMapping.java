package org.lodder.subtools.sublibrary.settings.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.UnaryOperator;

import io.gsonfire.annotations.PostDeserialize;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class ReleaseMapping implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final UnaryOperator<String> NAME_FORMATTER = n -> n.replaceAll("[^A-Za-z]", "");
    @val String name;
    @val String providerId;
    @val String providerName;
    @var transient String formattedName;

    public ReleaseMapping(String name, String providerId, String providerName) {
        this.name = name;
        this.providerId = providerId;
        this.providerName = providerName;
        postDeserializeLogic();
    }

    @PostDeserialize
    public void postDeserializeLogic() {
        formattedName = name.replaceAll("[^A-Za-z]", "");
    }

    public static String formatName(String name) {
        return NAME_FORMATTER.apply(name);
    }

    public boolean matches(String name) {
        String formattedName = formatName(name);
        return this.formattedName.contains(formattedName)
               || (formattedName.contains(this.formattedName) && this.formattedName.length() > 3);
    }

    public boolean exactMatch(String name) {
        return formattedName.equalsIgnoreCase(formatName(name));
    }
}
