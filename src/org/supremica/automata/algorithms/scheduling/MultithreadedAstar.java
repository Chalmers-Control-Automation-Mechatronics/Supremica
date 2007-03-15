/******************** MultithreadedAstar.java **************************
 *
 * AKs modification of Tobbes modified A* scheduling algorithm  
 * allowing to take care of stochastic transitions, where each 
 * stochastic (uncontrollable) transition gives raise to a new search 
 * thread. 
 */

package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.automata.*;
import org.supremica.gui.ScheduleDialog;
import org.supremica.log.*;

public class MultithreadedAstar
	extends ModifiedAstar
{

	private static Logger logger = LoggerFactory.createLogger(MultithreadedAstar.class);
    
    public MultithreadedAstar(Automata theAutomata, boolean buildSchedule, ScheduleDialog gui)
		throws Exception
    {
        super(theAutomata, "1-product relax", buildSchedule, gui);
    }
    
    public MultithreadedAstar(Automata theAutomata, String heuristic, boolean buildSchedule, ScheduleDialog gui)
		throws Exception
    {
        super(theAutomata, heuristic, true, false, buildSchedule, gui);
    }
       
    public MultithreadedAstar(Automata theAutomata, boolean manualExpansion, boolean buildSchedule, ScheduleDialog gui)
		throws Exception
    {
        super(theAutomata, manualExpansion, buildSchedule, gui);
    }
}