
package org.supremica.automata.algorithms;


import org.supremica.automata.*;
import org.supremica.util.BDD.*;

import java.util.*;

/**
 * ModularBDDLanguageInclusion, for verification with BDDs
 *
 */

public class ModularBDDLanguageInclusion extends BaseBDDLanguageInclusion {

	// ---[ interface to the base class ]-----------------------------------------------

    public ModularBDDLanguageInclusion(org.supremica.automata.Automata selected,
			       org.supremica.automata.Automata unselected,
			       AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		super(selected, unselected, hd);
    }


    public ModularBDDLanguageInclusion(org.supremica.automata.Automata theAutomata,
		AutomataSynchronizerHelper.HelperData hd)
		throws Exception
	{
		super(theAutomata, hd);
	}




	// ---[ the actual code ]---------------------------------------------------------


	/**
	 * Check if languake of K is included in the language of the rest of automata?
	 */
    protected boolean check(BDDAutomaton k, AutomataConfiguration ac)
    {
		/*
		// after this, events will hold the events in k that are considred.
		// ac should mark the plants/specs with connections to these events
		boolean [] k_events = k.getEventCareSet(controllaibilty_test);
		IndexedSet.intersection(k_events, considred_events, workset_events);

		ac.reset(k, considred_events, events, workset_events);
		if(Options.debug_on)	Options.out.println("Verifiying " + ac.toString() );

		// check if L(w1) \Sigma \cap L(w2) \subseteq L(w1)

		// start with w1 = { k } ...
		work1.empty();
		work1.add(k);

		// and w2 = \emptyset, that is L(w2) = \Sigma^* ??
		work2.empty();

		Supervisor sup = null;
		int states = -1;
		while(ac.addone(events, work1, work2, true) != null) {
			try {
				if(	sup != null) {
					sup.cleanup();
					sup = null;
					if(states != -1) ba.deref(states);
					states = -1;
				}
				sup = SupervisorFactory.createSupervisor(ba, work1, work2);

				if(Options.debug_on)
				{
					Options.out.println("Checking if " + work2.toString() + " subseteq " + work1.toString() );
				}

				states = sup.computeReachableLanguageDifference();
				boolean ret = (states == ba.getZero());

				if(ret) {
					sup.cleanup();
					ba.deref(states);
					return true;
				}
			} catch(Exception exx) {
				exx.printStackTrace();
				if(sup != null) sup.cleanup();
				if(states != -1) ba.deref(states);
				return false;
			}

		}

		// dump trace ...
		if(Options.trace_on && sup != null && states != -1)
		{
			sup.trace("Trace", states);
		}

		// cleanup
		if(sup != null) sup.cleanup();
		if(states != -1) ba.deref(states);



		// if no plants existed, then we cant fail! [becasue then L(P) = \Sigma^* ]
		return work2.isEmpty();
		*/

		return false; // TODO
	}

}
