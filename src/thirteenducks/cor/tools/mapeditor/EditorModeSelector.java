/*
 *  Copyright 2008, 2009, 2010:
 *   Tobias Fleig (tfg[AT]online[DOT]de),
 *   Michael Haas (mekhar[AT]gmx[DOT]de),
 *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
 *
 *  - All rights reserved -
 *
 *
 *  This file is part of Centuries of Rage.
 *
 *  Centuries of Rage is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Centuries of Rage is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package thirteenducks.cor.tools.mapeditor;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class EditorModeSelector extends JLabel implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        //Get the selected index. (The index param isn't
        //always valid, so just use the value.)
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        ImageIcon icon = null;
        try {
            if (value.toString().equals("MAP")) {
                icon = new ImageIcon(ImageIO.read(new File("img/notinlist/editor/mode_map.png")));
            } else if (value.toString().equals("COL")) {
                icon = new ImageIcon(ImageIO.read(new File("img/notinlist/editor/mode_col.png")));
            } else if (value.toString().equals("UNIT")) {
                icon = new ImageIcon(ImageIO.read(new File("img/notinlist/editor/mode_unit.png")));
            }
            if (icon != null) {
                setIcon(icon);
            }
        } catch (IOException ex) {
            System.out.println("[RLE][ERROR] Can't load Editor-mode-images. RLE may not work correctly.");
        }

        return this;
    }

    public EditorModeSelector() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
    }
}
