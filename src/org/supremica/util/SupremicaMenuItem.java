
/****************** SupremicaMenuItem.java **********************/

// Necessary to fix a major glitch (bug?) in Swing
// Without this, JMenuItem shows the icon. We don't want that, do we...
// Use this instead of JMenuItem when adding Action objects to menus
package org.supremica.util;

import javax.swing.*;

public class SupremicaMenuItem
	extends JMenuItem
{
	public SupremicaMenuItem(Action action)
	{
		super(action);
	}

	protected void configurePropertiesFromAction(Action action)
	{
		super.configurePropertiesFromAction(action);
		setIcon(null);    // wipe the icon in the menu
	}
}
