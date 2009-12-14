package org.supremica.gui.ide.actions;

import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.ModularForbidder;
import org.supremica.automata.algorithms.ModularForbidderInput;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * A new action
 */
public class AnalyzerModularForbidderAction
    extends IDEAction
{
    private static Logger logger = LoggerFactory.createLogger(AnalyzerModularForbidderAction.class);

     private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public AnalyzerModularForbidderAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(false);
        
        putValue(Action.NAME, "ModularForbidder");
        putValue(Action.SHORT_DESCRIPTION, "Modular forbidding of undesirable sub-states");
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
        logger.info("ModularForbidder started...");

        Automata automata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
       
        // MODULAR FORBIDDER!
        try
        {
            ModularForbidderInput mfi = new ModularForbidderInput();
            
            mfi.createSubState();
            Iterator<Automaton> it = automata.getPlantAutomata().iterator();
            while(it.hasNext())
            {
                mfi.addLocalStateIn(it.next(), 2, 0);
            }
            
            mfi.createSubState();
            it = automata.iterator();
            while(it.hasNext())
            {
                mfi.addLocalStateIn(it.next(), 1, 1);
            }
            
            
            ModularForbidder mf = new ModularForbidder(mfi, ide.getIDE().getActiveProject());
            ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().addAutomata(mf.execute());
        }
        catch (Exception ex)
        {
            logger.debug("AnalyzerModularForbidderAction::actionPerformed() -- ", ex);
            logger.debug(ex.getStackTrace());
        }
   
        logger.info("ModularForbidder finished.");
    }
}
