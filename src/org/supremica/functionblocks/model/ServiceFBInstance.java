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
 * @author cengic
 */
package org.supremica.functionblocks.model;

import bsh.Interpreter;
import java.util.Iterator;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;

public class ServiceFBInstance extends FBInstance
{
	private File serviceScript;

	private Object serviceState;
	
	private Interpreter interpreter = new Interpreter();

	private Event currentEvent;

	//==========================================================================
	private ServiceFBInstance() {}
	
	public ServiceFBInstance(String n, Resource r, ServiceFBType t, File script)
	{
		setName(n);
		resource = r;
		fbType = t;
		serviceScript = script;
	}
	//==========================================================================


	public void initialize()
	{
		try
		{
			interpreter.set("serviceInitialize", true);
			interpreter.set("serviceFB", this);
			interpreter.set("serviceState", null);
			interpreter.set("serviceEvent", null);
			interpreter.set("serviceVariables", variables);
			
			// evaluate the serviceScript
			Reader serviceScriptReader = new FileReader(serviceScript);
			interpreter.eval(serviceScriptReader);
			
			serviceState = interpreter.get("serviceState");
			interpreter.set("serviceInitialize", false);
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}

	public void receiveEvent(String eventInput)
	{
		if(variables.getVariable(eventInput) != null)
			if(variables.getVariable(eventInput).getType().equals("EventInput"))
			{
				currentEvent = (Event) events.get(eventInput);
				resource.getScheduler().scheduleFBInstance(this);
			}
			else
			{
				System.err.println("ServiceFBInstance: No event input " + eventInput);
				System.exit(0);
			}
		else
		{
			System.err.println("ServiceFBInstance: No event input " + eventInput);
			System.exit(0);
		}
	}

	public void handleEvent()
	{
		// set all InputEvents to false
		for (Iterator iter = variables.iterator();iter.hasNext();)
		{
			String curName = (String) iter.next();
			if (((Variable) variables.getVariable(curName)).getType().equals("EventInput"))
			{
				((BooleanVariable) variables.getVariable(curName)).setValue(false);
			}
		}
		// set the corrensponding event var of the input event to TRUE 
		((BooleanVariable) variables.getVariable(currentEvent.getName())).setValue(true);
		
		// get input data values
		getDataInputs(currentEvent);
		
		try
		{
			interpreter.set("serviceFB", this);
			interpreter.set("serviceState", serviceState);
			interpreter.set("serviceEvent", currentEvent);
			interpreter.set("serviceVariables", variables);
			
			// evaluate the serviceScript
			Reader serviceScriptReader = new FileReader(serviceScript);
			interpreter.eval(serviceScriptReader);
			
			serviceState = interpreter.get("serviceState");
			variables = (Variables) interpreter.get("serviceVariables");
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}

	public void setVariableValue(String name, Object value)
	{
		Variable var = variables.getVariable(name);

		if ( var == null)
		{
			System.err.println("ServiceFBInstance.setVariableValue(): No such variable " + name);
			System.exit(1);
		}
		
		if(var instanceof StringVariable)
		{
			((StringVariable) variables.getVariable(name)).setValue(((String) value));
		}
		else if(var instanceof IntegerVariable)
		{
			((IntegerVariable) variables.getVariable(name)).setValue(((Integer) value).intValue());
		}
		else if(var instanceof DoubleVariable)
		{
			((DoubleVariable) variables.getVariable(name)).setValue(((Double) value).doubleValue());
		}
		else if(var instanceof FloatVariable)
		{
			((FloatVariable) variables.getVariable(name)).setValue(((Float) value).floatValue());
		}
		else if(var instanceof BooleanVariable)
		{
			((BooleanVariable) variables.getVariable(name)).setValue(((Boolean) value).booleanValue());
		}
	}
	
	public Object getServiceState()
	{
		return serviceState;
	}
	
}
