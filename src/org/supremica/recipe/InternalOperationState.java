
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


public class InternalOperationState
{

	private String[] activeOperations;
	private int[] activeResources;
	private static final String undeterminedResource = new String("");
	private boolean initial = false;

	public InternalOperationState(int nbrOfOperations)
	{
		activeOperations = new String[nbrOfOperations];
		activeResources = new int[nbrOfOperations];
	}

	public InternalOperationState(InternalOperationState oldState)
	{

		this(oldState.activeOperations.length);

		System.arraycopy(oldState.activeOperations, 0, activeOperations, 0, oldState.activeOperations.length);
		System.arraycopy(oldState.activeResources, 0, activeResources, 0, oldState.activeResources.length);
	}

	public void setActive(InternalOperation op)
	{
		setActiveResource(op, undeterminedResource);
	}

	public void setActiveResource(InternalOperation op, String resource)
	{
		activeOperations[op.getIndex()] = resource;
	}

	public void deactiveOperation(InternalOperation op)
	{
		activeOperations[op.getIndex()] = null;
	}

	public boolean isActive(InternalOperation op)
	{
		return activeOperations[op.getIndex()] != null;
	}

	public String getActiveResource(InternalOperation op)
	{
		return activeOperations[op.getIndex()];
	}

	public void resetState()
	{

		for (int i = 0; i < activeOperations.length; i++)
		{
			activeOperations[i] = null;
		}
	}

	public boolean isInitial()
	{
		return initial;
	}

	public void setInitial(boolean initial)
	{
		this.initial = initial;
	}

	public void computeNextResourceSet() {}

	public void resetResourceSet()
	{

		for (int i = 0; i < activeResources.length; i++)
		{
			activeResources[i] = -1;
		}
	}

	public int size()
	{
		return activeOperations.length;
	}
}
