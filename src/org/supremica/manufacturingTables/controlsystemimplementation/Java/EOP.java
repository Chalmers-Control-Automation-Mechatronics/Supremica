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
 * The EOP class describes an EOP, Execution of OPeration, that is read by a MachineController
 * 
 *
 * Created: Mon May  15 16:08:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Iterator;
import java.util.Map.Entry;

public class EOP
{
    private int id;
    private String type;
    private String comment;
    private List EOPRows; // contains initial row and action rows 
    static final String [] TYPES = {"alternative", "basic"};
    private ListIterator actionIterator;
    
    public EOP(int id, String type)
    {
	this.id = id;

	boolean typeOK = false;
	for (int i=0; i<TYPES.length; i++)
	    {
		if (TYPES[i].equals(type))
		    {
			typeOK = true;
		    }
	    }
	if (!typeOK)
	    {
		System.err.println("Wrong EOP type declared!");
		return;
	    }
	    
	this.type = type;
	comment = null;
	EOPRows = new LinkedList();
	actionIterator = EOPRows.listIterator();
    }

    public int getId()
    {
	return id;
    }

    public String getType()
    {
	return type;
    }

    public String getComment()
    {
	return comment;
    }

    public void setComment(String comment)
    {
	this.comment = comment;
    }
    // Set the first element of the EOPRows list to the new EOPInitialRow.
    // If the list is empty, add the EOPInitialRow to the list.
    public void setEOPInitialRow(EOPInitialRow EOPInitialRow)
    {
	if ( EOPRows.size() == 0 )
	    {
		EOPRows.add(EOPInitialRow);
	    }
	else
	    {
		EOPRows.set(0, EOPInitialRow);
	    }
    }

//     public EOPInitialRow getEOPInitialRow()
//     {
// 	return (EOPInitialRow) ((LinkedList) EOPRows).getFirst();
//     }

    // Returns a clone of EOPInitialRow with bookingZones, unbookingZones and actuators, sensors and variables
    // that must be checked (those that does not have a "*" as the state).
    public EOPInitialRow getEOPInitialRowActions()
    {
	EOPInitialRow initialRowActions =  (EOPInitialRow) ( (EOPInitialRow) ( (LinkedList) EOPRows).getFirst() ).clone();
        initialRowActions.removeUnimportantStates();
	startActions();
	return initialRowActions;
    }

    // Append a new EOPActionRow to the end of the EOPRows list
    public void addEOPActionRow(EOPActionRow newEOPActionRow)
    {
	EOPRows.add(newEOPActionRow);
    }

    // Goto the first action row. 
    public void startActions()
    {
	actionIterator = EOPRows.listIterator();
	actionIterator.next();
    }
    
    // Return true if the EOP has more actions to perform.
    public boolean hasMoreActions()
    {
	return actionIterator.hasNext();
    }
    
    // Returns next EOPActionRow with bookingZones, unbookingZones and only changing actuators and sensors 
    // for the next action. A "*" means that the state of that component is not important and hence those are
    // ignored as well. Returns null if there are no more EOPActionRows.
    public EOPActionRow getNextActions()
    {
	if (!hasMoreActions())
	    {
		return null;
	    }
	else
	    {
		EOPRow previousRow = (EOPRow) EOPRows.get( actionIterator.previousIndex() ); 
		// previousIndex does not change the value of the iterator 
		EOPActionRow nextActions = (EOPActionRow) ( (EOPActionRow) actionIterator.next() ).clone();
		nextActions.removeUnchangedComponents(previousRow);

		return nextActions;
	    }
    }
    // Returns the last row. Is often used to get the last row of the whole list.
    public EOPRow getLastRow()
    {
	return (EOPRow) EOPRows.get( actionIterator.previousIndex() ); 

    }
}