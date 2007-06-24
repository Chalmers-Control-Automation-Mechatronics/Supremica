//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   CloseAction
//###########################################################################
//# $Id: CloseAction.java,v 1.10 2007-06-24 18:40:06 robi Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

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
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
    }


    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
        final IDE ide = getIDE();
        final DocumentContainerManager manager =
            ide.getDocumentContainerManager();
        final DocumentContainer container = manager.getActiveContainer();
        manager.closeContainer(container);
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Observer
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

}
