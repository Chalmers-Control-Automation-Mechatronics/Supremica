/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.gui;

import java.util.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;

// MF -- Small changes here to make the main popup menu usefable from other tables
// MF -- Instead of hardwiring the menuhandler to a certain table, it takes a table param where appropriate
// MF -- I also made the menuhandler accessible from org.supremica.gui.Supremica
public class MenuHandler
{

	// private final JTable theTable;
	private final JPopupMenu oneAutomataMenu = new VPopupMenu();
	private final JPopupMenu twoAutomataMenu = new VPopupMenu();
	boolean oneAutomataMenuLastSep = false;
	boolean twoAutomataMenuLastSep = false;
	private final LinkedList zeroAutomataItems = new LinkedList();
	private final LinkedList oneAutomataItems = new LinkedList();
	private final LinkedList twoAutomataItems = new LinkedList();

	public MenuHandler( /* JTable theTable */)
	{

		// this.theTable = theTable;
	}

	public void add(JMenuItem theMenuItem, int minNbrOfAutomata)
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

		// System.err.println("new item");
		// if (minNbrOfAutomata <= 2)
		// {
		// System.err.println("Added to two menu");
		twoAutomataMenu.add(theMenuItem);

		twoAutomataMenuLastSep = false;

		// }

		/*
		 * if (minNbrOfAutomata <= 1)
		 * {
		 *       System.err.println("Added to one menu");
		 *       oneAutomataMenu.add(theMenuItem);
		 *       oneAutomataMenuLastSep = false;
		 * }
		 */
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

	// Shoudl not knwo anything about table, doesnät even need to know about gui
	public JPopupMenu getDisabledPopupMenu(int nbrOfAutomata /* JTable theTable */)
	{

		// int nbrOfAutomata = theTable.getSelectedRowCount();
		setEnabled(twoAutomataItems, nbrOfAutomata >= 2);
		setEnabled(oneAutomataItems, nbrOfAutomata >= 1);
		setEnabled(zeroAutomataItems, nbrOfAutomata >= 0);

		return twoAutomataMenu;
	}

	public JPopupMenu getTrimmedPopupMenu(JTable theTable)
	{
		int nbrOfAutomata = theTable.getSelectedRowCount();

		if (nbrOfAutomata >= 2)
		{
			return twoAutomataMenu;
		}

		return oneAutomataMenu;
	}

	private void setEnabled(LinkedList theList, boolean enable)
	{
		Iterator menuItemIt = theList.iterator();

		while (menuItemIt.hasNext())
		{
			JMenuItem currItem = (JMenuItem) menuItemIt.next();

			currItem.setEnabled(enable);
		}
	}
}
