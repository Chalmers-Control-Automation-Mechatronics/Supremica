//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IconLoader
//###########################################################################
//# $Id: IconLoader.java,v 1.6 2006-11-30 01:58:05 robi Exp $
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
        final String resourcename = "/icons/" + name + ".gif";
        final URL resource = cls.getResource(resourcename);
        
        return new ImageIcon(resource);
    }
    
    
    //#########################################################################
    //# Class Constants
    private static final String NAME_CONTROLLABLE = "waters/controllable";
    private static final String NAME_EVENT = "waters/event";
    private static final String NAME_PROPOSITION = "waters/proposition";
    private static final String NAME_FORBIDDEN = "ForbiddenState16";
    private static final String NAME_SIMPLEPARAM = "waters/rangeparam";
    private static final String NAME_UNCONTROLLABLE = "waters/uncontrollable";
    
    
    //#########################################################################
    //# Class Constants
    static final ImageIcon ICON_CONTROLLABLE = getIcon(NAME_CONTROLLABLE);
    static final ImageIcon ICON_EVENT = getIcon(NAME_EVENT);
    static final ImageIcon ICON_PROPOSITION = getIcon(NAME_PROPOSITION);
    static final ImageIcon ICON_FORBIDDEN = getIcon(NAME_FORBIDDEN);
    static final ImageIcon ICON_SIMPLEPARAM = getIcon(NAME_SIMPLEPARAM);
    static final ImageIcon ICON_UNCONTROLLABLE = getIcon(NAME_UNCONTROLLABLE);
    
}
