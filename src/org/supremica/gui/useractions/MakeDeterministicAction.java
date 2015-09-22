//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.standard.Determinizer;
import org.supremica.gui.Gui;
import org.supremica.gui.ActionMan;

public class MakeDeterministicAction
    extends AbstractAction
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger =
        LoggerFactory.createLogger(MakeDeterministicAction.class);
    private static LabeledEvent epsilon = new LabeledEvent("");

    private Automata newautomata;
    
    public MakeDeterministicAction()
    {
        super("Make Deterministic", null);
        
        putValue(SHORT_DESCRIPTION, "Determinize selected automata (experimental)");
        epsilon.setUnobservable(true);
        
        this.newautomata = new Automata();
    }
    
    public void actionPerformed(ActionEvent e)
    {
        logger.debug("MakeDeterministicAction::actionPerformed");
        
        Gui gui = ActionMan.getGui();
        Automata automata = gui.getSelectedAutomata();
        
        // Iterate over all automata
        for (Iterator<?> autit = automata.iterator(); autit.hasNext(); )
        {
            // Determinize this automaton!
            Automaton automaton = (Automaton) autit.next();
            
            determinize(new Automaton(automaton));
        }
        
        // Did anything happen?
        if (newautomata.nbrOfAutomata() > 0)
        {
            try
            {
                // Add automaton to gui
                ActionMan.gui.addAutomata(newautomata);
                
                // Clear
                newautomata = new Automata();
            }
            catch (Exception ex)
            {
                logger.debug("MakeDeterministicAction::actionPerformed() -- ", ex);
                logger.debug(ex.getStackTrace());
            }
        }
        
        logger.debug("MakeDeterministicAction::actionPerformed done");
    }
    
    // For each non-deterministic state, add "epsilon" transitions, then call Determinizer
    // Note that we add a single epsilon event, so initially the automaton becomes even
    // more non-detm
    private void determinize(Automaton automaton)
    {
        // automaton.beginTransaction();
        boolean doit = false;
        
        // Find nondeterminisms and add epsilon events
        for (Iterator<State> stit = automaton.safeStateIterator();
        stit.hasNext(); )
        {
            State state = stit.next();
            
            doit |= epsilonize(state, automaton);
        }
        
        // If there is a need for determinization, determinize!
        if (doit)
        {
            Determinizer determinizer = new Determinizer(automaton);
            
            determinizer.execute();
            
            Automaton newautomaton = determinizer.getNewAutomaton();
            
            newautomaton.setComment("detm(" + automaton.getName() + ")");
            newautomata.addAutomaton(newautomaton);
        }
        else
        {
            logger.info(automaton.getName() + " is already deterministic");
        }
        
        // automaton.endTransaction();
    }
    
    // Note the brilliant ingenuity here! We manage this with but a single pass!
    // And the idea is that we need only to epsilonize n-1 transitions of n nondeterministic
    // ones.
    // The first we see we leave as it is, the others (with same label) we epsilonize
    // Brilliant!
    
    /**
     * This method examines state and if the state is nondeterministic by multiple outgoing
     * arcs with the same label, they are "epsilonized", so the nondeterminism is represented
     * by epsilon transitions instead.
     */
    @SuppressWarnings("deprecation")
	private boolean epsilonize(State state, Automaton automaton)
    {
        boolean found = false;
        HashMap<String, Arc> arcset = new HashMap<String, Arc>();
        
        // Initialize arc iterator
        Iterator<Arc> arcit = state.safeOutgoingArcsIterator();
        
                /* Why treat the first arc differently?
                if(arcit.hasNext())
                {
                                Arc arc = arcit.next();
                                if (arc.getEvent().isEpsilon())
                                {
                                                found = true;
                                }
                 
                                arcset.put(arc.getEvent().getLabel(), arc);     // put the first on the set
                }
                 */
        
        // Iterate over the remaining arcs and add epsilon events for nondeterministic arcs
        while (arcit.hasNext())
        {
            Arc arc = arcit.next();
            
            // Is this already an epsilon transition?
            if (arc.getEvent().isUnobservable())
            {
                found = true;
                
                continue;
            }
            
            // Have we seen this label before from this state? => non-detm
            if (arcset.containsKey(arc.getEvent().getLabel()))
            {
                epsilonize(arc, automaton);
                
                found = true;
            }
            else    // we've not already seen it - add it
            {
                arcset.put(arc.getEvent().getLabel(), arc);
            }
        }
        
        return found;
    }
    
    /**
     * For arc, insert epsilon transition to a unique intermediate state
     * The result is that
     *    fromState ---arcevent---> toState
     * is replaced by
     *    fromState ---epsilon---> newState ---arcevent---> toState
     */
    private void epsilonize(Arc arc, Automaton automaton)
    {
        State x = automaton.createUniqueState();    // This is the new intermediate state
        automaton.addState(x);
        Arc arc1 = new Arc(arc.getFromState(), x, epsilon);
        Arc arc2 = new Arc(x, arc.getToState(), arc.getEvent());
        
        automaton.addArc(arc1);
        automaton.addArc(arc2);
        automaton.removeArc(arc);
    }
}





