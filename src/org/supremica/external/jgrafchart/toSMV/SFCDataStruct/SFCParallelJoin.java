package org.supremica.external.jgrafchart.toSMV.SFCDataStruct;
import java.util.*;

public class SFCParallelJoin
{
	String id = null;
	String nextTransitionId = null;
	List prevStepIds = null;
	List subJoins = new LinkedList();

	public SFCParallelJoin(String id,List prevStepIds,String nextTransitionId)
	{
		this.id = id;
		this.nextTransitionId = nextTransitionId;
		this.prevStepIds = prevStepIds;
	}

	public String getId()
	{
		return id;
	}

	public String getNextTransitionId()
	{
		return nextTransitionId;
	}

	public List getPrevStepIds()
	{
		return prevStepIds;
	}

	public List getSubJoins()
	{
		return subJoins;
	}

	public String toString()
	{
		return id;
	}

	public void setPrevStepIds(List prevStepIds)
	{
		this.prevStepIds = prevStepIds;
	}

	public void setNextTransitionId(String nextTransitionId)
	{
		this.nextTransitionId = nextTransitionId;
	}
	public void addSubJoins(SFCParallelJoin aSubJoin)
	{
		if(!subJoins.contains(aSubJoin))
		{
			subJoins.add(aSubJoin);
		}
	}

}