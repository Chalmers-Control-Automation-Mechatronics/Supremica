package org.supremica.util.BDD;

import java.util.*;

public class State
{

	public State() {
		next = new Vector();
		outgoing = new Vector();
		incoming = new Vector();
	}


	String name, name_id;
	int id, code;
	int bdd_s, bdd_sp;
	boolean i, m, f;


	Vector next; /* next state according to the transitions in ArcSet */
	Vector outgoing; /* outgoing arcs  */
	Vector incoming; /* incoming arcs  */
}
