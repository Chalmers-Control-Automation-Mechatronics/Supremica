package org.supremica.util.BDD;

import java.util.*;

public class Arc
{

	public Arc() {
		next = new Vector();
		prev = new Vector();
	}


	public String toString() {
		return state1 + "-(" + event + ")->" + state2;
	}

	/* string identifiers */
	String event, state1, state2;

	/* the actuall objects */
	State o_from, o_to;
	Event o_event;

	int id;

	/* int identifiers */
	int e_code, s1_code, s2_code;


	Vector next; /* arc proceeding this one */
	Vector prev; /* arc preceeding this one */

	public State fromState() { return o_from; }
	public State toState() { return o_to; }
}
