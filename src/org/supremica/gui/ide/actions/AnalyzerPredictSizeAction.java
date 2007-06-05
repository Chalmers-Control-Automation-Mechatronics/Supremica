package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.GeneticAlgorithms;

/**
 * Action for predicting composition size.
 */
public class AnalyzerPredictSizeAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public AnalyzerPredictSizeAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Predict size");
        putValue(Action.SHORT_DESCRIPTION, "Predict composition size using genetically evolved algorithm, works best for two automata");
        //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    /**
     * Predict the size, calculate the exact size and the worst case size and present the result.
     */
    public void doAction()
    {
        Automata automata = ide.getSelectedAutomata();
        
        // Calculate predicted synchronisation size
        int prediction = (int) GeneticAlgorithms.predictSynchronizationSize(automata);
        // Calculate exact synchronisation size (for comparison)
        int exact = GeneticAlgorithms.calculateSynchronizationSize(automata);
        // Calculate worst case size
        int worst = 1;
        for (Automaton aut: automata)
            worst *= aut.nbrOfStates();

        ide.info("Predicted size: " + prediction + ", exact size: " + exact + ", worst case size: " + worst + ".");
    }
}
