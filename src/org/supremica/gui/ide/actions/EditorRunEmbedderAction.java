package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import net.sourceforge.waters.gui.springembedder.SpringEmbedder;
import net.sourceforge.waters.subject.module.GraphSubject;
import org.supremica.gui.ide.IDE;

/**
 * A new action
 */
public class EditorRunEmbedderAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public EditorRunEmbedderAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(false);

        putValue(Action.NAME, "Layout graph");
        putValue(Action.SHORT_DESCRIPTION, "Makes an automatic layout of the graph");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
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
        GraphSubject graph = ide.getIDE().getActiveEditorWindowInterface().getControlledSurface().getGraph();
        if (graph != null)
        {
			if (SpringEmbedder.isLayoutable(graph))
			{
				Thread t = new Thread(new SpringEmbedder(graph));
				t.start();
			}
			else
			{
				JOptionPane.showMessageDialog(ide.getFrame(), "Graph is not layoutable", "Alert", JOptionPane.ERROR_MESSAGE);
				return;
			}
        }
    }
}
