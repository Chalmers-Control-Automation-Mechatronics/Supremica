//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorAddEventAction
//###########################################################################
//# $Id: EditorAddComponentEventAction.java,v 1.6 2007-05-23 07:24:11 avenir Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.EditorWindowInterface;

import org.supremica.gui.ide.IDE;


public class EditorAddComponentEventAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public EditorAddComponentEventAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        
        putValue(Action.NAME, "New Component Event...");
        putValue(Action.SHORT_DESCRIPTION, "Add a new event to the component");
        putValue(Action.SMALL_ICON,
            new ImageIcon(IDE.class.getResource("/icons/waters/event16.gif")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
        putValue(Action.ACTION_COMMAND_KEY, KEY);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        final EditorWindowInterface handler =
            ide.getActiveEditorWindowInterface();
        if (handler != null)
        {
            handler.createEvent();
        }
    }

	public static final String KEY = "EVENT";

}
