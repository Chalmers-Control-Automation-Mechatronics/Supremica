
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.comm.xmlrpc;

import java.util.*;
import java.io.*;

import org.supremica.gui.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import org.supremica.properties.*;

import helma.xmlrpc.*;

public class Server
{

	// singleton stuff:
	private static Server instance_ = null;
	public static void shutdown() {
		if(instance_ != null) {
			instance_.theServer.shutdown();
			instance_  = null;
		}
	}

	// -----------------------------------------------------

	private VisualProjectContainer container;
	private WebServer theServer;

	// -----------------------------------------------------
	public Server(VisualProjectContainer container, int port)
		throws Exception
	{

		this.container = container;
		instance_ = this; // this statement would be illegal in C++  :)

		String filter = SupremicaProperties.getXmlRpcFilter();
		theServer = new WebServer(port);
		if(filter.length() > 0) {
			theServer.setParanoid(true);
			theServer.acceptClient(filter);
		}

		XmlRpc.setDebug( SupremicaProperties.isXmlRpcDebugging() );

		theServer.addHandler("$default", this);

	}



		public Vector getAutomataIdentities()
		{

			Vector theIdentities = new Vector();
			Iterator autIt = container.getActiveProject().iterator();

			while (autIt.hasNext())
			{
				Object o = autIt.next();
				String currName = o.toString();
				theIdentities.add(currName);
			}

			return theIdentities;
		}


		public String getAutomata(Vector automataIdentities)
				throws XmlRpcException
		{

				// Construct an automata object
				Automata theAutomata = new Automata();

				for (int i = 0; i < automataIdentities.size(); i++)
				{
						String currName = (String) automataIdentities.get(i);
						Automaton currAutomaton;

						try
						{
								currAutomaton = container.getActiveProject().getAutomaton(currName);
						}
						catch (Exception e)
						{
								throw new XmlRpcException(0, currName + " does not exist.");
						}

						theAutomata.addAutomaton(currAutomaton);
				}

				AutomataToXml exporter = new AutomataToXml(theAutomata);
				StringWriter response = new StringWriter();
				PrintWriter pw = new PrintWriter(response);

				exporter.serialize(pw);

				return response.toString();
		}

		public void addAutomata(String automataXmlEncoding)
				throws XmlRpcException
		{
			StringBufferInputStream sbis = new StringBufferInputStream(automataXmlEncoding);
			Project project;

			try
			{
				ProjectBuildFromXml builder = new ProjectBuildFromXml();
				project = builder.build(sbis);
			}
			catch (Exception e)
			{
					throw new XmlRpcException(0, "Error while parsing automataXmlEncoding.");
			}

			Iterator autIt = project.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton) autIt.next();

				try
				{
						container.getActiveProject().addAutomaton(currAutomaton);
				}
				catch (Exception e)
				{
						throw new XmlRpcException(0, currAutomaton.getName() + " does already exist.");
				}
			}

		}


		public void removeAutomata(Vector automataIdentities)
				throws XmlRpcException
		{
				for (int i = 0; i < automataIdentities.size(); i++)
				{
						String currName = (String) automataIdentities.get(i);

						try
						{
								// container.remove(currName);
								container.getActiveProject().removeAutomaton(currName);
						}
						catch (Exception e)
						{
								throw new XmlRpcException(0, currName + " does not exist.");
						}
				}
		}

		public void synchronizeAutomata(Vector automataIdentitites, String newautomatonIdentitity)
				throws XmlRpcException {}

		public void minimizeAutomaton(String automatonIdentity, String newIdentity)
				throws XmlRpcException {}



}
