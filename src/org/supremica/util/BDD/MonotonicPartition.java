
package org.supremica.util.BDD;

/** A disjunctive partition that grows monotonic */

public class MonotonicPartition {

    private int max_size, curr;
    private int [] delta;
    private JBDD manager;
    private int cube, cubep, s2sp,sp2s;

    public MonotonicPartition(BDDAutomata manager, int max_size) {
	this.manager  = manager;
	this.max_size = max_size;
	this.curr     = 0;

	this.cube     = manager.getStateCube();
	this.cubep    = manager.getStatepCube();
	this.s2sp     = manager.getPermuteS2Sp();
	this.sp2s     = manager.getPermuteSp2S();

	this.delta = new int[max_size];
    }

    public void cleanup() {
	for(int i = 0; i < curr; i++) manager.deref(delta[i]);
	curr = 0;
    }
    // --------------------------------------------------
    public int getNumberOfClusters() { return curr; }
    public int getCube() { return cube; }
    public int getS2Sp() { return s2sp; }
    public int getSp2s() { return sp2s; }
    // --------------------------------------------------
    /**
     * my simple/idiotic/stupid insertation algo
     *
     * worst-case of this greedy algo gives 2 times the optimal solution.
     * (and optimal is NP-complete). There is a proof somewhere in Cormens book ...
     */
    public void add(int delta_k) {
	for(int i = 0; i < curr; i++) {
	    int tmp = manager.or( delta[i], delta_k);
	    if( manager.nodeCount(tmp) < Options.max_partition_size) {
		// keep it
		manager.deref(delta[i]);
		delta[i] = tmp;
		return;
	    }
	    /* else */ manager.deref(tmp);
	}

	/* no good found */
	BDDAssert.internalCheck(curr < max_size, "Partition overflow");
	delta[curr] = delta_k;
	manager.ref(delta[curr]);
	curr++;
    }


    // -------------------------------------------------
    public void report() {
	SizeWatch.setOwner("DisjPartition");
	for(int i = 0; i < curr; i++)
	    SizeWatch.report(delta[i], "Cluster " + (i + 1));
    }

    // --------------------------------------------------

    /**
     * 1-step forward rechables.
     */
    public int image(int q_k) {

			int front = manager.getZero(); manager.ref(front);
		 	for(int i = 0; i < curr; i++) {
				int tmp = manager.relProd(delta[i], q_k, cube);
				front = manager.orTo(front, tmp);
				manager.deref(tmp);
			}

			int front_s = manager.replace( front, sp2s);
			manager.deref(front);
			return front_s;
    }

    /** 1-step backward reachables.<br>
     * Note: q_k must be in S' _not_ in S'!<br>
     * the returned BDD is also in S'
     *
     */
    public int preImage(int q_k) {

			int front = manager.getZero(); manager.ref(front);
			for(int i = 0; i < curr; i++) {
		    int tmp = manager.relProd(delta[i], q_k, cubep);
		    front = manager.orTo(front, tmp);
		    manager.deref(tmp);
			}

			int q_kplus1 = manager.replace( front, s2sp);
			manager.deref(front);
			return q_kplus1;
    }


	// ------------------------------------------------------------------

	 /** forward rechable fixedpoint */
	 // XXX: can be much faster by moving out the permutations!
	public int forward(GrowFrame gf, Limiter limit, int q_k) {
		int q_k_minus1;
		do {
			q_k_minus1 = q_k;
			int front = image(q_k_minus1);
			q_k = manager.orTo(q_k_minus1, front);
			manager.deref(front);
			if(gf != null)    gf.add( q_k );
		} while(q_k_minus1 != q_k && !limit.stopped());

		return q_k;
	}


	 /** backward rechable fixedpoint */
	 // XXX: can be much faster by moving out the permutations!
	public int backward(GrowFrame gf, Limiter limit, int q_k) {
		int q_k_minus1;
		do {
			q_k_minus1 = q_k;
			int front = preImage(q_k_minus1);
			q_k = manager.orTo(q_k_minus1, front);
			manager.deref(front);
			if(gf != null)    gf.add( q_k );
		} while(q_k_minus1 != q_k && !limit.stopped());


		return q_k;
	}
}
