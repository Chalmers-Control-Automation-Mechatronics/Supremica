//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   CloseAction
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;


public class CloseAction
    extends net.sourceforge.waters.gui.actions.IDEAction
{

	//#######################################################################
    //# Constructor
    CloseAction(final IDE ide)
    {
        super(ide);
        setEnabled(false);
        putValue(Action.NAME, "Close");
        putValue(Action.SHORT_DESCRIPTION, "Close the current module");
    }


    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
        final IDE ide = getIDE();
        final DocumentContainerManager manager =
            ide.getDocumentContainerManager();
        manager.closeActiveContainer();
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Observer
    @Override
    public void update(final EditorChangedEvent event)
    {
        switch (event.getKind()) {
        case CONTAINER_SWITCH:
            final IDE ide = getIDE();
            final DocumentContainerManager manager =
                ide.getDocumentContainerManager();
            final DocumentContainer container = manager.getActiveContainer();
            final boolean enabled = container != null;
            setEnabled(enabled);
            break;
        default:
            break;
        }
    }


	//#######################################################################
    //# Class Constants
	private static final long serialVersionUID = 1L;

}
