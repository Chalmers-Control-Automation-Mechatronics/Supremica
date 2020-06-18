//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.treeview;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class SupremicaTreeCellRenderer
    extends DefaultTreeCellRenderer
{
    private static final long serialVersionUID = 1L;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,    //
        boolean expanded,    // true => openIcon, else closedIcon
        boolean leaf,    // true => leafIcon, cannot be open
        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        SupremicaTreeNode node = (SupremicaTreeNode) value;

        if (node.isEnabled() == false)
        {
            Icon icon = node.getDisabledIcon();

            if (icon == null)
            {
                icon = getDisabledIcon();
            }

            setIcon(icon);

            return this;
        }

        if (leaf)
        {
            Icon icon = node.getLeafIcon();

            if (icon == null)
            {
                icon = getDefaultLeafIcon();
            }

            setIcon(icon);

            return this;
        }
        else if (expanded)    // cannot be leaf
        {
            Icon icon = node.getOpenIcon();

            if (icon == null)
            {
                icon = getDefaultOpenIcon();
            }

            setIcon(icon);

            return this;
        }
        else    // must be non-expanded non-leaf
        {
            Icon icon = node.getClosedIcon();

            if (icon == null)
            {
                icon = getDefaultClosedIcon();
            }

            setIcon(icon);

            return this;
        }
    }
}
