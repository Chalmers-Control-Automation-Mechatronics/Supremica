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

package org.supremica.gui;

import org.supremica.automata.IO.*;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.Arc;

public class AutomatonViewer
	extends DotViewer
	implements AutomatonListener
{
	private static final long serialVersionUID = 1L;
	private Automaton theAutomaton;

	protected AutomatonViewer(Automaton theAutomaton) // protected, use VisualProject::getAutomatonViewer instead
		throws Exception
	{
		this.theAutomaton = theAutomaton;

		super.setObjectName(theAutomaton.getName());
		theAutomaton.getListeners().addListener(this);
	}

	// Implementation of AutomatonListener interface
	public void stateAdded(Automaton aut, State q)
	{
		updated(aut, theAutomaton);
	}

	public void stateRemoved(Automaton aut, State q)
	{
		updated(aut, theAutomaton);
	}

	public void arcAdded(Automaton aut, Arc a)
	{
		updated(aut, theAutomaton);
	}

	public void arcRemoved(Automaton aut, Arc a)
	{
		updated(aut, theAutomaton);
	}

	public void attributeChanged(Automaton aut)
	{
		updated(aut, theAutomaton);
	}

	public void automatonRenamed(Automaton aut, String oldName)
	{
		setObjectName(aut.getName());
		updated(aut, theAutomaton);
	}

	// End of interface implementation
	public AutomataSerializer getSerializer()
	{
		AutomatonToDot serializer = new AutomatonToDot(theAutomaton);

		serializer.setLeftToRight(leftToRightCheckBox.isSelected());
		serializer.setWithLabels(withLabelsCheckBox.isSelected());
		serializer.setWithEventLabels(withEventLabelsCheckBox.isSelected());
		serializer.setWithCircles(withCirclesCheckBox.isSelected());
		serializer.setUseStateColors(useStateColorsCheckBox.isSelected());
		serializer.setUseArcColors(useArcColorsCheckBox.isSelected());

		return serializer;
	}

	public Automaton getAutomaton()
	{
		return theAutomaton;
	}
}
