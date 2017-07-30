//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.flexfact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorStep;

import org.supremica.gui.ide.IDE;

/**
 * Thread to read what comes in from Flexfact
 * @author lkh12
 */
public class Read implements Runnable{

	Socket client;
	BufferedReader in;
	Simulation sim;
	boolean isLocal;

	/**
	 * Constructor to read from the input stream
	 * @param _client - The socket to read from
	 */
	public Read(final Socket _client, final boolean _isLocal, final Simulation _sim) {
		client = _client;
		isLocal = _isLocal;
		sim = _sim;
		try {
			in = new BufferedReader(
			        new InputStreamReader(client.getInputStream()));
		} catch (final IOException e) {}
	}

	/**
	 * Thread execution
	 */
	@Override
	public void run() {
		String line;

		try{
			// Forever reading in lines
			while ((line = in.readLine()) != null){
				if(isLocal && !line.startsWith("%"))
					System.out.println("local: " + line);
				else if (!line.startsWith("%"))
					System.out.println("Flexfact: " + line);

				// If it gives a list of events able to be sent
				// add them to the list of capable commands.
				if(line.startsWith("<Subscribe>")){
					line = line.replaceAll("</?Subscribe>", "");
					// Fill the list with commands
					LocalServer.events = Arrays.asList(line.split(" +"));
				}
				else if(line.startsWith("<Notify>")){
				  line = line.replaceAll(" *</?Notify> *", "");
				  //TODO: Subject to change
				  if(line.contains("+"))
				    line = line.replaceAll("\\+", "_south");
				  if(line.contains("-"))
				    line = line.replaceAll("-", "_north");


				  final List<SimulatorStep> steps = sim.getEnabledSteps();
				  final Iterator<SimulatorStep> i = steps.iterator();
				  SimulatorStep e = null;
				  while(i.hasNext()){
				    final SimulatorStep eventProxy = i.next();
				    if(eventProxy.getEvent().getName().equals(line)){
				      e = eventProxy;
				    }
				  }
				  if(e != null)
				    sim.step(e);
				  else{
				    final IDE ide = sim.getModuleContainer().getIDE();
				    ide.error("Event " + line + " is not enabled.");
				  }


				}
			}
		}catch (final Exception exception){
		  try {
            client.close();
            in.close();
          } catch (final IOException exception2) {
            System.err.println("Error at Read");
            exception2.printStackTrace();
          }
		}
	}
}
