package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.IDE;
import java.util.List;
import javax.swing.KeyStroke;

public class EditorRedoAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public EditorRedoAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        
        putValue(Action.NAME, "Redo");
        putValue(Action.SHORT_DESCRIPTION, "Redo the last command that was undone");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Redo16.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        if (ide.getActiveDocumentContainer() != null)
        {
            if (ide.getActiveDocumentContainer().getEditorPanel().getUndoInterface().canRedo())
            {
                ide.getActiveDocumentContainer().getEditorPanel().getUndoInterface().redo();
            }
        }
    }
}
