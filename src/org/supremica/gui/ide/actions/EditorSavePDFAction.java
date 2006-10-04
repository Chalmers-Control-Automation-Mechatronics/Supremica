package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.ImageIcon;
import org.supremica.gui.ide.IDE;

public class EditorSavePDFAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public EditorSavePDFAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        
        putValue(Action.NAME, "Print As PDF...");
        putValue(Action.SHORT_DESCRIPTION, "Print As PDF");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Print16.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        try
        {
            ide.getActiveModuleContainer().getEditorPanel().getActiveEditorWindowInterface().exportPDF();
        }
        catch (NullPointerException ex)
        {
            // This action should only be enabled when theres an editor panel open!
            ide.getIDE().info("Must have an editor panel open.");
        }
    }
}

