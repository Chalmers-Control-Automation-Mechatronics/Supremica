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
/*
 * Created on Dec 16, 2004
 */
/**
 * @author Goran Cengic
 */
package org.supremica.functionblocks.model;

import java.util.*;

public class BasicFBType extends FBType
{

	// the ECC of the type
	private ECC theECC = new ECC();

	// Alogrithms
	// map algorithm name to algorithm text
	private Map algorithms = new HashMap();
	// map algorithm name to algorithm variables map;
	//private Map algorithmVariablesMap = new HashMap();

	//==========================================================================

	// forbid this kind of instantiation
	private BasicFBType() {}

	// instantiate BasicFBType of name n in resource r 
	public BasicFBType(String n,Resource r)
	{
		System.out.println("BasicFBType(" + n + "," + r.getName()  + ")");
		setName(n);
		resource = r;
	}
	
	//==========================================================================
	

	public FBInstance createInstance(String name)
	{
		System.out.println("BasicFBType.createInstace(" + name + ")");
		BasicFBInstance newInstance = new BasicFBInstance(name,resource,this);	
		
		newInstance.setEvents(events);

		newInstance.setVariables((Variables) variables.clone());

		instances.put(name,newInstance);

		return newInstance;
	}

	public ECC getECC()
	{
		return theECC;
	}

    public void addAlgorithm(Algorithm alg)
    {
		algorithms.put(alg.getName(),alg);
    }

    public Algorithm getAlgorithm(String name)
    {
		return (Algorithm) algorithms.get(name);
    }


}
