package org.supremica.external.jgrafchart.toSMV.SFCDataStruct;
import java.util.*;

public class SFCTransition
{
	List outgoingSteps = new LinkedList();
	List incomingSteps = new LinkedList();
	String id;
	String actionText;

	public SFCTransition(String id,String actionText)
	{
		this.id = id;
		this.actionText = actionText;
	}

	public String getId()
	{
		return id;
	}

	public void setOutgoingSteps(List outgoingSteps)
	{
		this.outgoingSteps = outgoingSteps;
	}

	public void setIncomingSteps(List incomingSteps)
	{
		this.incomingSteps = incomingSteps;
	}

	public List getOutgoingSteps()
	{
		return outgoingSteps;
	}

	public List getIncomingSteps()
	{
		return incomingSteps;
	}

	public Iterator incomingStepsIterator()
	{
		return incomingSteps.iterator();
	}

	public Iterator outgoingStepsIterator()
	{
		return outgoingSteps.iterator();
	}

	public void addOutgoingStep(SFCStep aStep)
	{
		if(!outgoingSteps.contains(aStep))
		{
			outgoingSteps.add(aStep);
			//System.out.println("&&&&&&&&&&&&&&&&&Added outgoing Step :"+aStep+" for Transition :"+id);

		}
	}

	public void addIncomingStep(SFCStep aStep)
	{
		if(!incomingSteps.contains(aStep))
			incomingSteps.add(aStep);
	}

	public String getTransCondition()
	{
		return actionText;
	}

	public void appendActionText(String newActionText)
	{
		actionText = "("+actionText+") "+newActionText;
	}

	public String toString()
	{
		return id;
	}

}