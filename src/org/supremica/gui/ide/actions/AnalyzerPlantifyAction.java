package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.minimization.MinimizationHelper;
import org.supremica.gui.ide.IDE;

/**
 * A new action
 */
public class AnalyzerPlantifyAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerPlantifyAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Plantify");
        putValue(Action.SHORT_DESCRIPTION, "Turns specifications and supervisors into plants");
        //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/plant16.gif")));
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
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        MinimizationHelper.plantify(selectedAutomata);
    }
}
