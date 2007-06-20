package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterJob;
import javax.print.StreamPrintServiceFactory;
import javax.swing.ImageIcon;
import org.supremica.gui.ide.IDE;

/**
 * A new action
 */
public class EditorSaveEncapsulatedPostscriptAction
    extends EditorSavePostscriptAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public EditorSaveEncapsulatedPostscriptAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        putValue(Action.NAME, "Save as EPS");
        putValue(Action.SHORT_DESCRIPTION, "Save currently viewed automaton as Encapsulated Postscript");
    }
  
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    /**
     * The code that is run when the action is invoked.
     */
    public void doAction()
    {
        try
        {
            ide.getActiveDocumentContainer().getEditorPanel().getActiveEditorWindowInterface().exportEncapsulatedPostscript();
        }
        catch (NullPointerException ex)
        {
            // This action should only be enabled when theres an editor panel open!
            ide.getIDE().info("Must have an editor panel open.");
        }
    }  
}
