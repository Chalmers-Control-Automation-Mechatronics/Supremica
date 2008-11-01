//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IconLoader
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.net.URL;
import javax.swing.ImageIcon;


public class IconLoader
{

    //#########################################################################
    //# Static Class Methods
    private static final ImageIcon getIcon(final String name)
    {
        final Class cls = IconLoader.class;
        final String resourcename = "/icons/" + name + ".gif";
        final URL resource = cls.getResource(resourcename);
        
        return new ImageIcon(resource);
    }

    
    //#########################################################################
    //# Class Constants
    private static final String NAME_AUTOMATON = "waters/automaton16";
    private static final String NAME_CONTROLLABLE = "waters/controllable";
    private static final String NAME_EVENT = "waters/event";
    private static final String NAME_FORBIDDEN = "ForbiddenState16";
    private static final String NAME_INSTANCE = "waters/instance";
    private static final String NAME_PLANT = "waters/plant";
    private static final String NAME_PROPERTY = "waters/property";
    private static final String NAME_PROPOSITION = "waters/proposition";
    private static final String NAME_SIMPLEPARAM = "waters/rangeparam";
    private static final String NAME_SPEC = "waters/spec";
    private static final String NAME_SUPERVISOR = "waters/supervisor";
    private static final String NAME_UNCONTROLLABLE = "waters/uncontrollable";
    private static final String NAME_VARIABLE = "waters/variable";
    
    
    //#########################################################################
    //# Class Constants
    public static final ImageIcon ICON_AUTOMATON = getIcon(NAME_AUTOMATON);
    public static final ImageIcon ICON_BINDING = null;
    public static final ImageIcon ICON_CONTROLLABLE =
		getIcon(NAME_CONTROLLABLE);
    public static final ImageIcon ICON_EVENT = getIcon(NAME_EVENT);
    public static final ImageIcon ICON_FORBIDDEN = getIcon(NAME_FORBIDDEN);
    public static final ImageIcon ICON_FOREACH = null;
    public static final ImageIcon ICON_INSTANCE = getIcon(NAME_INSTANCE);
    public static final ImageIcon ICON_PLANT = getIcon(NAME_PLANT);
    public static final ImageIcon ICON_PROPERTY = getIcon(NAME_PROPERTY);
    public static final ImageIcon ICON_PROPOSITION = getIcon(NAME_PROPOSITION);
    public static final ImageIcon ICON_SIMPLEPARAM = getIcon(NAME_SIMPLEPARAM);
    public static final ImageIcon ICON_SPEC = getIcon(NAME_SPEC);
    public static final ImageIcon ICON_SUPERVISOR = getIcon(NAME_SUPERVISOR);
    public static final ImageIcon ICON_UNCONTROLLABLE =
		getIcon(NAME_UNCONTROLLABLE);
    public static final ImageIcon ICON_VARIABLE = getIcon(NAME_VARIABLE);
    
}
