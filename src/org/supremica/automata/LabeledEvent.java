/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata;

public class LabeledEvent
	implements Comparable
{
	// The id is the local identifier of an event, should be
	// different from any other id:s in the same automaton
	// private String id = "";
	// The label is what shows in the dot-figures, this is the
	// global identifier of an event, appearing in the alphabet
	private String label = "";

	private boolean controllable = true;
	private boolean prioritized = true;
	private boolean observable = true;
	private boolean operator = false;
	private boolean immediate = false;
	private boolean epsilon = false;
	private int expansionPriority = -1;
	private int synchIndex = -1;

	/**
	 * extra variable used to speed up algorithms.
	 * dont store your stuff here, it may be changed by other algos
	 */
//	public int extra1;

	// private Action theAction = null;
	public LabeledEvent() {}

	public LabeledEvent(String label)
	{
		this.label = label;

		// ** MF ** if I instantiate with only a label, I (probably) want the id to be the same (not?)
		// ** MF ** adding this cannot break any code, can it?
//		this.id = label;
	}

/*
	public LabeledEvent(String label, String id)
	{
		setLabel(label);
		setId(id);
	}
*/

	public LabeledEvent(LabeledEvent e)
	{
//		id = e.id;
		label = e.label;
		controllable = e.controllable;
		prioritized = e.prioritized;
		observable = e.observable;
		operator = e.operator;
		immediate = e.immediate;
		epsilon = e.epsilon;
		synchIndex = e.synchIndex;
	}

	public String toString()
	{
		return "'" + label + "'"; //  + " (" + id + ")";
	}

	/**
	 * Calls update in all execution listeners.
	 *
	 *@return  The id value
	 */

	/*
	 *  public void execute()
	 *  {
	 *      notifyExceutionListeners();
	 *  }
	 */

	// get/setId are now _only_ used by Alphabet
/*
	String getId() // default access, accessible within package, not outside (poor java mans friend declaration)
	{
		return id;
	}

	void setId(String id)
	{
		this.id = id;
	}
*/

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public boolean isControllable()
	{
		return controllable;
	}

	public void setControllable(boolean controllable)
	{
		this.controllable = controllable;
	}

	public boolean isObservable()
	{
		return observable;
	}

	public void setObservable(boolean observable)
	{
		this.observable = observable;
	}

	public boolean isOperator()
	{
		return operator;
	}

	public void setOperator(boolean operator)
	{
		this.operator = operator;
	}

	public boolean isImmediate()
	{
		return immediate;
	}

	public void setImmediate(boolean immediate)
	{
		this.immediate = immediate;
	}

	public boolean isPrioritized()
	{
		return prioritized;
	}

	public void setPrioritized(boolean prioritized)
	{
		this.prioritized = prioritized;
	}

	public boolean isEpsilon()
	{
		return epsilon;
	}

	public void setEpsilon(boolean b)
	{
		epsilon = b;
	}

	public void setExpansionPriority(int expansionPriority)
	{
		this.expansionPriority = expansionPriority;
	}

	public int getExpansionPriority()
	{
		return expansionPriority;
	}

	// This method must exist and work, since every map from event to something-else calls this one
	public boolean equals(Object obj)
	{
		return equals((LabeledEvent)obj);
		// throw new RuntimeException("LabeledEvent::equals(Object), not expected to be called!");

		// System.err.println("equalsObject");
		// return this.label.equals(((LabeledEvent) obj).label);
	}

	// NOTE -- this one's experimental, while migrating to the real problem domain
	// The Java people have messed it all up with inheriting equals(Object)
	public boolean equals(LabeledEvent event)
	{
		// return getId().equals(event.getId());
		return getLabel().equals(event.getLabel());
	}

	public boolean equals(String label)
	{
		// System.err.println("equalsString");
		return this.label.equals(label);
	}

	public boolean isEqual(LabeledEvent ev)
	{
		return this.label.equals(ev.label); // should also check priority & controllability?
	}

/*
	private boolean equalId(Object obj)
	{
		return this.id.equals(((LabeledEvent) obj).id);
	}

	private boolean equalId(String id)
	{
		return this.id.equals(id);
	}
*/

	public int hashCode()
	{
		return label.hashCode();
	}

	public int getSynchIndex()
	{
		return synchIndex;
	}

	void setSynchIndex(int synchIndex)
	{
		this.synchIndex = synchIndex;
	}

	public int compareTo(Object event)
	{
		// System.err.println("CompareTo");
		return label.compareTo(((LabeledEvent) event).label);
	}

	/*
	 *  public Listeners getExceutionListeners()
	 *  {
	 *  if (listeners == null)
	 *  {
	 *  listeners = new AutomatonListeners(this);
	 *  }
	 *  return listeners;
	 *  }
	 *
	 *  private void notifyExceutionListeners()
	 *  {
	 *  if (listeners != null)
	 *  {
	 *  listeners.notifyListeners();
	 *  }
	 *  }
	 */
}
