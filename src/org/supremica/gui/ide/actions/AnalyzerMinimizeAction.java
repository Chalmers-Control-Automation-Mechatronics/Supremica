package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.gui.MinimizationDialog;
import org.supremica.gui.AutomataMinimizationWorker;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;


/**
 * A new action
 */
public class AnalyzerMinimizeAction
    extends IDEAction
{
    private Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerMinimizeAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Minimize");
        putValue(Action.SHORT_DESCRIPTION, "Minimize");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
//        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/minimise16.gif")));
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
        // Retrieve the selected automata and make a sanity check
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1))
        {
            return;
        }

        // Get the current options and allow the user to change them...
        MinimizationOptions options = new MinimizationOptions();
        MinimizationDialog dialog = new MinimizationDialog(ide.getFrame(), options, selectedAutomata);
        dialog.show();
        if (!options.getDialogOK())
        {
            return;
        }
        Project currProject = ide.getActiveDocumentContainer().getAnalyzerPanel().getVisualProject();
        AutomataMinimizationWorker worker = new AutomataMinimizationWorker(ide.getFrame(), selectedAutomata, currProject, options);
    }
}
