package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;

/**
 * simplify a project by removing and transforming the automata
 */
public class AutomataSimplifier
{
	private static Logger logger = LoggerFactory.createLogger(AutomataSimplifier.class);

	/**
	 * transform an automata to something with equal behaviour, but simpler structure
	 * @retruns a modified automata that behaves like the input.
	 */
	public static Automata simplify(Automata org)
		throws Exception
	{
		Automata ret = new Automata();

		/**
		 * Minimize with respect to language equivalence. 
		 * (Is this really clever? Nope. Use AutomatonMinimizer!)
		 */
		for (AutomatonIterator it = org.iterator(); it.hasNext(); )
		{
			Automaton a = it.nextAutomaton();
			AutomatonMinimizer am = new AutomatonMinimizer(a);
			Automaton mm = am.getMinimizedAutomaton(MinimizationOptions.getDefaultMinimizationOptions());

			mm.setName("simp(" + a.getName() + ")");
			ret.addAutomaton(mm);
		}

		// TODO:
		// minimizer doesnt remove unreachable states, we should do that ourselves!
		// first, remove automata that dont do anything
		Vector toRemove = new Vector();    // automata to remove
		HashSet toDisable = new HashSet();    // events to disable

		for (AutomatonIterator it = ret.iterator(); it.hasNext(); )
		{
			Automaton a = it.nextAutomaton();
			StateSet ss = a.getStateSet();

			if (ss.isEmpty())
			{
				// XXX: maybe we sould remove everything??? or at least every automata that shares its events?
				toRemove.add(a);
				logger.info("Removing NULL automaton " + a);
			}
			else
			{
				HashSet blocked = AlphabetAnalyzer.getBlockedEvents(a);

				if (ss.size() == 1)
				{
					toRemove.add(a);
					logger.info("Removing singe state automaton " + a);
				}

				toDisable.addAll(blocked);
			}
		}

		for (Iterator e = toRemove.iterator(); e.hasNext(); )
		{
			Automaton a = (Automaton) e.next();

			ret.removeAutomaton(a);
		}

		// let the user know...
		if (!toDisable.isEmpty())
		{
			for (Iterator e = toDisable.iterator(); e.hasNext(); )
			{
				LabeledEvent event = (LabeledEvent) e.next();

				logger.info("Removing blocked event " + event);
			}

			for (AutomatonIterator it = ret.iterator(); it.hasNext(); )
			{
				Automaton a = it.nextAutomaton();

				if (blockEvent(a, toDisable))
				{

					// TODO: minimize a, see if its singleton, and so an...
				}
			}
		}

		return ret;
	}

	private static boolean blockEvent(Automaton a, HashSet events)
	{
		boolean changed = false;
		Vector toRemove = new Vector();

		for (ArcIterator ai = a.arcIterator(); ai.hasNext(); )
		{
			Arc arc = ai.nextArc();

			if (events.contains(arc.getLabel()))
			{
				toRemove.add(arc);

				changed = true;
			}
		}

		for (Iterator e = toRemove.iterator(); e.hasNext(); )
		{
			Arc arc = (Arc) e.next();

			a.removeArc(arc);
		}

		return changed;
	}
}
