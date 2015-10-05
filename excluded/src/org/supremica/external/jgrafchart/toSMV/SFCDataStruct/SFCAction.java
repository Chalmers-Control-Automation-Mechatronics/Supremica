package org.supremica.external.jgrafchart.toSMV.SFCDataStruct;

public class SFCAction
{
	String actionType;
	String leftHandSide;
	String rightHandSide;

	public SFCAction(String actionType, String leftHandSide, String rightHandSide)
	{
		this.actionType = actionType.trim();
		this.leftHandSide = leftHandSide.trim();

		if (rightHandSide != null)
		{
			this.rightHandSide = rightHandSide.trim();
		}
		else
		{
			this.rightHandSide = rightHandSide;
		}
	}

	public void setActionType(String newActionType)
	{
		actionType = newActionType;
	}

	public void setLeftHandSide(String newLeftHandSide)
	{
		leftHandSide = newLeftHandSide;
	}

	public void setRightHandSide(String newRightHandSide)
	{
		rightHandSide = newRightHandSide;
	}

	public String getActionType()
	{
		return actionType;
	}

	public String getLeftHandSide()
	{
		return leftHandSide;
	}

	public String getRightHandSide()
	{
		return rightHandSide;
	}

	public String getActionString()
	{
		return leftHandSide + " := " + rightHandSide;
	}

	public String toString()
	{
		if (actionType.equals("N"))
		{
			return actionType + " " + leftHandSide;
		}
		else
		{
			return actionType + " " + getActionString();
		}
	}
}
