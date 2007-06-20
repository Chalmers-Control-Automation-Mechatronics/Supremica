package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.minimization.AutomataMinimizer;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * A new action
 */
public class AnalyzerExperimentAction
    extends IDEAction
{
    private static Logger logger = LoggerFactory.createLogger(AnalyzerExperimentAction.class);

     private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public AnalyzerExperimentAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(false);
        
        putValue(Action.NAME, "Experiment");
        putValue(Action.SHORT_DESCRIPTION, "Test of new stuff");
        //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Remove16.gif")));
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
        logger.info("Experiment started...");

        Automata automata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        
        // EXPERIMENT!
        {
            logger.info("Test: " + automata);
        }
        
        logger.info("Experiment finished.");
    }
}
