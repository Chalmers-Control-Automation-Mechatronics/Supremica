
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.recipe;

import java.util.*;

public class InternalOperation
{
	private LinkedList<InternalTransition> prevTransitions = new LinkedList<InternalTransition>();
	private LinkedList<InternalTransition> nextTransitions = new LinkedList<InternalTransition>();
	private ArrayList<String> resourceCandidates = new ArrayList<String>();
	private String identity;
	private String activeResource = null;
	private int time = Integer.MIN_VALUE;
	private int index = -1;

	public InternalOperation(String identity)
	{
		this.identity = identity;
	}

	public InternalOperation(InternalOperation orgOperation)
	{
		identity = orgOperation.identity;
		activeResource = orgOperation.activeResource;
		time = orgOperation.time;
		index = orgOperation.index;

		// Copy the resource candidates
		Iterator<String> resourceIt = orgOperation.resourceCandidates();

		addResourceCandidates(resourceIt);
	}

	public String getIdentity()
	{
		return identity;
	}

	public boolean isInitial()
	{
		return prevTransitions.size() == 0;
	}

	public boolean isFinal()
	{
		return nextTransitions.size() == 0;
	}

	public void setTime(int time)
	{
		this.time = time;
	}

	public int getTime()
	{
		return time;
	}

	public void setActiveResource(String currResource)
	{
		activeResource = currResource;
	}

	public String getActiveResource()
	{
		return activeResource;
	}

	public void deactivateOperation()
	{
		activeResource = null;
	}

	public boolean isActive()
	{
		return activeResource != null;
	}

	public void addResourceCandidates(Iterator<String> resourceIt)
	{
		while (resourceIt.hasNext())
		{
			String resource = resourceIt.next();

			addResourceCandidate(resource);
		}
	}

	public void addResourceCandidate(String resource)
	{
		resourceCandidates.add(resource);
	}

	// Remove !?
	public Iterator<String> resourceCandidates()
	{
		return resourceCandidates.iterator();
	}

	public Iterator<String> resourceCandidateIterator()
	{
		return resourceCandidates.iterator();
	}

	public void clearResourceCandidates()
	{
		resourceCandidates.clear();
	}

	public int nbrOfResourceCandidates()
	{
		return resourceCandidates.size();
	}

	void addPrevTransition(InternalTransition transition)
	{
		prevTransitions.add(transition);
	}

	void addNextTransition(InternalTransition transition)
	{
		nextTransitions.add(transition);
	}

	public Iterator<InternalTransition> nextTransitionIterator()
	{
		return nextTransitions.iterator();
	}

	public Iterator<InternalTransition> prevTransitionIterator()
	{
		return prevTransitions.iterator();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("InternalOperation: " + identity + " initial: " + isInitial() + " final: " + isFinal() + "\n");
		sb.append("\tPrevious transitions:\n");

		Iterator<InternalTransition> transitionsIt = prevTransitions.iterator();

		while (transitionsIt.hasNext())
		{
			InternalTransition currTransition = transitionsIt.next();

			sb.append("\t\t" + currTransition.getIdentity() + "\n");
		}

		sb.append("\tNext transitions:\n");

		transitionsIt = nextTransitions.iterator();

		while (transitionsIt.hasNext())
		{
			InternalTransition currTransition = transitionsIt.next();

			sb.append("\t\t" + currTransition.getIdentity() + "\n");
		}

		sb.append("\tResources:\n");

		Iterator<String> resourceIt = resourceCandidates.iterator();

		while (resourceIt.hasNext())
		{
			String currResource = resourceIt.next();

			sb.append("\t\t" + currResource + "\n");
		}

		return sb.toString();
	}

	void setIndex(int index)
	{
		this.index = index;
	}

	int getIndex()
	{
		return index;
	}
}
