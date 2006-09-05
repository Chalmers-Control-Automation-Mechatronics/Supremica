package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.ImageIcon;
import org.supremica.gui.ide.IDE;

public class EditorOptionsAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public EditorOptionsAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        
        putValue(Action.NAME, "Editor Options...");
        putValue(Action.SHORT_DESCRIPTION, "Editor Options");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Properties16.gif")));        
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        try
        {
            ide.getActiveModuleContainer().getEditorPanel().getActiveEditorWindowInterface().getControlledSurface().setOptionsVisible(true);
        }
        catch (NullPointerException ex)
        {
            // No panel opened?
            // You can't reach the options without an editor panel open (active)...
            ide.getIDE().info("You must open an editor panel (view a component) before you can access the editor options.");
        }
    }
}
