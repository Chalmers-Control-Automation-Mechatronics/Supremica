//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IconLoader
//###########################################################################
//# $Id: IconLoader.java,v 1.3 2005-11-03 01:24:15 robi Exp $
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
	private static final String NAME_INTPARAM = "intparam";
	private static final String NAME_PROPOSITION = "proposition";
	private static final String NAME_RANGEPARAM = "rangeparam";
	private static final String NAME_UNCONTROLLABLE = "uncontrollable";


	//#########################################################################
	//# Class Constants
	static final ImageIcon ICON_CONTROLLABLE = getIcon(NAME_CONTROLLABLE);
	static final ImageIcon ICON_EVENT = getIcon(NAME_EVENT);
	static final ImageIcon ICON_INTPARAM = getIcon(NAME_INTPARAM);
	static final ImageIcon ICON_PROPOSITION = getIcon(NAME_PROPOSITION);
	static final ImageIcon ICON_RANGEPARAM = getIcon(NAME_RANGEPARAM);
	static final ImageIcon ICON_UNCONTROLLABLE = getIcon(NAME_UNCONTROLLABLE);

}
