package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.supremica.automata.Automata;
import org.supremica.automata.Project;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.gui.AutomataMinimizationWorker;
import org.supremica.gui.MinimizationDialog;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


/**
 * A new action
 */
public class AnalyzerMinimizeAction
    extends IDEAction
{
    @SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerMinimizeAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Minimize");
        putValue(Action.SHORT_DESCRIPTION, "Minimize");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
//        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/supremica/minimise16.gif")));
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
        // Retrieve the selected automata and make a sanity check
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1))
        {
            return;
        }

        // Get the current options and allow the user to change them...
        final MinimizationOptions options = new MinimizationOptions();
        final MinimizationDialog dialog = new MinimizationDialog(ide.getFrame(), options, selectedAutomata);
        dialog.show();
        if (!options.getDialogOK())
        {
            return;
        }
        final Project currProject = ide.getActiveDocumentContainer().getAnalyzerPanel().getVisualProject();
        new AutomataMinimizationWorker(ide.getFrame(), selectedAutomata, currProject, options);
    }
}
