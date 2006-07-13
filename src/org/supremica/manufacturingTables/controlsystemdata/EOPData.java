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
 * The EOP class describes an EOP, Execution of OPeration, that is to be read by a MachineController
 * 
 *
 * Created: Wed May  24 07:55:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemdata;

import java.util.List;
import java.util.LinkedList;

public class EOPData
{
    private int id;
    private String type;
    private String comment;
    private List EOPRows; // contains initial row and action rows 
    public static final String ALTERNATIVE_TYPE = "alternative";
    public static final String BASIC_TYPE = "basic";
    public static final String [] TYPES = {BASIC_TYPE, ALTERNATIVE_TYPE};
    public static final String IGNORE_TOKEN = "*";
    public EOPData(int id, String type)
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
   
    public int getId()
    {
	return id;
    }
 
    // Set the first element of the EOPRows list to the new EOPInitialRow.
    // If the list is empty, add the EOPInitialRow to the list.
    public void setEOPInitialRow(EOPInitialRowData EOPInitialRow)
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
    
    public EOPInitialRowData getEOPInitialRow()
    {
	return (EOPInitialRowData) ( (LinkedList) EOPRows ).getFirst();
    }

    // Append a new EOPActionRow to the end of the EOPRows list
    public void addEOPActionRow(EOPActionRowData newEOPActionRow)
    {
	EOPRows.add(newEOPActionRow);
    }

    // Return the whole list with EOPRows including actionrow
    public List getEOPRows()
    {
	return EOPRows;
    }
    
    // Return the list of actionrows
    public List getEOPActionRows()
    {
	return EOPRows.subList( 1, EOPRows.size() );
    }
}