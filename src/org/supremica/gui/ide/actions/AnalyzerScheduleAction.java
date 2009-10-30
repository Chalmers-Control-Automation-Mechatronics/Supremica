/*
 * AnalyzerScheduleAction.java
 *
 * Created on den 20 juni 2007, 12:02
 *
 */

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Action;
import org.supremica.gui.ScheduleDialog;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
//import org.supremica.automata.Automata;
//import org.supremica.automata.Automaton;

/**
 *
 * @author Avenir Kobetski
 */
public class AnalyzerScheduleAction
        extends IDEAction
{
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(AnalyzerScheduleAction.class);
    private static final long serialVersionUID = 1L;
    
    /** Creates a new instance of AnalyzerScheduleAction */
    public AnalyzerScheduleAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Schedule...");
        putValue(Action.SHORT_DESCRIPTION, "Several scheduling methods to find a time-optimal working schedule for the automata");
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
        ScheduleDialog scheduleDialog = new ScheduleDialog(ide.getIDE());
        scheduleDialog.setVisible(true);

//        Automata automata = ide.getSelectedAutomata();
    }
}
