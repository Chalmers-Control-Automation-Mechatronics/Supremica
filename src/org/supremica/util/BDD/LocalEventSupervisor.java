


package org.supremica.util.BDD;

import java.util.*;

public class LocalEventSupervisor
    extends ConjSupervisor
{

	private AutomataCoverManager covermanager;

    /** Constructor, passes to the base-class */
    public LocalEventSupervisor(BDDAutomata manager, Group plant, Group spec) {
		super(manager, plant, spec);
		local_init();
    }
    /** Constructor, passes to the base-class */
    public LocalEventSupervisor(BDDAutomata manager, BDDAutomaton[] automata) {
		super(manager, automata);
		local_init();
    }

	private void local_init() {
		covermanager = new AutomataCoverManager(manager, gh.getSortedList());
	}
    // -----------------------------------------------------------------
    public void cleanup() {
		covermanager.cleanup();
		super.cleanup();
	}

    // -----------------------------------------------------------------
    // TODO


	// public int getReachables(int initial_states)
	// public int getReachables(boolean [] events)
	// public int getReachables(boolean [] events, int intial_states)

    // protected void computeReachables()
    // protected void computeCoReachables()




	protected void computeReachables()
	{
		int i_all = manager.and(plant.getI(), spec.getI());
		bdd_reachables = internal_computeReachablesLocal(i_all);
		has_reachables = true;
		manager.deref(i_all);
	}

	private int internal_computeReachablesLocal(int i_all) {
		return covermanager.forward_reachability(i_all);

	}
}

