package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.supremica.gui.ide.IDE;

public class EditorPrintAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public EditorPrintAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        
        putValue(Action.NAME, "Print...");
        putValue(Action.SHORT_DESCRIPTION, "Print");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
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
            ide.getActiveModuleContainer().getEditorPanel().getActiveEditorWindowInterface().printFigure();
        }
        catch (NullPointerException ex)
        {
            // This action should only be enabled when there's an editor panel open!
            ide.getIDE().info("Must have an editor panel open.");
        }        
    }

    /**
     * Is enabled if it is possible to get a hold of an active EditorWindowInterface.
     */
    /* Should use setEnabled when the property becomes true...?
    public boolean isEnabled()
    {
        try
        {
            return (null != ide.getActiveModuleContainer().getEditorPanel().getActiveEditorWindowInterface());
        }
        catch (NullPointerException ex)
        {
            return false;
        }
    }
     */
}
