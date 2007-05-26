//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.automata.IO
//# CLASS:   ProjectBuildFromWaters
//###########################################################################
//# $Id: ProjectBuildFromWaters.java,v 1.21 2007-05-26 11:29:22 robi Exp $
//###########################################################################

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

import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.des.*;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.*;


/**
 * A converter that translates the WATERS {@link ProductDESProxy}
 * interfaces into Supremica's {@link Project} classes.
 *
 * @author Knut &Aring;kesson
 */ 
public class ProjectBuildFromWaters
{

    //#######################################################################
    //# Constructors
	/**
	 * Creates a WATERS-to-Supremica converter using a default project
	 * factory.
	 * @param  manager  The document manager used when compiling Waters
	 *                  modules. This argument must be provided and reference
	 *                  a document manager that is properly initialised to
	 *                  load modules. To support proper caching, the
	 *                  document manager should be shared throughout the
	 *                  application.
	 */
    public ProjectBuildFromWaters(final DocumentManager manager)
    {
        this(manager, new DefaultProjectFactory());
    }

	/**
	 * Creates a WATERS-to-Supremica converter.
	 * @param  manager  The document manager used when compiling WATERS
	 *                  modules. This argument must be provided and reference
	 *                  a document manager that is properly initialised to
	 *                  load modules. To support proper caching, the
	 *                  document manager should be shared throughout the
	 *                  application.
	 * @param  factory  The project factory used to create the Supremica
	 *                  automata.
	 */
    public ProjectBuildFromWaters(final DocumentManager manager,
								  final ProjectFactory factory)
    {
		mDocumentManager = manager;
        mProjectFactory = factory;
    }


    //#######################################################################
    //# Invocation
	/**
	 * Converts a WATERS module to a Supremica project.
	 * This method uses a compiler to translate and instantiate the module,
	 * and then converts the resulting product DES.
	 * @param  module   The WATERS module to be converted.
	 * @return The module in Supremica form.
	 * @throws EvalException to indicate that compilation of the module
	 *                  has failed.
	 */
    public Project build(ModuleProxy module)
		throws EvalException
    {
		if (module == null) {
			throw new NullPointerException("argument must be non null");
		}
		final ProductDESProxyFactory factory =
            ProductDESElementFactory.getInstance();
		ModuleCompiler compiler =
			new ModuleCompiler(mDocumentManager, factory, module);
		return build(compiler.compile());
    }

	/**
	 * Converts a WATERS product DES to a Supremica project.
	 * @param  des      The WATERS product DES to be converted.
	 * @return The product DES in Supremica form.
	 * @throws EvalException to indicate that the model could not be
	 *                  converted due to WATERS features not supported
	 *                  by Supremica.
	 */
 	public Project build(final ProductDESProxy des)
		throws EvalException
    {
        final Project currProject = mProjectFactory.getProject();
        currProject.setName(des.getName());
        for (final AutomatonProxy aut : des.getAutomata()) {
			final Automaton supaut = build(aut);
            currProject.addAutomaton(supaut);
        }
        return currProject;
	}

	/**
	 * Converts a WATERS automaton to a Supremica automaton.
	 * @param  aut      The WATERS automaton to be converted.
	 * @return The automaton in Supremica form.
	 * @throws EvalException to indicate that the model could not be
	 *                  converted due to WATERS features not supported
	 *                  by Supremica.
	 */
	public Automaton build(final AutomatonProxy aut)
		throws EvalException
	{
		final Automaton supaut = new Automaton();
		supaut.setCorrespondingAutomatonProxy(aut);
		supaut.setName(aut.getName());

		//System.err.println("Automaton: " + aut.getName());
		supaut.setType(AutomatonType.toType(aut.getKind()));

		// Termination event
		EventProxy term = null;
		boolean multicolored = false;

		// Create states
		for (final StateProxy currWatersState : aut.getStates()) {
			State currSupremicaState = new State(currWatersState.getName());
			// Set attributes
			// Initial?
			currSupremicaState.setInitial(currWatersState.isInitial());
			// Find marked status (only one type of marking here!!!)
			for (final EventProxy event : currWatersState.getPropositions()) {
				if (event.getKind() == EventKind.PROPOSITION) {
					if (event.getName().equals
						  (EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
						currSupremicaState.setForbidden(true);
					} else {
						if (!multicolored && (term != null) &&
							!event.equals(term)) {
							multicolored = true;
						}
						term = event;
						currSupremicaState.setAccepting(true);
					}
					// A state CAN be both forbidden and accepting, both
					// must be acknowledged (but forbidden is usually more
					// important)
					//break;
				}
			}

			// Did we find multiple colours?
			if (multicolored) {
				throw new EvalException
					("Multiple propositions are not yet supported!");
			}
			// Add to automaton
			supaut.addState(currSupremicaState);
		}

		Alphabet currSupremicaAlphabet = supaut.getAlphabet();
		// Create the alphabet
		for (final EventProxy currWatersEvent : aut.getEvents()) {
			if (currWatersEvent.getKind() != EventKind.PROPOSITION) {
				LabeledEvent currSupremicaEvent =
					new LabeledEvent(currWatersEvent);
				currSupremicaAlphabet.addEvent(currSupremicaEvent);
			}
		}

		// Create transitions
		for (final TransitionProxy currWatersTransition :
				 aut.getTransitions()) {
			StateProxy watersSourceState = currWatersTransition.getSource();
			StateProxy watersTargetState = currWatersTransition.getTarget();
			EventProxy watersEvent = currWatersTransition.getEvent();
			State supremicaSourceState =
				supaut.getStateWithName(watersSourceState.getName());
			State supremicaTargetState =
				supaut.getStateWithName(watersTargetState.getName());
			LabeledEvent supremicaEvent =
				currSupremicaAlphabet.getEvent(watersEvent.getName());
			Arc currSupremicaArc = new Arc(supremicaSourceState,
										   supremicaTargetState,
										   supremicaEvent);
			supaut.addArc(currSupremicaArc);
		}

		return supaut;
    }


    //#######################################################################
    //# Data Members
    private final ProjectFactory mProjectFactory;
	private final DocumentManager mDocumentManager;

}

