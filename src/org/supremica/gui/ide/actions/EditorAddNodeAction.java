//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorAddNodeAction
//###########################################################################
//# $Id: EditorAddNodeAction.java,v 1.15 2007-02-12 21:38:49 robi Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.ControlledToolbar;
import java.util.List;

public class EditorAddNodeAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public EditorAddNodeAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        
        putValue(Action.NAME, "Add Node");
        putValue(Action.SHORT_DESCRIPTION, "Add node");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/node16.gif")));
        putValue(Action.ACTION_COMMAND_KEY, ControlledToolbar.Tool.NODE.toString());
    }
    
    public void actionPerformed(ActionEvent e)
    {
        //System.out.println("Node");
        doAction();
    }
    
    public void doAction()
    {
        ide.setEditorMode(this);
        
//		System.err.println("Add Node is not implemented yet!");
    }
}
