package org.supremica.util.BDD;

import java.util.*;

public class State
{
	public State()
	{
		next = new Vector<State>();
		outgoing = new Vector<Arc>();
		incoming = new Vector<Arc>();
	}

	public int encodingIndex; // Help variable used for encoding

	/** for algorithmic use, to have a f:state->int map */
	/* package */
	String name, name_id;
	/* package */
	int id, code;
	/* package */
	int bdd_s; // The bdd representing the encoding of this state (current), e.g. v1 & !v2 for state 2
	int bdd_sp;// The bdd representing the encoding of this state (next), e.g. v1' & !v2' for state 2
	/* package */
	boolean i, m, f;
	/* package */
	Vector<State> next;    /* next state according to the transitions in ArcSet */
	/* package */
	Vector<Arc> outgoing;    /* outgoing arcs  */
	/* package */
	Vector<Arc> incoming;    /* incoming arcs  */

	/**
	 * sorry, we shouldn't make this public, but we cant have it package-access since
	 * we need to call it from another package :(
 * so this function is PUBLIC but should NOT be called by the user (unless from a xxxEncoding object)
	 */
	public void setCode(int c)
	{
		code = c;
	}

	public boolean isInitial()
	{
		return i;
	}

	public boolean isMarked()
	{
		return m;
	}

	public boolean isForbidden()
	{
		return f;
	}

	public Enumeration<Arc> in()
	{
		return incoming.elements();
	}

	public Enumeration<Arc> out()
	{
		return outgoing.elements();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append(name);
		sb.append(':');

		if (i)
		{
			sb.append('I');
		}

		if (m)
		{
			sb.append('M');
		}

		if (f)
		{
			sb.append('F');
		}

		return sb.toString();
	}
}
