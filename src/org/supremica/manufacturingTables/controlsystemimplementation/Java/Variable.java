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
 * The Variable class describes some state of a part of the machine, relevant for the control 
 * function (the EOPs) and can have a finite number of states.
 *
 *
 * Created: Mon Apr  24 15:00:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.HashMap;
import java.util.Map;

public class Variable implements Listener
{
    private String name;
    private Map values; // HashMap will be used for quick access to the values.
    private String currentValue;
    private Mailbox mailbox;

    public Variable(String name, Mailbox mailbox)
    {
	this.name = name;
	values = new HashMap(5); //initital capacity 5 and default load factor (0,75) suits me fine;
	currentValue = null;
	this.mailbox = mailbox;
	mailbox.register(this);
    }

    public String getName()
    {
	return name;
    }
    
    public boolean setCurrentValue(String newValue)
    {
	if (values.containsKey(newValue))
	    {
		currentValue = newValue;
		return true;
	    }
	else
	    {
		System.err.println("The value "+ newValue  +" is not possible to set for the variable " + name + ".");
		return false;
	    }
    }

    public void addValue(String valueToAdd)
    {
	values.put(valueToAdd, valueToAdd); 
	// Now Strings are used both as values and keys, but the value may in the future be a Value object
    }

    public String getID()
    {
	return getName();
    }

    public void receiveMessage(Message msg) 
    {
	if (msg.getType().equals("requestState"))
	{
	    mailbox.send(new Message(getID(), "MachineController", "reportState", currentValue));
	}
	else if (msg.getType().equals("checkState"))
	{
	    boolean valueOK = ((String) msg.getContent()).equals(currentValue); 
	    mailbox.send(new Message(getID(), "MachineController", "confirmState", valueOK) );
	}
	// An order is allways performed for a variable that we can control, if the value to be set is a correct
	// value for this variable
	else if (msg.getType().equals("orderState"))
	{
	    mailbox.send(new Message(getID(), "MachineController", "confirmState", setCurrentValue( (String) msg.getContent() ) ) );
	}
	else
	{
	    System.err.println("Unknown message type for the top level actuator " + name + " !");
	}
    }
    
}