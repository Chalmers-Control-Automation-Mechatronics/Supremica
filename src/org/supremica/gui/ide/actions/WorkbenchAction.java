package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.supremica.automata.Automata;
import org.supremica.gui.VisualProject;
import org.supremica.gui.ide.IDE;
import org.supremica.workbench.Workbench;
// Action related

/**
 * A new action
 */
public class WorkbenchAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public WorkbenchAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Workbench...");
        putValue(Action.SHORT_DESCRIPTION, "Launch workbench");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_W));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/supremica/workbench16.gif")));
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    @Override
    public void doAction()
    {
        try
        {
            final VisualProject visualProject = ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getVisualProject();
            final Automata selection = ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();

            if (selection.size() <= 0)
            {
                ide.getIDE().info("No automata selected.");
                return;
            }

            final Workbench workbench = new Workbench(visualProject, selection);
            workbench.setVisible(true);
        }
        catch (final Exception ex)
        {
            ide.getIDE().error("Error starting Workbench.");
        }
    }
}
