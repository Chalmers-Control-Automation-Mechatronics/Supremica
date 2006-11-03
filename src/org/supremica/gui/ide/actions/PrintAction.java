package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;

import java.util.List;

public class PrintAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public PrintAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        putValue(Action.NAME, "Print...");
        putValue(Action.SHORT_DESCRIPTION, "Print");
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        try
        {
            ide.getActiveModuleContainer().getEditorPanel().getActiveEditorWindowInterface().printFigure();
        }
        catch (NullPointerException ex)
        {
            // This action should only be enabled when theres an editor panel open!
            ide.getIDE().info("Must have an editor panel open.");
        }
    }
}
