//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
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
import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.automata.Automaton;

public class AutomatonSubTree
    extends SupremicaTreeNode
{
    private static final long serialVersionUID = 1L;

    private static ImageIcon plantIcon = IconLoader.ICON_PLANT;
    private static ImageIcon specificationIcon = IconLoader.ICON_SPEC;
    private static ImageIcon supervisorIcon = IconLoader.ICON_SUPERVISOR;

    public AutomatonSubTree(final Automaton automaton, final boolean includeAlphabet, final boolean includeStates)
    {
        //super(automaton.getName());
        super(automaton);

        // If we are to show either, but not both, the "Alphabet" and/or "State" nodes are unnecessary
        if (includeAlphabet && includeStates)
        {
            add(new AlphabetSubTree(automaton.getAlphabet()));
            add(new StateSetSubTree(automaton.getStateSet()));
        }
        else if (includeAlphabet)
        {
            AlphabetSubTree.buildSubTree(automaton.getAlphabet(), this);
        }
        else if (includeStates)
        {
            StateSetSubTree.buildSubTree(automaton.getStateSet(), this);
        }
    }

    @Override
    public Icon getOpenIcon()
    {
        //return null;
        final Automaton aut = (Automaton) userObject;

        if (aut.isPlant())
        {
            return plantIcon;
        }
        else if (aut.isSpecification())
        {
            return specificationIcon;
        }
        else if (aut.isSupervisor())
        {
            return supervisorIcon;
        }
        else
        {
            return null;
        }
    }

    @Override
    public Icon getClosedIcon()
    {
        // Same icon as for open
        return getOpenIcon();
    }

    @Override
    public String toString()
    {
        return ((Automaton) userObject).getName();
    }
}
