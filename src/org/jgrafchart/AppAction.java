
/*
 *  Copyright © Northwoods Software Corporation, 2000-2001. All Rights
 *  Reserved.
 *
 *  Restricted Rights: Use, duplication, or disclosure by the U.S.
 *  Government is subject to restrictions as set forth in subparagraph
 *  (c) (1) (ii) of DFARS 252.227-7013, or in FAR 52.227-19, or in FAR
 *  52.227-14 Alt. III, as applicable.
 *
 */
package org.jgrafchart;

import java.awt.Container;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import com.nwoods.jgo.*;
import java.util.Vector;
import org.jgrafchart.Basic2GC;

public abstract class AppAction
	extends AbstractAction
{
	public Basic2GC getApp()
	{
		return (Basic2GC) myApp;
	}

	public GCView getView()
	{
		return getApp().getCurrentView();
	}

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

	private final void init(Container app)
	{
		myApp = app;

		myAllActions.add(this);
	}

	public String toString()
	{
		return (String) getValue(NAME);
	}

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

	protected Container myApp;

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

	private static Vector myAllActions = new Vector();
}
