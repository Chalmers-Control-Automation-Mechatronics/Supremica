//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui;

/*
 * ColorRenderer.java (compiles with releases 1.2, 1.3, and 1.4) is used by
 * TableDialogEditDemo.java.
 */
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public class ColorRenderer
	extends JLabel
	implements TableCellRenderer
{
    private static final long serialVersionUID = 1L;

    Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;

	public ColorRenderer(boolean isBordered)
	{
		this.isBordered = isBordered;

		setOpaque(true);    //MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column)
	{
		Color newColor = (Color) color;

		setBackground(newColor);

		if (isBordered)
		{
			if (isSelected)
			{
				if (selectedBorder == null)
				{
					selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
				}

				setBorder(selectedBorder);
			}
			else
			{
				if (unselectedBorder == null)
				{
					unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
				}

				setBorder(unselectedBorder);
			}
		}

		setToolTipText("RGB value: " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue());

		return this;
	}
}
