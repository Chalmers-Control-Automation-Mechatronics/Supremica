
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   IconLoader
//###########################################################################
//# $Id: IconLoader.java,v 1.2 2005-02-18 03:09:06 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.net.URL;
import javax.swing.ImageIcon;

class IconLoader
{

	//#########################################################################
	//# Static Class Methods
	private static final ImageIcon getIcon(final String name)
	{
		final Class cls = IconLoader.class;
		final String resourcename = "/icons/waters/" + name + ".gif";
		final URL resource = cls.getResource(resourcename);

		return new ImageIcon(resource);
	}

	//#########################################################################
	//# Class Constants
	private static final String NAME_CONTROLLABLE = "controllable";
	private static final String NAME_EVENT = "event";
	private static final String NAME_PROPOSITION = "proposition";
	private static final String NAME_UNCONTROLLABLE = "uncontrollable";

	//#########################################################################
	//# Class Constants
	static final ImageIcon ICON_CONTROLLABLE = getIcon(NAME_CONTROLLABLE);
	static final ImageIcon ICON_EVENT = getIcon(NAME_EVENT);
	static final ImageIcon ICON_PROPOSITION = getIcon(NAME_PROPOSITION);
	static final ImageIcon ICON_UNCONTROLLABLE = getIcon(NAME_UNCONTROLLABLE);
}
