package org.lodder.subtools.multisubdownloader.gui.extra;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

import manifold.ext.props.rt.api.var;
import net.miginfocom.swing.MigLayout;

public class TitlePanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    @var JPanel panel;

    public static class TitlePanelBuilder {

        private final String title;
        private final boolean useGrid;
        private final boolean fillContents;
        private final BoxModelProperties margin;
        private final BoxModelProperties padding;
        private final LayoutManager panelLayout;
        private final String panelColumnConstraints;

        public TitlePanelBuilder(String title, boolean useGrid=false, boolean fillContents=true,
            BoxModelProperties margin=new BoxModelProperties(),
            BoxModelProperties padding=new BoxModelProperties(), LayoutManager panelLayout=null,
            String panelColumnConstraints="") {
            this.title = title;
            this.useGrid = useGrid;
            this.fillContents = fillContents;
            this.margin = margin;
            this.padding = padding;
            this.panelLayout = panelLayout;
            this.panelColumnConstraints = panelColumnConstraints;
        }

        public JPanel addTo(Container component) {
            return addTo(component, "");
        }

        public JPanel addTo(Container component, Object constraints) {
            LayoutManager panelLayoutNew = panelLayout != null ? panelLayout : new MigLayout(
                (fillContents ? "fill," : "") + (useGrid ? "" : "nogrid,") + padding.getInsets(),
                panelColumnConstraints);
            TitlePanel titlePanel =
                new TitlePanel(title, panelLayoutNew, margin);
            component.add(titlePanel, constraints);
            return titlePanel.panel;
        }
    }

    private TitlePanel(String title, LayoutManager panelLayout, BoxModelProperties margin) {
        super(new MigLayout("fillx, nogrid, " + margin.getInsets()));
        super.add(new JLabel(title));
        super.add(new JSeparator(), "growx, gapy 6, wrap");
        super.add(this.panel = new JPanel(panelLayout), "growx, span");
    }
}
