package org.supremica.external.jgrafchart.toSMV.SFCDataStruct;

import java.util.*;

public class SFCParallelSplit
{
	String id = null;
	List nextStepIds = null;
	String prevTransitionId = null;
	List subSplits = new LinkedList();
	SFCParallelJoin matchingJoin = null;

	public SFCParallelSplit(String id, String prevTransitionId, List nextStepIds)
	{
		this.id = id;
		this.nextStepIds = nextStepIds;
		this.prevTransitionId = prevTransitionId;
	}

	public String getId()
	{
		return id;
	}

	public List getNextStepIds()
	{
		return nextStepIds;
	}

	public void setNextStepIds(List nextStepIds)
	{
		this.nextStepIds = nextStepIds;
	}

	public String getPrevTransitionId()
	{
		return prevTransitionId;
	}

	public void addSubSplits(SFCParallelSplit aSubSplit)
	{
		if (!subSplits.contains(aSubSplit))
		{
			subSplits.add(aSubSplit);
		}
	}

	public List getSubSplits()
	{
		return subSplits;
	}

	public void setMatchingJoin(SFCParallelJoin matchingJoin)
	{
		this.matchingJoin = matchingJoin;
	}

	public SFCParallelJoin getMatchingJoin()
	{
		return matchingJoin;
	}

	public String toString()
	{
		return id;
	}
}
