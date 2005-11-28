
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

public class InternalTransition
{
	private List prevOperations = new LinkedList();
	private List nextOperations = new LinkedList();
	private String identity;
	private boolean controllable;

	public InternalTransition(String identity)
	{
		this(identity, true);
	}

	public InternalTransition(String identity, boolean controllable)
	{
		if (identity == null)
		{
			System.err.println("null identity");
		}

		this.identity = identity;
		this.controllable = controllable;
	}

	public InternalTransition(InternalTransition orgTransition)
	{
		this.identity = orgTransition.identity;
		this.controllable = orgTransition.controllable;
	}

	public String getIdentity()
	{
		return identity;
	}

	void addPrevOperation(InternalOperation operation)
	{
		prevOperations.add(operation);
	}

	void addNextOperation(InternalOperation operation)
	{
		nextOperations.add(operation);
	}

	public Iterator nextOperationIterator()
	{
		return nextOperations.iterator();
	}

	public Iterator prevOperationIterator()
	{
		return prevOperations.iterator();
	}

	public int nbrOfPrevOperations()
	{
		return prevOperations.size();
	}

	public int nbrOfNextOperations()
	{
		return nextOperations.size();
	}

	public boolean isEnabled(InternalOperationState theState)
	{
		Iterator prevOperationIt = prevOperations.iterator();

		while (prevOperationIt.hasNext())
		{
			InternalOperation currOperation = (InternalOperation) prevOperationIt.next();

			if (!theState.isActive(currOperation))
			{
				return false;
			}
		}

		return true;
	}

	public InternalOperationState fire(InternalOperationState oldState)
	{
		InternalOperationState newState = new InternalOperationState(oldState);
		Iterator operationIt = prevOperations.iterator();

		while (operationIt.hasNext())
		{
			InternalOperation currOperation = (InternalOperation) operationIt.next();

			newState.deactiveOperation(currOperation);
		}

		operationIt = nextOperations.iterator();

		while (operationIt.hasNext())
		{
			InternalOperation currOperation = (InternalOperation) operationIt.next();

			newState.setActive(currOperation);
		}

		return newState;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("InternalTransition: " + identity + "\n");
		sb.append("\tPrevious operations:\n");

		Iterator operationIt = prevOperations.iterator();

		while (operationIt.hasNext())
		{
			InternalOperation currOperation = (InternalOperation) operationIt.next();

			sb.append("\t\t" + currOperation.getIdentity() + "\n");
		}

		sb.append("\tNext operations:\n");

		operationIt = nextOperations.iterator();

		while (operationIt.hasNext())
		{
			InternalOperation currOperation = (InternalOperation) operationIt.next();

			sb.append("\t\t" + currOperation.getIdentity() + "\n");
		}

		return sb.toString();
	}
}
