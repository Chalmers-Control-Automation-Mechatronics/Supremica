package org.supremica.util.BDD;

import java.util.*;
import org.supremica.util.BDD.heuristics.*;


/**
 *
 * When I finally code this, it should delay all _each_ automata, such that
 *  1. it starts with the currently added automata (Cluster c in the argument)
 *  2. it always adds the "most relavant" automata.
 *  3. (we could use MonotonicPartition for this :)
 *
 * XXX:
 * current implementation uses a stack to do this, which is not a very good approximation.
 * we need heuristics such as does in ES_* and AS_* for better reults
 */

public class  DelayedStarSmoothSupervisor extends DelayedSmoothSupervisor {

	Cluster [] cluster_stack;
	int cluster_tos; // top of stach

	DelayedInsertationHeuristic dih;

    public DelayedStarSmoothSupervisor(BDDAutomata manager, Group p, Group sp) {
		super(manager,p,sp);
		delaystar_init();
    }

    public DelayedStarSmoothSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
		super(manager,as);
		delaystar_init();
    }

	private void delaystar_init() {
		cluster_stack = new Cluster[disj_size];
		dih = DelayedInsertationHeuristicFactory.create(cluster_stack, disj_size);
	}

	public String toString() {
		return "delayed* smothing/" + Options.DSSI_HEURISTIC_NAMES[Options.dssi_heuristics];
	}


	// ---------------------------------------------------------------------------
	protected void init_delay() {
		cluster_tos = 0;
	}
	protected void cleanup_delay() {
	}
	// ---------------------------------------------------------------------------


	// compute the forward image
	protected int delay_forward(GrowFrame gf, Cluster c, int r) {

		MonotonicPartition delay_partition= new MonotonicPartition(manager, plant.getSize() + spec.getSize());

		cluster_stack[ cluster_tos++] = c;
		dih.init(cluster_tos);

		for(int i = 0; i < cluster_tos; i++) {
			delay_partition.add( dih.next().twave );
			r = delay_partition.forward(gf, r);
		}
		if(gf != null)    gf.mark("Released* " + c.toString());
		delay_partition.cleanup();
		return r;
	}

	protected int delay_backward(GrowFrame gf, Cluster c, int r) {
		MonotonicPartition delay_partition= new MonotonicPartition(manager, plant.getSize() + spec.getSize());

		cluster_stack[ cluster_tos++] = c;
		dih.init(cluster_tos);

		for(int i = 0; i < cluster_tos; i++) {
			delay_partition.add( dih.next().twave );
			r = delay_partition.backward(gf, r);
		}
		if(gf != null)    gf.mark("Released* " + c.toString());
		delay_partition.cleanup();
		return r;
	}
}