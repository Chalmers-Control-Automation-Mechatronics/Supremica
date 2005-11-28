
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
import org.supremica.petrinet.*;
import org.supremica.petrinet.algorithms.*;
import java.util.*;
import java.io.*;

public class RecipeResourcePetriNetBuilder
{

	// private PlantConnections plantConnections;
	private PetriNet thePetriNet;
	private InternalOperationRecipes theRecipes;
	private List firstPlaces = new LinkedList();
	private List lastPlaces = new LinkedList();
	private HashMap resourcePlaces = new HashMap();
	private int transitionCounter = 1;
	private int nbr_of_resource_places = 0;
	private int[] product_start_index;
	private int recipeCounter = 0;

	public RecipeResourcePetriNetBuilder() {}

	/*
	 *      public RecipeResourcePetriNetBuilder(PlantConnections plantConnections)
	 *       {
	 *               this.plantConnections = plantConnections;
	 *       }
	 */
	public PetriNet buildPetriNet(InternalOperationRecipes theRecipes)
		throws Exception
	{
		this.theRecipes = theRecipes;
		nbr_of_resource_places = 0;
		product_start_index = new int[theRecipes.nbrOfRecipes()];
		recipeCounter = 0;
		thePetriNet = new PetriNet("unknown");

		Iterator recipeIt = theRecipes.iterator();

		while (recipeIt.hasNext())
		{
			InternalOperationRecipe currRecipe = (InternalOperationRecipe) recipeIt.next();

			doInternalOperationRecipe(currRecipe);
		}

		// Create goalTransit
		Transition goalTransit = new Transition("goaltransit");

		thePetriNet.addTransition(goalTransit);

		// Create all arcs to the goalTransit
		Iterator lastPlacesIt = lastPlaces.iterator();

		while (lastPlacesIt.hasNext())
		{
			Place currPlace = (Place) lastPlacesIt.next();

			thePetriNet.addArc(currPlace, goalTransit);
		}

		// Add all resource places
		Iterator resourceIt = resourcePlaces.values().iterator();

		while (resourceIt.hasNext())
		{
			Place currPlace = (Place) resourceIt.next();

			thePetriNet.addPlace(currPlace);
		}

		return thePetriNet;
	}

	public String buildComments()
	{
		StringBuffer sb = new StringBuffer();
		int nbr_of_product_places = thePetriNet.nbrOfPlaces() - nbr_of_resource_places;

		sb.append("nbr_of_products: " + theRecipes.nbrOfRecipes() + "\n");
		sb.append("nbr_of_product_places: " + nbr_of_product_places + "\n");

		for (int i = 0; i < product_start_index.length; i++)
		{
			sb.append("product_start_index: " + product_start_index[i] + "\n");
		}

		sb.append("nbr_of_resource_places: " + nbr_of_resource_places + "\n");

		return sb.toString();
	}

	private Place getResourcePlace(String name)
		throws Exception
	{
		if (resourcePlaces.containsKey(name))
		{
			return (Place) resourcePlaces.get(name);
		}
		else
		{
			nbr_of_resource_places++;

			Place newPlace = new Place(name);

			newPlace.setMarking(1);
			newPlace.setTime(0);
			resourcePlaces.put(name, newPlace);

			// thePetriNet.addPlace(newPlace); // Do this after all products are created
			return newPlace;
		}
	}

	private Place getPlace(String recipe, String operation, String resource)
	{
		String name = recipe + "_" + operation + "_" + resource;

		return thePetriNet.getPlace(name);
	}

	private void doInternalOperationRecipe(InternalOperationRecipe theRecipe)
		throws Exception
	{
		String recipeName = theRecipe.getIdentity();

		// Create first and last places
		Place firstPlace = new Place(recipeName + "_first");

		thePetriNet.addPlace(firstPlace);
		firstPlace.setMarking(1);
		firstPlace.setTime(0);

		Place lastPlace = new Place(recipeName + "_last");

		thePetriNet.addPlace(lastPlace);
		lastPlace.setTime(0);
		firstPlaces.add(firstPlaces);
		lastPlaces.add(lastPlace);

		// Update the indicies
		product_start_index[recipeCounter++] = firstPlace.getIndex();

		// Create a place for each <operation,resource>
		Iterator operationIt = theRecipe.operationIterator();

		while (operationIt.hasNext())
		{
			InternalOperation currOperation = (InternalOperation) operationIt.next();
			String currOperationName = currOperation.getIdentity();
			Iterator resourceIt = currOperation.resourceCandidateIterator();

			while (resourceIt.hasNext())
			{
				String currResourceName = (String) resourceIt.next();
				Place currPlace = new Place(recipeName + "_" + currOperationName + "_" + currResourceName);

				if (currOperation.getTime() != Integer.MIN_VALUE)
				{
					currPlace.setTime(currOperation.getTime());
				}
				else
				{
					currPlace.setTime(0);
				}

				thePetriNet.addPlace(currPlace);
			}
		}

		// Create an outgoing transition for each combination of <next_operation, resource>
		operationIt = theRecipe.operationIterator();

		while (operationIt.hasNext())
		{
			InternalOperation currOperation = (InternalOperation) operationIt.next();
			String currOperationName = currOperation.getIdentity();
			Iterator resourceIt = currOperation.resourceCandidateIterator();

			while (resourceIt.hasNext())
			{
				String currResourceName = (String) resourceIt.next();

				// Final operation
				if (currOperation.isFinal())
				{
					Transition currTransition = new Transition("t_" + transitionCounter++);

					thePetriNet.addTransition(currTransition);

					Place prevPlace = getPlace(recipeName, currOperationName, currResourceName);

					thePetriNet.addArc(prevPlace, currTransition);
					thePetriNet.addArc(currTransition, lastPlace);
				}

				// Iterate over all the next transitions in the InternalOperationRecipe
				Iterator intTransitionIt = currOperation.nextTransitionIterator();

				while (intTransitionIt.hasNext())
				{
					InternalTransition intTransition = (InternalTransition) intTransitionIt.next();

					// Check that we have a valid structure
					if (intTransition.nbrOfPrevOperations() > 1)
					{
						throw new SupremicaException("Only recipes with single prev operations are supported");
					}

					if (intTransition.nbrOfNextOperations() > 1)
					{
						throw new SupremicaException("Only recipes with single next operations are supported");
					}

					// First operation
					if (currOperation.isInitial())
					{
						Transition currTransition = new Transition("t_" + transitionCounter++);

						thePetriNet.addTransition(currTransition);
						currTransition.setLabel(recipeName + "_" + currOperationName + "_" + currResourceName);

						Place resourcePlace = getResourcePlace(currResourceName);

						thePetriNet.addArc(resourcePlace, currTransition);
						thePetriNet.addArc(firstPlace, currTransition);

						Place prevPlace = getPlace(recipeName, currOperationName, currResourceName);

						thePetriNet.addArc(currTransition, prevPlace);
					}

					// For all cases.
					Iterator nextOperationIt = intTransition.nextOperationIterator();

					while (nextOperationIt.hasNext())
					{
						InternalOperation nextOperation = (InternalOperation) nextOperationIt.next();
						String nextOperationName = nextOperation.getIdentity();

						// Find all possible resource candidates for the next resource
						Iterator nextResourceIt = nextOperation.resourceCandidateIterator();

						while (nextResourceIt.hasNext())
						{
							String nextResourceName = (String) nextResourceIt.next();

							// Create a transition for this allocation
							Transition currTransition = new Transition("t_" + transitionCounter++);

							thePetriNet.addTransition(currTransition);
							currTransition.setLabel(recipeName + "_" + nextOperationName + "_" + nextResourceName);

							Place prevPlace = getPlace(recipeName, currOperationName, currResourceName);
							Place nextPlace = getPlace(recipeName, nextOperationName, nextResourceName);

							thePetriNet.addArc(prevPlace, currTransition);
							thePetriNet.addArc(currTransition, nextPlace);

							if (!currResourceName.equals(nextResourceName))
							{

								// Add resource allocating arc
								Place nextResourcePlace = getResourcePlace(nextResourceName);

								thePetriNet.addArc(nextResourcePlace, currTransition);

								// Add resource deallocating arc
								Place prevResourcePlace = getResourcePlace(currResourceName);

								thePetriNet.addArc(currTransition, prevResourcePlace);
							}
						}
					}
				}
			}
		}
	}

	public static void main(String args[])
		throws Exception
	{
		InternalOperationRecipes recipes = new InternalOperationRecipes();
		InternalOperationRecipe b1 = new InternalOperationRecipe("b1");

		recipes.addRecipe(b1);

		InternalOperation o1 = new InternalOperation("o1");

		b1.addOperation(o1);

		InternalOperation o2 = new InternalOperation("o2");

		b1.addOperation(o2);

		InternalOperation o3 = new InternalOperation("o3");

		b1.addOperation(o3);

		InternalTransition t1 = new InternalTransition("t1");

		b1.addTransition(t1);
		b1.addArc(o1, t1);
		b1.addArc(t1, o2);

		InternalTransition t2 = new InternalTransition("t2");

		b1.addTransition(t2);
		b1.addArc(o2, t2);
		b1.addArc(t2, o3);
		o1.addResourceCandidate("u1");
		o1.addResourceCandidate("u2");
		o1.setTime(1);
		o2.addResourceCandidate("u2");
		o2.addResourceCandidate("u3");
		o2.setTime(2);
		o3.addResourceCandidate("u2");
		o3.addResourceCandidate("u3");
		o3.setTime(3);

		InternalOperationRecipe b2 = b1.createCopy("b2");

		recipes.addRecipe(b2);

		RecipeResourcePetriNetBuilder builder = new RecipeResourcePetriNetBuilder();
		PetriNet pn = builder.buildPetriNet(recipes);
		PetriNetToDsx exporter = new PetriNetToDsx(pn);
		PrintWriter pw = new PrintWriter(System.out);

		exporter.serialize(pw);
		pw.println();
		pw.println(builder.buildComments());
		pw.flush();
	}
}
