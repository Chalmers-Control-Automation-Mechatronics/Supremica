/******************** DiminishAction.java **********************/
//** Strictly EXPERIMENTAL. MF
// Only for teh Volvo Bus thesis work
// Goes through the automaton, forbids states that have both x.hi and x.lo events defined
// Then those states are removed, and the automaton is "restricted" to the remaining states
// (Is it that simple? We don't know, but let's try it.)
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import org.supremica.log.*;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.gui.Gui;
import org.supremica.gui.ActionMan;

public class DiminishAction
	extends AbstractAction
{
	private static Logger logger = LoggerFactory.createLogger(DiminishAction.class);
	private Automata newautomata = new Automata();
	private int statenbr = 0;
	
	public DiminishAction()
	{
		super("Diminish", null);
		putValue(SHORT_DESCRIPTION, "Diminish selected automata (experimental)");
	}

	public void actionPerformed(ActionEvent e)
	{
		logger.debug("DiminishAction::actionPerformed");
		
		Gui gui = ActionMan.getGui();
		Automata automata = gui.getSelectedAutomata();
		
		// Iterate over all automata
		for(Iterator autit = automata.iterator(); autit.hasNext(); )
		{
			Automaton automaton = new Automaton((Automaton)autit.next());	// make a copy
			if(diminish(automaton))	// If any states forbidden, remove them, but delicately
			{
				purgeDelicately(automaton);
				automaton.setComment("dim(" + automaton.getName() + ")");
				automaton.setName("");
				newautomata.addAutomaton(automaton);
			}
		}
		
		if(newautomata.nbrOfAutomata() > 0)
		{
			try
			{
				ActionMan.gui.addAutomata(newautomata);
				newautomata = new Automata();
			}
			catch(Exception ex)
			{
				logger.debug("DiminishAction::actionPerformed() -- ", ex);
				logger.debug(ex.getStackTrace());
			}

		}

		logger.debug("DiminishAction::actionPerformed done");
	}
	
	private boolean diminish(Automaton automaton)
	{
		boolean didit = false;
		
		for(StateIterator stit = automaton.stateIterator(); stit.hasNext(); )
		{
			State state = stit.nextState();
			didit |= diminish(state);
		}
		return didit;	// true if at least one state was forbidden
	}
	
	// We assume all labels look like <xxx>.hi or <xxx>.lo
	// If we for a state q find both .hi and .lo for the same <xxx>, then forbid q
	private boolean diminish(State state)
	{
		logger.debug("State" + "(" + ++statenbr + "): " + state.getName());
		
		for(ArcIterator ait = state.outgoingArcsIterator(); ait.hasNext(); )
		{
			Arc arc = ait.nextArc();
			LabeledEvent event = arc.getEvent();

			for(ArcIterator bit = state.outgoingArcsIterator(); bit.hasNext(); )
			{
				if(hiloMatch(event, bit.nextArc().getEvent()))
				{
					state.setForbidden(true);
					logger.debug("Forbidding state " + state.getName());
					return true;	// yes, we did something
				}
			}
		}
		logger.debug("Nada!");
		return false; // nothing done
	}
	
	// This can be much improved, ok for now
	private boolean hiloMatch(LabeledEvent ev1, LabeledEvent ev2)
	{
		String label1 = ev1.getLabel();
		String label2 = ev2.getLabel();
		// Find the index of the "."
		int idx1 = label1.indexOf(".");
		int idx2 = label2.indexOf(".");
		logger.debug("label1: " + label1 + " =? label2: " + label2);
		if(idx1 != idx2)	// if the lengths of the prefixes are not the same, we have no match
		{
			return false;
		}
		
		if(!label1.regionMatches(0, label2, 0, idx2))	// if the prefixes are not equal, we have no match
		{
			return false;
		}
		
		// Ok, so the prefixes are equal, do we have a hi and lo match?
		if(label1.substring(idx1+1).equals("hi") && label2.substring(idx2+1).equals("lo"))
		{
			return true;	// hi + lo match
		}
		if(label1.substring(idx1+1).equals("lo") && label2.substring(idx2+1).equals("hi"))
		{
			return true;	// lo + hi match
		}
		return false;
	}
	
	// Now, adjust the arcs
	// We make two passes, one for removing arcs between forbidden states
	// Next pass for adjusting the remaining arcs
	// Finally, we purge the forbidden stuff
	private void purgeDelicately(Automaton automaton)
	{
		logger.debug("Purging delicately...");
		
		automaton.beginTransaction();
		
		// Remove all arcs between two forbidden states (is it safe to do this?)
		for(Iterator arcit = automaton.safeArcIterator(); arcit.hasNext(); )
		{
			Arc arc = (Arc)arcit.next();
			if(arc.getFromState().isForbidden() && arc.getToState().isForbidden())
			{
				automaton.removeArc(arc);
			}
		}

		// Now, lets adjust the remaining arcs. 
		// If we reach a forbidden state, we need to take that arc further down the chain
		// Do we know that it is a chain? What about nondeterminism??
		for(Iterator arcit = automaton.safeArcIterator(); arcit.hasNext(); )
		{
			Arc arc = (Arc)arcit.next();
			State nextstate = arc.getToState();
			if(nextstate.isForbidden())
			{
				// We know that from the forbidden state, we reach a non-forbidden one
				// (otherwise this transition would have been removed above)
				// We don't know if there are multiple non-forbidden states reached, however!
				for(Iterator narcit = nextstate.outgoingArcsIterator(); narcit.hasNext(); )
				{
					Arc newarc = (Arc)narcit.next();
					logger.debug("Add arc<" + arc.getFromState().getName() + ", " + newarc.getToState().getName() + 
											", " + arc.getEvent().getLabel() + ">");
					automaton.addArc(new Arc(arc.getFromState(), newarc.getToState(), arc.getEvent()));
				}
				
				automaton.removeArc(arc);	// Remove it (must not remove before adjusting!!)
			}
		}
		
		// Remove the forbidden states
		AutomatonPurge automatonPurge = new AutomatonPurge(automaton);

		try
		{
			automatonPurge.execute();
		}
		catch (Exception ex)
		{
			logger.error("Exception in AutomataPurge. Automaton: " + automaton.getName(), ex);
			logger.debug(ex.getStackTrace());
		}
		/* Remove unconnected states
		for(Iterator stit = automaton.safeStateIterator(); stit.hasNext(); )
		{
			State state = stit.next();
			if(state.nbrOfIncomingArcs() == 0 && state.nbr
		}
		*/
		automaton.endTransaction();
		logger.debug("Delicate purging done");
	}
}
