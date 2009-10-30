
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

import org.supremica.util.SupremicaException;
import java.util.*;

/**
 * This is an intermediate format, similar to Petri nets, for
 * storing recipes.
 */
public class InternalOperationRecipe
{
	private HashMap<String, InternalOperation> operations = new HashMap<String, InternalOperation>();
	private List<InternalOperation> sortedOperations = new LinkedList<InternalOperation>();
	private HashMap<String, InternalTransition> transitions = new HashMap<String, InternalTransition>();
	private List<InternalTransition> sortedTransitions = new LinkedList<InternalTransition>();
	private String identity;
	private InternalOperationRecipeStatus status = InternalOperationRecipeStatus.NotStarted;

	public InternalOperationRecipe(String identity)
	{
		this.identity = identity;
	}

	public String getIdentity()
	{
		return identity;
	}

	public void setIdentity(String identity)
	{
		this.identity = identity;
	}

	public void addOperation(InternalOperation operation)
		throws Exception
	{
		String id = operation.getIdentity();

		if (id == null)
		{
			throw new SupremicaException("Identity must be non null");
		}

		if (operations.containsKey(id))
		{
			throw new SupremicaException(id + " already exists");
		}

		operations.put(id, operation);
		operation.setIndex(sortedOperations.size());
		sortedOperations.add(operation);
	}

	public void addTransition(InternalTransition transition)
		throws Exception
	{
		String id = transition.getIdentity();

		if (id == null)
		{
			throw new SupremicaException("Identity must be non null");
		}

		if (transitions.containsKey(id))
		{
			throw new SupremicaException(id + " already exists");
		}

		transitions.put(id, transition);
		sortedTransitions.add(transition);
	}

	public void addArc(InternalOperation operation, InternalTransition transition)
		throws Exception
	{
		if (!transitions.containsKey(transition.getIdentity()))
		{
			throw new SupremicaException(identity + " does not exist");
		}

		if (!operations.containsKey(operation.getIdentity()))
		{
			throw new SupremicaException(identity + " does not exist");
		}

		operation.addNextTransition(transition);
		transition.addPrevOperation(operation);
	}

	public void addArc(InternalTransition transition, InternalOperation operation)
		throws Exception
	{
		if (!transitions.containsKey(transition.getIdentity()))
		{
			throw new SupremicaException(identity + " does not exist");
		}

		if (!operations.containsKey(operation.getIdentity()))
		{
			throw new SupremicaException(identity + " does not exist");
		}

		operation.addPrevTransition(transition);
		transition.addNextOperation(operation);
	}

	public InternalOperation getInternalOperation(String identity)
		throws Exception
	{
		if (!operations.containsKey(identity))
		{
			throw new SupremicaException(identity + " does not exist");
		}

		return operations.get(identity);
	}

	public InternalTransition getInternalTransition(String identity)
		throws Exception
	{
		if (!transitions.containsKey(identity))
		{
			throw new SupremicaException(identity + " does not exist");
		}

		return transitions.get(identity);
	}

	public Iterator<InternalOperation> operationIterator()
	{
		return sortedOperations.iterator();
	}

	public Iterator<InternalTransition> transitionIterator()
	{
		return sortedTransitions.iterator();
	}

	public int nbrOfOperations()
	{
		return sortedOperations.size();
	}

	public int nbrOfTransitions()
	{
		return sortedTransitions.size();
	}

	public InternalOperationRecipeStatus getStatus()
	{
		return status;
	}

	public void setStatus(InternalOperationRecipeStatus status)
	{
		this.status = status;
	}

	/**
	 * Creates a clone, including new operations and transitions,
	 * of this recipe.
	 */
	public InternalOperationRecipe createCopy(String newIdentity)
		throws Exception
	{
		InternalOperationRecipe newRecipe = new InternalOperationRecipe(newIdentity);

		// Create copies of all operations
		Iterator<InternalOperation> operationIt = operationIterator();

		while (operationIt.hasNext())
		{
			InternalOperation currOperation = operationIt.next();
			InternalOperation newOperation = new InternalOperation(currOperation);

			newRecipe.addOperation(newOperation);
		}

		// Create copies of all transitions
		Iterator<InternalTransition> transitionIt = transitionIterator();

		while (transitionIt.hasNext())
		{
			InternalTransition currTransition = transitionIt.next();
			InternalTransition newTransition = new InternalTransition(currTransition);

			newRecipe.addTransition(newTransition);
		}

		// Create copies of all arcs
		// Each element is responsible for creating its outgoing arcs
		// Start with creating all outgoing arcs from the operations
		operationIt = operationIterator();

		while (operationIt.hasNext())
		{
			InternalOperation orgOperation = operationIt.next();
			InternalOperation newOperation = newRecipe.getInternalOperation(orgOperation.getIdentity());

			transitionIt = orgOperation.nextTransitionIterator();

			while (transitionIt.hasNext())
			{
				InternalTransition orgTransition = transitionIt.next();
				InternalTransition newTransition = newRecipe.getInternalTransition(orgTransition.getIdentity());

				newRecipe.addArc(newOperation, newTransition);
			}
		}

		// Now create all outgoing arcs from the transitions
		transitionIt = transitionIterator();

		while (transitionIt.hasNext())
		{
			InternalTransition orgTransition = transitionIt.next();
			InternalTransition newTransition = newRecipe.getInternalTransition(orgTransition.getIdentity());

			operationIt = orgTransition.nextOperationIterator();

			while (operationIt.hasNext())
			{
				InternalOperation orgOperation = operationIt.next();
				InternalOperation newOperation = newRecipe.getInternalOperation(orgOperation.getIdentity());

				newRecipe.addArc(newTransition, newOperation);
			}
		}

		return newRecipe;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("InternalOperationRecipe: " + identity + "\n");

		Iterator<InternalOperation> operationIt = operationIterator();

		while (operationIt.hasNext())
		{
			InternalOperation currOperation = operationIt.next();

			sb.append(currOperation);
		}

		Iterator<InternalTransition> transitionIt = transitionIterator();

		while (transitionIt.hasNext())
		{
			InternalTransition currTransition = (transitionIt.next());

			sb.append(currTransition);
		}

		return sb.toString();
	}

	public Iterator<InternalTransition> enabledTransitions(InternalOperationState theState)
	{
		List<InternalTransition> enabledTransitions = new LinkedList<InternalTransition>();
		Iterator<InternalTransition> transitionIt = transitionIterator();

		while (transitionIt.hasNext())
		{
			InternalTransition currTransition = transitionIt.next();

			if (currTransition.isEnabled(theState))
			{
				enabledTransitions.add(currTransition);
			}
		}

		return enabledTransitions.iterator();
	}

	public InternalOperationState createFirstState()
		throws Exception
	{
		InternalOperationState theState = new InternalOperationState(nbrOfOperations());

		if (status == InternalOperationRecipeStatus.Undetermined)
		{
			throw new SupremicaException("Recipe status is undetermined");
		}

		if (status == InternalOperationRecipeStatus.Finished)
		{
			throw new SupremicaException("Recipe status is finished");
		}

		if (status == InternalOperationRecipeStatus.NotStarted)
		{
			theState.setInitial(true);

			return theState;
		}

		if (status == InternalOperationRecipeStatus.Running)
		{    // Find all operations that are active
			Iterator<InternalOperation> operationIt = operationIterator();

			while (operationIt.hasNext())
			{
				InternalOperation currOperation = operationIt.next();

				if (currOperation.isActive())
				{
					theState.setActiveResource(currOperation, currOperation.getActiveResource());
				}
			}

			return theState;
		}

		throw new SupremicaException("Unknown status");
	}

	/**
	 * All recipes are required to have a single initial operation.
	 * All recipes are required to have a single final operation.
	 */
	public boolean hasValidStructure()
	{
		int nbrOfInitialOperations = 0;
		int nbrOfFinalOperations = 0;
		Iterator<InternalOperation> operationIt = operationIterator();

		while (operationIt.hasNext())
		{
			InternalOperation currOperation = operationIt.next();

			if (currOperation.isInitial())
			{
				nbrOfInitialOperations++;
			}

			if (currOperation.isFinal())
			{
				nbrOfFinalOperations++;
			}

			if (currOperation.nbrOfResourceCandidates() < 1)
			{
				System.err.println("Each operation must have at least one resource candidate");

				return false;
			}
		}

		if (nbrOfInitialOperations != 1)
		{
			System.err.println("Only recipes with exactly one initial operation are handled");

			return false;
		}

		if (nbrOfFinalOperations != 1)
		{
			System.err.println("Only recipes with exactly one final operation are handled");

			return false;
		}

		return true;
	}
}
