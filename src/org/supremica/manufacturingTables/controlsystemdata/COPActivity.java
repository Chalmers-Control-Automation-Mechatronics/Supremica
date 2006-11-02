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

/**
 * The COPActivity contains (optional) preconditions for operations in other machines, and 
 * then always an operation for the corresponding COPs machine that has to be performed.
 *
 * Created: Thu Jun  08 13:40:13 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemdata;

import java.util.List;
import java.util.LinkedList;

public class COPActivity
{
    private String operation;
    private List predecessors; 
    // List of predecessors. For now I assume that all predecessors has to be performed to begin the operation, 
    // i.e. I assume a logical "and" relation between the predecessors. There may be other logical expressions 
    // in the future. For now the order is not important but I still use lists.

    public COPActivity(String operation)
    {
	this.operation = operation;
	predecessors = new LinkedList();
    }

    public String getOperation()
    {
	return operation;
    }

    public List getPredecessors()
    {
	return predecessors;
    }

    // Add a new predecessor to the list.
    public void addPredecessor(Predecessor predecessorToAdd)
    {
	predecessors.add(predecessorToAdd);
    }
    
}