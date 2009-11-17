package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import net.sourceforge.waters.gui.EditorWindowInterface;

/**
 * A new action
 */
public class EditorCopyAsWMFAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public EditorCopyAsWMFAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(true);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Copy as WMF");
        putValue(Action.SHORT_DESCRIPTION, "Copy as WMF");
        //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
    }

    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    @SuppressWarnings("deprecation")
    public void doAction()
    {
		EditorWindowInterface editorWindow = ide.getActiveEditorWindowInterface();
		if (editorWindow != null)
		{
			editorWindow.copyAsWMFToClipboard();
		}
    }
}
