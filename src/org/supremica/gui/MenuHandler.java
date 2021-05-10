//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
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

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

// MF -- Small changes here to make the main popup menu usable from other tables
// MF -- Instead of hardwiring the menuhandler to a certain table, it takes a table param where appropriate
// MF -- I also made the menuhandler accessible from org.supremica.gui.Supremica
public class MenuHandler
{
	// private final JTable theTable;
	private final JPopupMenu oneAutomataMenu = new JPopupMenu();
	private final JPopupMenu twoAutomataMenu = new JPopupMenu();
	boolean oneAutomataMenuLastSep = false;
	boolean twoAutomataMenuLastSep = false;
	private final LinkedList<JMenuItem> zeroAutomataItems = new LinkedList<JMenuItem>();
	private final LinkedList<JMenuItem> oneAutomataItems = new LinkedList<JMenuItem>();
	private final LinkedList<JMenuItem> twoAutomataItems = new LinkedList<JMenuItem>();
	private final LinkedList<JMenuItem> disabledItems = new LinkedList<JMenuItem>();

	public static final int DISABLED = -1;

	public MenuHandler( /* JTable theTable */)
	{

		// this.theTable = theTable;
	}

	public void add(final JMenuItem theMenuItem, final int minNbrOfAutomata)
		throws Exception
	{
		if (minNbrOfAutomata == 0)
		{
			zeroAutomataItems.add(theMenuItem);
		}
		else if (minNbrOfAutomata == 1)
		{
			oneAutomataItems.add(theMenuItem);
		}
		else if (minNbrOfAutomata == 2)
		{
			twoAutomataItems.add(theMenuItem);
		}
		else if (minNbrOfAutomata == DISABLED)
		{
			disabledItems.add(theMenuItem);
		}
		else
		{
			// Use an Enum instead!!!
			throw new Exception("Error in MenuHandler. Illegal number.");
		}

		twoAutomataMenu.add(theMenuItem);
		twoAutomataMenuLastSep = false;

	}

	/**
	 * Add a sub-menu
	 *
	 */
	public void addSubMenu(final JMenu theMenu, final int minNbrOfAutomata)
	{
		if (minNbrOfAutomata == 0)
		{
			zeroAutomataItems.add(theMenu);
		}
		else if (minNbrOfAutomata == 1)
		{
			oneAutomataItems.add(theMenu);
		}
		else if (minNbrOfAutomata == 2)
		{
			twoAutomataItems.add(theMenu);
		}

		twoAutomataMenu.add(theMenu);

		twoAutomataMenuLastSep = false;
	}

	public void addSeparator()
	{
		if (!oneAutomataMenuLastSep)
		{
			oneAutomataMenu.addSeparator();

			oneAutomataMenuLastSep = true;
		}

		if (!twoAutomataMenuLastSep)
		{
			twoAutomataMenu.addSeparator();

			twoAutomataMenuLastSep = true;
		}
	}

	public JPopupMenu getDisabledPopupMenu(final int nbrOfAutomata)
	{
		setEnabled(twoAutomataItems, nbrOfAutomata >= 2);
		setEnabled(oneAutomataItems, nbrOfAutomata >= 1);
		setEnabled(zeroAutomataItems, nbrOfAutomata >= 0);
		setEnabled(disabledItems, false);

		return twoAutomataMenu;
	}

	public JPopupMenu getTrimmedPopupMenu(final JTable theTable)
	{
		final int nbrOfAutomata = theTable.getSelectedRowCount();

		if (nbrOfAutomata >= 2)
		{
			return twoAutomataMenu;
		}

		return oneAutomataMenu;
	}

	private void setEnabled(final LinkedList<JMenuItem> theList, final boolean enable)
	{
		final Iterator<JMenuItem> menuItemIt = theList.iterator();

		while (menuItemIt.hasNext())
		{
			final JMenuItem currItem = menuItemIt.next();

			currItem.setEnabled(enable);
		}
	}
}
