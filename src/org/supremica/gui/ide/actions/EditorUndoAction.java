package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.IDE;
import java.util.List;
import javax.swing.KeyStroke;

public class EditorUndoAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorUndoAction(List<IDEAction> actionList)
	{
            super(actionList);

            setEditorActiveRequired(true);
            setAnalyzerActiveRequired(false);

            putValue(Action.NAME, "Undo");
            putValue(Action.SHORT_DESCRIPTION, "Undo the last command");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
            putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Undo16.gif")));
        }

        public void actionPerformed(ActionEvent e)
        {
            doAction();
        }

        public void doAction()
        {
            if (ide.getActiveDocumentContainer() != null)
            {
                if (ide.getActiveDocumentContainer().getEditorPanel().getUndoInterface().canUndo())
                {
                    ide.getActiveDocumentContainer().getEditorPanel().getUndoInterface().undo();
                }
            }
        }
}
