//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorEventsPanel
//###########################################################################
//# $Id: EditorEventsPanel.java,v 1.23 2007-06-11 15:07:51 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import javax.swing.JScrollPane;
import javax.swing.JList;

import net.sourceforge.waters.gui.EventDeclListView;
import net.sourceforge.waters.gui.EventEditorDialog;
import net.sourceforge.waters.gui.ModuleWindowInterface;


class EditorEventsPanel
    extends JScrollPane
{
   
    //######################################################################
    //# Constructor
    EditorEventsPanel(final ModuleWindowInterface root, final String name)
    {
		mRoot = root;
		mName = name;
        final JList list = new EventDeclListView(root);
        getViewport().add(list);
    }


    //######################################################################
    //# Simple Access
	public String getName()
	{
		return mName;
	}


    //######################################################################
    //# Actions
    public void addModuleEvent()
    {
        new EventEditorDialog(mRoot, false);
    }
    
    public void addComponentEvent()
    {
        new EventEditorDialog(mRoot, false);
    }


	//#########################################################################
	//# Data Members 
	private final ModuleWindowInterface mRoot;
	private final String mName;


    //######################################################################
    //# Static Class Constants
    private static final long serialVersionUID = 1L;
    
}
