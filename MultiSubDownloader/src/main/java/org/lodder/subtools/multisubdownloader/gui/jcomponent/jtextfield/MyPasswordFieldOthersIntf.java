package org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.lodder.subtools.sublibrary.util.BooleanConsumer;

public interface MyPasswordFieldOthersIntf {
    MyPasswordFieldOthersIntf withValueVerifier(Predicate<String> verifier);

    default MyPasswordFieldOthersIntf requireValue() {
        return requireValue(true);
    }

    MyPasswordFieldOthersIntf requireValue(boolean requireValue);

    MyPasswordFieldOthersIntf withValueChangedCallback(Consumer<String> valueChangedCalbackListener);

    MyPasswordFieldOthersIntf withValidityChangedCallback(BooleanConsumer... validityChangedCalbackListeners);

    MyPasswordField build();
}
