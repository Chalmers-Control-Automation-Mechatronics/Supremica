
/********************* MakeDeterministicAction.java *****************/
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.standard.ObserverBuilder;
import org.supremica.gui.Gui;
import org.supremica.gui.ActionMan;

public class BuildObserverAction
    extends AbstractAction
{
    private static Logger logger = LoggerFactory.createLogger(BuildObserverAction.class);
    private Automata newautomata;
    
    public BuildObserverAction()
    {
        super("Build Observer", null);
        
        putValue(SHORT_DESCRIPTION, "Build observer automaton (experimental)");
        
        this.newautomata = new Automata();
    }
    
    public void actionPerformed(ActionEvent e)
    {
        logger.debug("BuildObserverAction::actionPerformed");
        
        Gui gui = ActionMan.getGui();
        Automata automata = gui.getSelectedAutomata();
        
        // Iterate over all automata
        for (Iterator autit = automata.iterator(); autit.hasNext(); )
        {
            Automaton automaton = (Automaton) autit.next();
            
            observerize(new Automaton(automaton));
        }
        
        if (newautomata.nbrOfAutomata() > 0)
        {
            try
            {
                ActionMan.gui.addAutomata(newautomata);
                
                newautomata = new Automata();
            }
            catch (Exception ex)
            {
                logger.debug("BuildObserverAction::actionPerformed() -- ", ex);
                logger.debug(ex.getStackTrace());
            }
        }
        
        logger.debug("BuildObserverAction::actionPerformed done");
    }
    
    // For each non-deterministic state, add "epsilon" transitions, then call Determinizer
    // Note that we add a single epsilon event, so initially the automaton becomes even more non-detm
    private void observerize(Automaton automaton)
    {
        
        // automaton.beginTransaction();
        boolean doit = false;
        
        doit = automaton.nbrOfUnobservableEvents() > 0;
        
        if (doit)
        {
            ObserverBuilder observerbuilder = new ObserverBuilder(automaton);
            
            observerbuilder.execute();
            
            Automaton newautomaton = observerbuilder.getNewAutomaton();
            
            newautomaton.setComment("obs(" + automaton.getName() + ")");
            newautomata.addAutomaton(newautomaton);
        }
        else
        {
            logger.info(automaton.getName() + " has all events observable");
        }
        
        // automaton.endTransaction();
    }
}
