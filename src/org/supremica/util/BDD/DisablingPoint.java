package org.supremica.util.BDD;


/**
 * This contains the possible combinations that must be disabled for a given Event.
 * These states can be given as a list or as a tree
 */

public class DisablingPoint
{

	private IncompleteStateList isl;
	private IncompleteStateTree ist;
	private String event;

	public DisablingPoint(IncompleteStateList isl, Event e)
	{
		this.event = e.name_id;
		this.isl   = isl;
		this.ist   = null;
	}

	public DisablingPoint(IncompleteStateTree ist, Event e)
	{
		this.event = e.name_id;
		this.ist   = ist;
		this.isl   = null;
	}

	public String getEvent()
	{
		return event;
	}

	// ----------------------------------------------------
	public IncompleteStateList getStateList()
	{
		return isl;
	}

	public boolean stateListEmpty()
	{
		if(isl == null) return true;
		return isl.empty();
	}

	public boolean isList()
	{
		return isl != null;
	}

	// ----------------------------------------------------
	public IncompleteStateTree getStateTree()
	{
		return ist;
	}

	public boolean stateTreeEmpty()
	{
		if(ist == null) return true;
		return ist.empty();
	}
	public boolean isTree()
	{
		return ist != null;
	}

}
