
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
 * Haradsgatan 26A
 * 431 42 Molndal
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


/**
 * This is container class for storing several
 * InternalOperationRecipes together.
 */
public class InternalOperationRecipes
{

	private HashMap theRecipes = new HashMap();

	public InternalOperationRecipes() {}

	public void addRecipe(InternalOperationRecipe theRecipe)
		throws Exception
	{
		theRecipes.put(theRecipe.getIdentity(), theRecipe);
	}

	public void removeRecipe(InternalOperationRecipe theRecipe)
		throws Exception
	{
		removeRecipe(theRecipe.getIdentity());
	}

	public void removeRecipe(String recipeName)
		throws Exception
	{

		if (!contains(recipeName))
		{
			throw new Exception(recipeName + " does not exists");
		}

		theRecipes.remove(recipeName);
	}

	public InternalOperationRecipe getRecipe(String recipeName)
	{
		return (InternalOperationRecipe) theRecipes.get(recipeName);
	}

	public boolean contains(InternalOperationRecipe theRecipe)
	{
		return contains(theRecipe.getIdentity());
	}

	public boolean contains(String recipeName)
	{
		return theRecipes.containsKey(recipeName);
	}

	public int nbrOfRecipes()
	{
		return theRecipes.size();
	}

	public Iterator iterator()
	{
		return theRecipes.values().iterator();
	}

	public static void main(String args[])
		throws Exception
	{

		InternalOperationRecipes theRecipes = new InternalOperationRecipes();
		InternalOperationRecipe firstRecipe = new InternalOperationRecipe("firstRecipe");

		theRecipes.addRecipe(firstRecipe);

		InternalOperation p1 = new InternalOperation("p1");
		InternalOperation p2 = new InternalOperation("p2");
		InternalOperation p3 = new InternalOperation("p3");
		InternalOperation p4 = new InternalOperation("p4");
		InternalOperation p5 = new InternalOperation("p5");
		InternalOperation p6 = new InternalOperation("p6");

		p1.addResourceCandidate("m1");
		p1.addResourceCandidate("m2");
		p4.addResourceCandidate("m3");
		firstRecipe.addOperation(p1);
		firstRecipe.addOperation(p2);
		firstRecipe.addOperation(p3);
		firstRecipe.addOperation(p4);
		firstRecipe.addOperation(p5);
		firstRecipe.addOperation(p6);

		InternalTransition t1 = new InternalTransition("t1");
		InternalTransition t2 = new InternalTransition("t2");
		InternalTransition t3 = new InternalTransition("t3");
		InternalTransition t4 = new InternalTransition("t4");
		InternalTransition t5 = new InternalTransition("t5");
		InternalTransition t6 = new InternalTransition("t6");

		firstRecipe.addTransition(t1);
		firstRecipe.addTransition(t2);
		firstRecipe.addTransition(t3);
		firstRecipe.addTransition(t4);
		firstRecipe.addTransition(t5);
		firstRecipe.addTransition(t6);

		// Arcs from operations to transitions
		firstRecipe.addArc(p1, t1);
		firstRecipe.addArc(p2, t2);
		firstRecipe.addArc(p3, t2);
		firstRecipe.addArc(p4, t3);
		firstRecipe.addArc(p4, t4);

		// firstRecipe.addArc(p5, t5);
		// firstRecipe.addArc(p6, t6);
		// Arcs from transitions to operations
		firstRecipe.addArc(t1, p2);
		firstRecipe.addArc(t1, p3);
		firstRecipe.addArc(t2, p4);
		firstRecipe.addArc(t3, p5);
		firstRecipe.addArc(t4, p6);

		// firstRecipe.addArc(t5, p1);
		// firstRecipe.addArc(t6, p1);
		InternalOperationRecipe secondRecipe = firstRecipe.createCopy("secondRecipe");

		theRecipes.addRecipe(secondRecipe);

		Iterator recipeIt = theRecipes.iterator();

		while (recipeIt.hasNext())
		{
			InternalOperationRecipe currRecipe = (InternalOperationRecipe) recipeIt.next();

			System.out.println(currRecipe);
		}
	}
}
