
package org.supremica.util.BDD;

import java.util.*;

/**
 * This class re-creates the automaton-list by merging two groups and using
 * the oroginal ordering (according to BDDAutomaton.index) 
 *
 */

public class GroupHelper {
    
    private int size;
    private int [] tpri, cube, cubep;
    private Vector all;

    /**
     * create a ordred automata list from two groups
     * TODO: use a priority queue !!
     */

    public GroupHelper(Group g1, Group g2) {

	// We actually need a priority vector, but who cares (N is small anyway) :)
	all = new Vector(g1.getSize() + g2.getSize() + 1);	
	
	BDDAutomaton [] tmp = g1.getMembers();
	for(int i = 0; i < g1.getSize(); i++) all.addElement( tmp[i]);

	tmp = g2.getMembers();
	for(int i = 0; i < g2.getSize(); i++) all.addElement( tmp[i]);

	
	size  = all.size();
	tpri  = new int[size];
	cube  = new int[size];
	cubep = new int[size];

	for(int i = 0; i < size; i++) {
	    BDDAutomaton a = popSmallet();
	    tpri[i]  = a.getTpri();
	    cube[i]  = a.getCube();
	    cubep[i] = a.getCubep();
	}
    }

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

    // a PriorityQueue, my kingdom for a PriorityQueue...
    private BDDAutomaton popSmallet() {
	Enumeration e = all.elements();	
	BDDAutomaton s = (BDDAutomaton) e.nextElement();

	while(e.hasMoreElements()) {
	    BDDAutomaton c = (BDDAutomaton) e.nextElement();
	    if(c.getIndex() < s.getIndex())
		s = c;
	}

	all.removeElement(s);
	return s;
    }
}
