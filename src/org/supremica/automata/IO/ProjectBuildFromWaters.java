
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
package org.supremica.automata.IO;

import java.util.*;
import java.io.*;
import java.net.URL;
import org.supremica.automata.*;
import org.supremica.log.*;
import net.sourceforge.waters.model.base.DocumentManager;
//import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.des.*;


public class ProjectBuildFromWaters
{
	private static Logger logger = LoggerFactory.createLogger(ProjectBuildFromFSM.class);
	private ProjectFactory theProjectFactory = null;
	private Project currProject = null;

	public ProjectBuildFromWaters()
	{
		this.theProjectFactory = new DefaultProjectFactory();
	}

	public ProjectBuildFromWaters(ProjectFactory theProjectFactory)
	{
		this.theProjectFactory = theProjectFactory;
	}

	public Project build(ModuleProxy module)
	{
		if (module == null)
		{
			throw new IllegalArgumentException("argument must be non null");
		}
		Project currProject = theProjectFactory.getProject();
		currProject.setName(module.getName());

		DocumentManager mDocumentManager = new DocumentManager();
		ModuleCompiler compiler = new ModuleCompiler(module, mDocumentManager);

		ProductDESProxy des = null;
 		try
 		{
 			des = compiler.compile();
		}
		catch (Exception ex)
		{
			logger.error("Exception - Failed to compile: " + ex.getMessage());
		}

		if (des == null)
		{
			return currProject;
		}

		Collection theWatersAutomata = des.getAutomata();
		for (Iterator autIt = theWatersAutomata.iterator(); autIt.hasNext(); )
		{
			AutomatonProxy currWatersAutomaton = (AutomatonProxy)autIt.next();
			Automaton currSupremicaAutomaton = new Automaton();
			currSupremicaAutomaton.setCorrespondingAutomatonProxy(currWatersAutomaton);
			currSupremicaAutomaton.setName(currWatersAutomaton.getName());

			//System.err.println("Automaton: " + currWatersAutomaton.getName());
			currSupremicaAutomaton.setType(AutomatonType.toType(currWatersAutomaton.getKind()));

			// Create states
			Set currWatersStates = currWatersAutomaton.getStates();
			for (Iterator stateIt = currWatersStates.iterator(); stateIt.hasNext(); )
			{
				StateProxy currWatersState = (StateProxy)stateIt.next();

				//System.err.println("State: " + currWatersState.getName());


				State currSupremicaState = new State(currWatersState.getName());
				currSupremicaState.setInitial(currWatersState.isInitial());

				currSupremicaAutomaton.addState(currSupremicaState);
			}

			Alphabet currSupremicaAlphabet = currSupremicaAutomaton.getAlphabet();

			// Create the alphabet
			Set currWatersEvents = currWatersAutomaton.getEvents();
			for (Iterator evIt = currWatersEvents.iterator(); evIt.hasNext(); )
			{
				EventProxy currWatersEvent = (EventProxy)evIt.next();
				LabeledEvent currSupremicaEvent = new LabeledEvent(currWatersEvent);
				currSupremicaAlphabet.addEvent(currSupremicaEvent);
			}

			// Create transitions
			List currWatersTransitions = currWatersAutomaton.getTransitions();
			for (Iterator trIt = currWatersTransitions.iterator(); trIt.hasNext(); )
			{
				TransitionProxy currWatersTransition = (TransitionProxy)trIt.next();
				StateProxy watersSourceState = currWatersTransition.getSource();
				StateProxy watersTargetState = currWatersTransition.getTarget();
				EventProxy watersEvent = currWatersTransition.getEvent();

				State supremicaSourceState = currSupremicaAutomaton.getStateWithName(watersSourceState.getName());
				State supremicaTargetState = currSupremicaAutomaton.getStateWithName(watersTargetState.getName());
				LabeledEvent supremicaEvent = currSupremicaAlphabet.getEvent(watersEvent.getName());
				Arc currSupremicaArc = new Arc(supremicaSourceState, supremicaTargetState, supremicaEvent);

				currSupremicaAutomaton.addArc(currSupremicaArc);

				//LabeledEvent currSupremicaEvent = new LabeledEvent(currWatersEvent);
				//currSupremicaAlphabet.addEvent(currSupremicaEvent);
			}

			currProject.addAutomaton(currSupremicaAutomaton);

		}
		return currProject;
	}
}

