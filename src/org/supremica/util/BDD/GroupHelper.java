
package org.supremica.util.BDD;

import java.util.*;

/**
 * This class re-creates the automaton-list by merging two groups and using
 * the original ordering (according to BDDAutomaton.index)
 * (last Automata first, to allow bottom-up construction)
 */

public class GroupHelper {

    private int size;
    private int [] tpri, cube, cubep, twave;
    private Vector all;
    private BDDAutomaton [] sorted_list;

    /**
     * create a ordred automata list from two groups
     * TODO: use a priority queue !!
     */

    public GroupHelper(Group g1, Group g2) {

	// We actually need a priority vector, but who cares (N is small anyway) :)
	all = new Vector(g1.getSize() + g2.getSize());

	BDDAutomaton [] tmp = g1.getMembers();
	for(int i = 0; i < g1.getSize(); i++) all.addElement( tmp[i]);

	tmp = g2.getMembers();
	for(int i = 0; i < g2.getSize(); i++) all.addElement( tmp[i]);

	sort();
    }

    public GroupHelper(BDDAutomaton [] a) {

	// We actually need a priority vector, but who cares (N is small anyway) :)
	all = new Vector(a.length);
	for(int i = 0; i < a.length; i++) all.addElement( a[i]);

	sort();
    }

    private void sort() {
	size  = all.size();
	tpri  = new int[size];
	cube  = new int[size];
	cubep = new int[size];

	sorted_list = new BDDAutomaton[size];

	for(int i = 0; i < size; i++) {
	    BDDAutomaton a = popLargest();
	    sorted_list[i] = a;
	    tpri[i]  = a.getTpri();
	    cube[i]  = a.getCube();
	    cubep[i] = a.getCubep();
	}

	twave = null; // NOT computed unless needed!
    }

    // --------------------------------------------------------------------------
    public int getSize()
    {
	return size;
    }
    public int [] getTpri()
    {
	return tpri;
    }

    public int [] getCube()
    {
	return cube;
    }

    public int [] getCubep()
    {
	return cubep;
    }


    public BDDAutomaton [] getSortedList() {
	return sorted_list;
    }

    /** get ~T (disjunctive T). <br>
     * This will probably trigger a long sequence of computation first time.<br>
     * (internal: you dont need to cleanup after this function, its done elsewhere)
     */
    public int [] getTwave() {
	if(twave == null) {
	    twave = new int[size];
	    for(int i = 0; i < size; i++)
		twave[i] = sorted_list[i].getDependencySet().getTwave();
	}
	return twave;
    }
    // ---------------------------------------------------------------------------
    // a PriorityQueue, my kingdom for a PriorityQueue...
    private BDDAutomaton popLargest() {
	Enumeration e = all.elements();
	BDDAutomaton s = (BDDAutomaton) e.nextElement();

	while(e.hasMoreElements()) {
	    BDDAutomaton c = (BDDAutomaton) e.nextElement();
	    if(c.getIndex() > s.getIndex())
		s = c;
	}

	all.removeElement(s);
	return s;
    }


    /** the code to compute Q_M is moved here.<br>
     * this is to allow different models of marked state to be controlled from the same place
     */
    public static int getM(JBDD m, Group spec, Group plant) {
	// This one is tricky:
	// if spec is empty, then we cant assume that all events in P are marked
	// because then everything is marked (there is no spec, remember?)
	int m_all = spec.isEmpty() ? plant.getM() : spec.getM();
	m.ref(m_all);
	return m_all;
    }
}
