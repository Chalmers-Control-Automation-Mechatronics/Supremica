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
 * The COPPredecessor contains a machine name and the operationNbr in that machine that has to be performed 
 * before the COP can continue.
 *
 * Created: Wed Jun  08 13:40:13 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

public class COPPredecessor implements Cloneable
{
    private String operation;
    private String machine;

    public COPPredecessor(String operation, String machine)
    {
	this.operation = operation;
	this.machine = machine;
    }

    public String getOperation()
    {
	return operation;
    }

    public String getMachine()
    {
	return machine;
    }

    // Since it is not absolutely certain that all operation numbers for the whole manufacturing cell
    // are unique this method returns a concatenation of the Machine and the operation that for certain is unique,
    public String getID()
    {
	return (String) (machine + operation);
    }

    public boolean equals(COPPredecessor predecessorToCompare)
    {
	return operation.equals( predecessorToCompare.getOperation() ) && machine.equals( predecessorToCompare.getMachine() );
    }
    
    public Object clone() 
    {
	COPPredecessor clone = null;
	try
	    {
		clone =(COPPredecessor) super.clone(); // Create space and clone the trivial data (all are)
	    }
	catch (CloneNotSupportedException e)
	    {
		System.err.println("The COPPredecessor could not be cloned!");
	    } 
	return clone;
    }

}