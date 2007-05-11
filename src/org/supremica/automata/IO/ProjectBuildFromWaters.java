//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.automata.IO
//# CLASS:   ProjectBuildFromWaters
//###########################################################################
//# $Id: ProjectBuildFromWaters.java,v 1.19 2007-05-11 02:07:10 robi Exp $
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
import org.supremica.log.*;


public class ProjectBuildFromWaters
{

    //#######################################################################
    //# Data Members
    private final ProjectFactory mProjectFactory;
	private final DocumentManager mDocumentManager;

    private static final Logger logger =
		LoggerFactory.createLogger(ProjectBuildFromWaters.class);

    
    //#######################################################################
    //# Constructors
    public ProjectBuildFromWaters(final DocumentManager manager)
    {
        this(manager, new DefaultProjectFactory());
    }
    
    public ProjectBuildFromWaters(final DocumentManager manager,
								  final ProjectFactory factory)
    {
		mDocumentManager = manager;
        mProjectFactory = factory;
    }
    

    //#######################################################################
    //# Invocation
    public Project build(final ModuleProxy module)
		throws EvalException
    {
        if (module == null) {
            throw new NullPointerException("Argument must be non-null!");
        }
        final ProductDESProxyFactory desfactory =
            ProductDESElementFactory.getInstance();
        final ModuleCompiler compiler =
            new ModuleCompiler(mDocumentManager, desfactory, module);
		final ProductDESProxy des = compiler.compile();
        return build(des);
	}

	public Project build(final ProductDESProxy des)
		throws EvalException
    {
        final Project currProject = mProjectFactory.getProject();
        currProject.setName(des.getName());
        for (final AutomatonProxy currWatersAutomaton : des.getAutomata()) {
            Automaton currSupremicaAutomaton = new Automaton();
            currSupremicaAutomaton.setCorrespondingAutomatonProxy(currWatersAutomaton);
            currSupremicaAutomaton.setName(currWatersAutomaton.getName());
            
            //System.err.println("Automaton: " + currWatersAutomaton.getName());
            currSupremicaAutomaton.setType(AutomatonType.toType(currWatersAutomaton.getKind()));
            
            // Termination event
            EventProxy term = null;
            boolean multicolored = false;
            
            // Create states
            for (final StateProxy currWatersState :
					 currWatersAutomaton.getStates()) {
                //System.err.println("State: " + currWatersState.getName());
                
                State currSupremicaState = new State(currWatersState.getName());
                
                // Set attributes
                // Initial?
                currSupremicaState.setInitial(currWatersState.isInitial());
                // Find marked status (only one type of marking here!!!)
                for (final EventProxy event :
						 currWatersState.getPropositions()) {
                    if (event.getKind() == EventKind.PROPOSITION)
                    {
                        if (event.getName().equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
                        {
                            currSupremicaState.setForbidden(true);
                        }
                        else
                        {
                            if (!multicolored && (term != null) && !event.equals(term))
                            {
                                multicolored = true;
                            }
                            term = event;
                            currSupremicaState.setAccepting(true);
                        }
                        // A state CAN be both forbidden and accepting, both must be acknowledged 
                        // (but forbidden is usually more important)
                        //break;
                    }
                }
                
                // Did we find multiple colors?
                if (multicolored)
                {
                    //Print warning! Color disappears in conversion!
                    throw new EvalException("Multiple propositions are not yet supported!");
                    //System.out.println("Waters model had multicolored marking, Supremica model treats all markings as the same color.");
                }
                
                // Add to automaton
                currSupremicaAutomaton.addState(currSupremicaState);
            }
            
            Alphabet currSupremicaAlphabet = currSupremicaAutomaton.getAlphabet();
            
            // Create the alphabet
            for (final EventProxy currWatersEvent :
					 currWatersAutomaton.getEvents()) {
                if (currWatersEvent.getKind() != EventKind.PROPOSITION)
                {
                    LabeledEvent currSupremicaEvent = new LabeledEvent(currWatersEvent);
                    currSupremicaAlphabet.addEvent(currSupremicaEvent);
                }
            }
            
            // Create transitions
            for (final TransitionProxy currWatersTransition :
					 currWatersAutomaton.getTransitions()) {
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

