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
 * The SOP class describes a SOP, Sequence of OPeration, that is to be read by the Coordinator for the 
 * whole manufacturing cell. A SOP contains the order to perform different operations for one machine.
 * 
 *
 * Created: Thu Jun  08 09:00:13 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemdata;

import java.util.List;
import java.util.LinkedList;

public class SOPData
{
    private String id;
    private String comment;
    private String machine;
    private List activities; 
    // List of activities. Each activity contains (optional) preconditions for operations in other machines, and 
    // then always an operation for this SOPs machine that has to be performed.
 
    public SOPData(String id, String machine)
    {
	this.id = id;
	this.machine = machine;
	comment = null;
	activities = new LinkedList();
    }

    public String getMachine()
    {
	return machine;
    }

    public String getComment()
    {
	return comment;
    }

    public void setComment(String comment)
    {
	this.comment = comment;
    }
   
    public String getId()
    {
	return id;
    }
 
    // Add a new SOP activity to the end of the activity list.
    public void addSOPActivity(SOPActivity SOPActivity)
    {
	activities.add(SOPActivity);
    }
    
    public List getSOPActivities()
    {
	return activities;
    }

}