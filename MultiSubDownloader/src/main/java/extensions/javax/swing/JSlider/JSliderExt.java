package extensions.javax.swing.JSlider;

import javax.swing.*;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@UtilityClass
@Extension
public class JSliderExt {

    public static @Self JSlider withMinimum(@This JSlider slider, int minimum) {
        slider.setMinimum(minimum);
        return slider;
    }

    public static @Self JSlider withMaximum(@This JSlider slider, int maximum) {
        slider.setMaximum(maximum);
        return slider;
    }
}
