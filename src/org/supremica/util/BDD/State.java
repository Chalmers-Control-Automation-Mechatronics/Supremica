package org.supremica.util.BDD;

import java.util.*;

public class State
{

	public State() {
		next = new Vector();
		outgoing = new Vector();
		incoming = new Vector();
	}


	public int extra1; /** for algorithmic use, to have a f:state->int map */
	/* package */ String name, name_id;
	/* package */ int id, code;
	/* package */ int bdd_s, bdd_sp;
	/* package */ boolean i, m, f;



	/* package */ Vector next; /* next state according to the transitions in ArcSet */
	/* package */ Vector outgoing; /* outgoing arcs  */
	/* package */ Vector incoming; /* incoming arcs  */


	/**
	 * sorry, we shouldn't make this public, but we cant have it package-access since
	 * we need to call it from another package :(
     * so this function is PUBLIC but should NOT be called by the user (unless from a xxxEncoding object)
	 */
	public void setCode(int c) { code = c; }

	public boolean isInitial() { return i; }
	public boolean isMarked() { return m; }
	public boolean isForbidden() { return f; }


	public Enumeration in() { return incoming.elements(); }
	public Enumeration out() { return outgoing.elements(); }

	public String  toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(name);
		sb.append(':');

		if(i) sb.append('I');
		if(m) sb.append('M');
		if(f) sb.append('F');

		return sb.toString();
	}

}
