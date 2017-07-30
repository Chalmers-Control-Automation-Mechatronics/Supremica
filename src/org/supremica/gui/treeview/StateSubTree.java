//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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

import javax.swing.*;
import org.supremica.automata.State;
import org.supremica.gui.Supremica;

public class StateSubTree
    extends SupremicaTreeNode
{
    private static final long serialVersionUID = 1L;

    private static ImageIcon ordinaryStateIcon = new ImageIcon(Supremica.class.getResource("/icons/State16.gif"));
    private static ImageIcon markedStateIcon = new ImageIcon(Supremica.class.getResource("/icons/MarkedState16.gif"));
    private static ImageIcon markedInitialStateIcon = new ImageIcon(Supremica.class.getResource("/icons/MarkedInitialState16.gif"));
    private static ImageIcon forbiddenInitialStateIcon = new ImageIcon(Supremica.class.getResource("/icons/ForbiddenInitialState16.gif"));
    private static ImageIcon initialStateIcon = new ImageIcon(Supremica.class.getResource("/icons/InitialState16.gif"));
    private static ImageIcon forbiddenStateIcon = new ImageIcon(Supremica.class.getResource("/icons/ForbiddenState16.gif"));

    public StateSubTree(State state)
    {
        super(state);    // Note that this also caches the state for quick access

        if (state.isInitial())
        {
            SupremicaTreeNode initial = new SupremicaTreeNode("initial");

            add(initial);
        }

        if (state.isForbidden())
        {
            SupremicaTreeNode forbidden = new SupremicaTreeNode("forbidden");

            add(forbidden);
        }

        if (state.isAccepting())
        {
            SupremicaTreeNode accepting = new SupremicaTreeNode("accepting");

            add(accepting);
        }
    }

    // This calculates the number of direct leaf children
    // That is, the number of initial/accepting/forbidden leaf nodes
    public int numDirectLeafs()
    {
        State state = (State) getUserObject();
        int directleafs = 0;

        if (state.isInitial())
        {
            ++directleafs;
        }

        if (state.isForbidden())
        {
            ++directleafs;
        }

        if (state.isAccepting())
        {
            ++directleafs;
        }

        return directleafs;
    }

    public Icon getOpenIcon()
    {
        //return null;
        if (((State) userObject).isInitial())
        {
            if (((State) userObject).isForbidden())
            {
                return forbiddenInitialStateIcon;
            }
            else if (((State) userObject).isAccepting())
            {
                return markedInitialStateIcon;
            }
            else
            {
                return initialStateIcon;
            }
        }
        else if (((State) userObject).isForbidden())
        {
            return forbiddenStateIcon;
        }
        else if (((State) userObject).isAccepting())
        {
            return markedStateIcon;
        }
        else
        {
            return ordinaryStateIcon;
        }
    }

    public Icon getClosedIcon()
    {
        //return null;
        return getOpenIcon();
    }

    public Icon getLeafIcon()
    {        
        //return null;
        return getOpenIcon();
    }

    public String toString()
    {
        return ((State) userObject).getName();
    }
}
