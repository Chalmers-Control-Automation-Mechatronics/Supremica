/******************** ModifiedAstar.java **************************
 * MFs implementation of Tobbes modified Astar search algo
 */

package org.supremica.automata.algorithms.scheduling;

import java.util.Set;
import java.util.Vector;
import org.supremica.automata.*;

class Element // this is what populates the Open and Closed lists
{
	public int f;		// the sum of g(n) and h(n)
	public int g;		// the price to get here, g(n)
	public Vector Tv;	// vector of remaining processing time for each product in its current resource
	public Vector Qv;	// vector representing the current logical product state
	public Vector EB;	// used for limiting the node expansion
	public Element p;	// ptr to predecessor element

	protected boolean equals(Element element)	// see page 65
	{
		return f == element.f;
	}
	
	public boolean equals(Object obj)
	{
		return equals((Element)obj);
	}
}

class Estimator
{
	// Assumptions about automata:
	//	* plants are the resources
	//	* specs are the product routes
	private Automata automata;
	
	public Estimator(Automata automata)	// Here we should precalculate the estimates
	{
		this.automata = automata;
	}
	
	public Automata getAutomata()		// Return the stored automata
	{
		return automata;
	}
	
	public int h()			// For this composite state, return an estimate
	{
		return 0;	// 0 is always less than teh exact "estimate" h*, so should always give the optimal
	}
}

class TwoProductRelaxation 
	extends Estimator
{
	
	public TwoProductRelaxation(Automata automata)	// Here we should precalculate the estimates
	{
		super(automata);
		// calc the two-product relaxation estimates
	}
}

class OneMachineRelaxation
	extends Estimator
{
	public OneMachineRelaxation(Automata automata)	// Here we should precalculate the estimates
	{
		super(automata);
		// calc the one-machine relaxation estimates
	}
}

public class ModifiedAstar
{
	private Estimator estimator = null;
	
	// Open and Closed are sorted sets of Elements, sorted on smallest f.
	private Set open = null;
	private Set closed = null;
	
	public ModifiedAstar(Estimator estimator)
	{
		this.estimator = estimator;
	}
	
}