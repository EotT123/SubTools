package org.lodder.subtools.multisubdownloader.gui.extra;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.gui.extra.table.CustomTable;

@NullMarked
public class PopupListener extends MouseAdapter {

    private final JPopupMenu popupMenu;

    public PopupListener(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        showPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showPopup(e);
    }

    private synchronized void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()
            && e.getComponent() instanceof CustomTable customTable
            && customTable.getModel().getRowCount() > 0) {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
