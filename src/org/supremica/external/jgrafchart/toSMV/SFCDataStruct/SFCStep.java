package org.supremica.external.jgrafchart.toSMV.SFCDataStruct;
import java.util.*;

public class SFCStep
{
	List outgoingTransitions = new LinkedList();
	List incomingTransitions = new LinkedList();
	boolean active = false;
	String id;
	String actionText;
	boolean initialStep;
	List actions = null;

	public SFCStep(String id,String actionText,boolean isInitialStep)
	{
		this.id = id;
		this.actionText = actionText;
		this.initialStep = isInitialStep;
		finalizeActionsList();
	}

	public SFCStep(String id,String actionText)
	{
		this.id = id;
		this.actionText = actionText;
		this.initialStep = false;
		finalizeActionsList();
	}

	public void setActive()
	{
		active = true;
	}

	public boolean isActive()
	{
			return active;
	}

	public String getId()
	{
		return id;
	}
	public boolean isInitialStep()
	{
		return initialStep;
	}

	/* for listing sfcAction objects based on makeActionsList procedure */
	/*
	public List getActionsList()
	{
		if(actions == null)
			actions = new LinkedList();

		String anActionString = null;
		String actionType = null;
		String leftHandSide = null;
		String rightHandSide = null;

		List stringActions = makeActionsList();
		//System.out.println("String actions are :"+stringActions +" for Step :"+id);
		Iterator it = stringActions.iterator();
		while(it.hasNext())
		{
			anActionString = (String) it.next();
			actionType = anActionString.substring(0,1);
			int eqIndex = anActionString.indexOf("=");

			if(eqIndex != -1)
			{
				leftHandSide = anActionString.substring(1,eqIndex);
				rightHandSide = anActionString.substring(eqIndex+1);
			}
			else
			{
				leftHandSide = anActionString.substring(1);
				rightHandSide = null;

			}
			SFCAction sfcAction = new SFCAction(actionType,leftHandSide,rightHandSide);
			actions.add(sfcAction);
		}

		return actions;

	}
	*/
	public List getActionsList()
	{
		return actions;
	}

	/* for listing sfcAction objects based on makeActionsList procedure */
	public void finalizeActionsList()
	{
		if(actions == null)
			actions = new LinkedList();

		String anActionString = null;
		String actionType = null;
		String leftHandSide = null;
		String rightHandSide = null;

		List stringActions = makeActionsList();
		//System.out.println("String actions are :"+stringActions +" for Step :"+id);
		Iterator it = stringActions.iterator();
		while(it.hasNext())
		{
			anActionString = (String) it.next();
			actionType = anActionString.substring(0,1);
			int eqIndex = anActionString.indexOf("=");

			if(eqIndex != -1)
			{
				leftHandSide = anActionString.substring(1,eqIndex);
				rightHandSide = anActionString.substring(eqIndex+1);
			}
			else
			{
				leftHandSide = anActionString.substring(1);
				rightHandSide = null;

			}
			SFCAction sfcAction = new SFCAction(actionType,leftHandSide,rightHandSide);
			actions.add(sfcAction);
		}
	}

	/* for seperating ; separated actions*/
	private List makeActionsList()
	{
		List stringActions = new LinkedList();
		int start = 0;
		int index = 1;//just a dummy value
		String action = "";
		actionText = actionText.trim();
		while(start != actionText.length())
		{
			index = actionText.indexOf(';',start);
			action = actionText.substring(start,index);
			if(action.trim().length() != 0)
			{
				stringActions.add(action.trim());
			}
			start = index + 1;
		}
		return stringActions;
	}

	public void addAction(SFCAction anAction)
	{
		if(actions != null)
		{
			actions.add(anAction);
		}
		else
		{
			List newActions = new LinkedList();
			newActions.add(anAction);
			System.out.println("Action has been added after making new list:"+anAction);
			actions = newActions;
		}

	}


	public void setOutgoingTransitions(List outgoingTransitions)
	{
		this.outgoingTransitions = outgoingTransitions;
	}

	public void setIncomingTransitions(List incomingTransitions)
	{
		this.incomingTransitions = incomingTransitions;
	}

	public List getOutgoingTransitions()
	{
		return outgoingTransitions;
	}

	public List getIncomingTransitions()
	{
		return incomingTransitions;
	}

	public Iterator incomingTransIterator()
	{
		return incomingTransitions.iterator();
	}

	public Iterator outgoingTransIterator()
	{
		return outgoingTransitions.iterator();
	}

	public void addOutgoingTransition(SFCTransition aTrans)
	{
		outgoingTransitions.add(aTrans);
	}

	public void addIncomingTransition(SFCTransition aTrans)
	{
		incomingTransitions.add(aTrans);
	}

	public String toString()
	{
		return id;
	}
}
