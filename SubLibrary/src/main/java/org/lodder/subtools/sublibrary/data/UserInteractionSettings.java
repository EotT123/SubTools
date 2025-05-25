package org.lodder.subtools.sublibrary.data;

import java.util.List;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.control.VideoPatterns;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Source;

public class UserInteractionSettings implements UserInteractionSettingsIntf {

    @override @val boolean optionsAlwaysConfirm;

    @override @val boolean optionsMinAutomaticSelection;

    @override @val int optionsMinAutomaticSelectionValue;

    @override @val boolean optionsDefaultSelection;

    @override @val List<VideoPatterns.Source> optionsDefaultSelectionQualityList;

    @override @val boolean optionsConfirmProviderMapping;

    public UserInteractionSettings(boolean optionsAlwaysConfirm, boolean optionsMinAutomaticSelection,
        int optionsMinAutomaticSelectionValue, boolean optionsDefaultSelection,
        List<Source> optionsDefaultSelectionQualityList, boolean optionsConfirmProviderMapping) {
        this.optionsAlwaysConfirm = optionsAlwaysConfirm;
        this.optionsMinAutomaticSelection = optionsMinAutomaticSelection;
        this.optionsMinAutomaticSelectionValue = optionsMinAutomaticSelectionValue;
        this.optionsDefaultSelection = optionsDefaultSelection;
        this.optionsDefaultSelectionQualityList = optionsDefaultSelectionQualityList;
        this.optionsConfirmProviderMapping = optionsConfirmProviderMapping;
    }
}
