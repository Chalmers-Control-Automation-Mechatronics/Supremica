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
 * The Mailbox class contains information, in a hashmap for fast search, about the listeners (such as Sensors, Actuators
 * Machine, Coordinator and MachineController. Listeners have to register themselfs.
 *
 *
 * Created: Tue Okt  30 11:17 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.IEC61499;

import java.util.HashMap;
import java.util.Map;

public class Mailbox
{
    private Map listeners; // HashMap will be used for quick access to listeners of the mailbox

    public Mailbox()
    {
	listeners = new HashMap(10); //initital capacity 10 and default load factor (0,75) suits me fine
    }

    public void send(Message msg)
    {
	Listener listener = (Listener) listeners.get(msg.getReceiver());
	if (listener != null)
	    {
		listener.receiveMessage(msg);
	    }
	else 
	    {
		System.err.println("No listener " + msg.getReceiver() + " registered!");
	    }
    }
   
    public void register(Listener newListener)
    {
	listeners.put(newListener.getID(), newListener);
    }
}