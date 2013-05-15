//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   OpenAction
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;


public class NewAction
    extends net.sourceforge.waters.gui.actions.IDEAction
{

    //#######################################################################
    //# Constructor
    NewAction(final IDE ide)
    {
        super(ide);
        putValue(Action.NAME, "New");
        putValue(Action.SHORT_DESCRIPTION, "New Module");
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
        putValue(Action.ACCELERATOR_KEY,
                 KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, IconLoader.ICON_TOOL_NEW);
    }


    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
        final IDE ide = getIDE();
        final DocumentContainerManager manager =
            ide.getDocumentContainerManager();
        manager.newModuleContainer();
    }


    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

}
