
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
package org.supremica.gui.editor;

import java.awt.Container;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import com.nwoods.jgo.*;
import java.util.Vector;

// Define an Action that knows about views and supports enabling/disabling
// depending on the current context.
public abstract class AppAction
	extends AbstractAction
{
	private static Vector myAllActions = new Vector();
	private Container myApp;

	public AppAction(String name, Container app)
	{
		super(name);

		init(app);
	}

	public AppAction(String name, Icon icon, Container app)
	{
		super(name, icon);

		init(app);
	}

	public AutomataEditor getApp()
	{
		return (AutomataEditor) myApp;
	}

	public AutomatonView getView()
	{
		return getApp().getCurrentView();
	}

	private final void init(Container app)
	{
		myApp = app;

		myAllActions.add(this);
	}

	public String toString()
	{
		return (String) getValue(NAME);
	}

	// by default each AppAction is disabled if there's no current view
	public boolean canAct()
	{
		return (getView() != null);
	}

	public void updateEnabled()
	{
		setEnabled(canAct());
	}

	public void free()
	{
		myAllActions.removeElement(this);

		myApp = null;
	}

	// keep track of all instances of AppAction
	public static void updateAllActions()
	{
		for (int i = 0; i < myAllActions.size(); i++)
		{
			AppAction act = (AppAction) myAllActions.elementAt(i);

			act.updateEnabled();
		}
	}

	public static Vector allActions()
	{
		return myAllActions;
	}
}
