//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2019 Knut Akesson, Martin Fabian, Robi Malik
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

import javax.swing.Icon;

import net.sourceforge.waters.gui.util.IconAndFontLoader;

import org.supremica.automata.LabeledEvent;


/**
 * An EventSubTree is a tree node with the event name as root
 * and the event properties as children.
 */

public class EventSubTree
    extends SupremicaTreeNode
{
    private static final long serialVersionUID = 1L;

    private int directLeafs = 0;

    public EventSubTree(final LabeledEvent event)
    {
        super(event);    // Note that this also caches the event for quick access

        final SupremicaTreeNode currControllableNode = new SupremicaTreeNode("controllable: " + event.isControllable());
        add(currControllableNode); directLeafs++;

        final SupremicaTreeNode currObservableNode = new SupremicaTreeNode("observable: " + event.isObservable());
        add(currObservableNode); directLeafs++;

		// Prioritized synch is not really junk, we use it.
        SupremicaTreeNode currPrioritizedNode = new SupremicaTreeNode("prioritized: " + event.isPrioritized());
        add(currPrioritizedNode); directLeafs++;
		
        // Hide junk...
        /*
        SupremicaTreeNode currOperatorIncreaseNode = new SupremicaTreeNode("operatorIncrease: " + event.isOperatorIncrease());
        add(currOperatorIncreaseNode); directLeafs++;

        SupremicaTreeNode currOperatorResetNode = new SupremicaTreeNode("operatorReset: " + event.isOperatorReset());
        add(currOperatorResetNode); directLeafs++;
         */
    }

    /**
     * Change this to reflect the correct number of children/properties/leaves
     * Could this be calculated from sizeof(LabeledEvent)? It should not.
     * This depends only on the above construction
     *
     * This method is used to quickly determine if an event occurs in all
     * automata or not, see showIntersection in EventsViewerPanel.
     */
    public int numDirectLeafs()
    {
        return directLeafs;
    }

    @Override
    public Icon getOpenIcon()
    {
      final LabeledEvent event = (LabeledEvent) userObject;
      if (event.isControllable()) {
        if (event.isObservable()) {
          return IconAndFontLoader.ICON_CONTROLLABLE_OBSERVABLE;
        } else {
          return IconAndFontLoader.ICON_CONTROLLABLE_UNOBSERVABLE;
        }
      } else {
        if (event.isObservable()) {
          return IconAndFontLoader.ICON_UNCONTROLLABLE_OBSERVABLE;
        } else {
          return IconAndFontLoader.ICON_UNCONTROLLABLE_UNOBSERVABLE;
        }
      }
    }

    @Override
    public Icon getClosedIcon()
    {
        //return null;
        return getOpenIcon();
    }

    @Override
    public String toString()
    {
        return ((LabeledEvent) userObject).getLabel();
    }

}
