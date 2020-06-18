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

package org.supremica.gui;

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.supremica.gui.useractions.CopyAction;
import org.supremica.gui.useractions.DeleteAction;
import org.supremica.gui.useractions.MoveAutomataAction;
import org.supremica.gui.useractions.OpenAction;
import org.supremica.gui.useractions.PreferencesAction;
import org.supremica.gui.useractions.SaveAction;
import org.supremica.gui.useractions.SaveAsAction;
import org.supremica.gui.useractions.StatusAction;
import org.supremica.gui.useractions.ViewAction;

public class MainToolBar
	extends JToolBar
{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static Supremica supremica;
	private static final OpenAction openAction = new OpenAction();
	private static final SaveAction saveAction = new SaveAction();
	private static final SaveAsAction saveAsAction = new SaveAsAction();
	private static final DeleteAction deleteAction = new DeleteAction();
	private static final CopyAction copyAction = new CopyAction();
	private static final ViewAction viewAction = new ViewAction();
	@SuppressWarnings("unused")
	private static final StatusAction statusAction = new StatusAction();

	private static final MoveAutomataAction moveAutomataToTopAction = new MoveAutomataAction(true, true);
	private static final MoveAutomataAction moveAutomataUpAction = new MoveAutomataAction(true, false);
	private static final MoveAutomataAction moveAutomataDownAction = new MoveAutomataAction(false, false);
	private static final MoveAutomataAction moveAutomataToBottomAction = new MoveAutomataAction(false, true);

	private static final PreferencesAction preferencesAction = new PreferencesAction();

	private static final Insets theInsets = new Insets(0, 0, 0, 0);

	public MainToolBar(final Supremica supremica)
	{
		MainToolBar.supremica = supremica;

		initToolBar();
		setRollover(true);
	}

	private void initToolBar()
	{
	    add(openAction);
	    add(saveAction);
	    add(saveAsAction);
	    addSeparator();

		add(deleteAction);
		add(copyAction);
		addSeparator();
		add(viewAction);

		// add(statusAction);
		addSeparator();
		add(moveAutomataToTopAction);
		add(moveAutomataUpAction);
		add(moveAutomataDownAction);
		add(moveAutomataToBottomAction);
		addSeparator();

		add(preferencesAction);
		addSeparator();


		add(ActionMan.helpAction);
	}

	/**
	 * Set the button margin
	 */
	@Override
  public JButton add(final Action theAction)
	{
		final JButton theButton = super.add(theAction);

		theButton.setMargin(theInsets);

		return theButton;
	}
}
