package org.lodder.subtools.sublibrary.data;

import java.util.List;

import manifold.ext.props.rt.api.Static;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Source;

@NullMarked
public interface UserInteractionSettingsIntf {

    @Static @val boolean optionsAlwaysConfirm;

    @Static @val boolean optionsMinAutomaticSelection;

    @Static @val int optionsMinAutomaticSelectionValue;

    @Static @val boolean optionsDefaultSelection;

    @Static @val List<Source> optionsDefaultSelectionQualityList;

    @Static @val boolean optionsConfirmProviderMapping;
}
