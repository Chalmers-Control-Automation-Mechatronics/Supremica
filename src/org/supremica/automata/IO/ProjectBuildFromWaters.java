//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.automata.IO
//# CLASS:   ProjectBuildFromWaters
//###########################################################################
//# $Id$
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

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.old.OldModuleCompiler;
import net.sourceforge.waters.model.des.*;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.properties.Config;


/**
 * A converter that translates the WATERS {@link ProductDESProxy}
 * interfaces into Supremica's {@link Project} classes.
 *
 * @author Knut &Aring;kesson, Robi Malik
 */
public class ProjectBuildFromWaters
{
    //# Data Members
    private final ProjectFactory mProjectFactory;
    private final DocumentManager mDocumentManager;

    private static final Logger logger =
        LoggerFactory.createLogger(ProjectBuildFromWaters.class);

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
    public Project build(final ModuleProxy module)
		throws EvalException
    {
        if (module == null) {
            throw new NullPointerException("argument must be non null");
        }
		final boolean expand = Config.EXPAND_EXTENDED_AUTOMATA.isTrue();
        final ProductDESProxyFactory factory =
            ProductDESElementFactory.getInstance();
		final ProductDESProxy des;
		if (Config.USE_OLD_COMPILER.isTrue()) {
			final OldModuleCompiler compiler =
				new OldModuleCompiler(mDocumentManager, factory, module);
			compiler.setExpandingEFATransitions(expand);
			des = compiler.compile();
		} else {
			final boolean optimize = Config.OPTIMIZING_COMPILER.isTrue();
			final boolean ealpha = Config.USE_EVENT_ALPHABET.isTrue();
			final ModuleCompiler compiler =
				new ModuleCompiler(mDocumentManager, factory, module);
			compiler.setOptimizationEnabled(optimize);
			compiler.setExpandingEFATransitions(expand);
			compiler.setUsingEventAlphabet(ealpha);
			des = compiler.compile();
		}
        return build(des);
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
        currProject.setComment(des.getComment());

        for (final AutomatonProxy aut : des.getAutomata())
        {
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
        final Automaton supaut = new Automaton(aut.getName());
        supaut.setCorrespondingAutomatonProxy(aut);

        //System.err.println("Automaton: " + aut.getName());
        supaut.setType(AutomatonType.toType(aut.getKind()));

        // Create the alphabet
        EventProxy marking = null;
        EventProxy forbidden = null;
        final Alphabet currSupremicaAlphabet = supaut.getAlphabet();
        for (final EventProxy currWatersEvent : aut.getEvents())
        {
            switch (currWatersEvent.getKind())
            {
                case PROPOSITION:
                    final String name = currWatersEvent.getName();
                    if (name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
                    {
                        forbidden = currWatersEvent;
                    }
                    else if (marking == null)
                    {
                        marking = currWatersEvent;
                    }
                    else
                    {
                        throw new EvalException
                            ("Multiple propositions are not yet supported!");
                    }
                    break;
                default:
                    final LabeledEvent currSupremicaEvent =
                        new LabeledEvent(currWatersEvent);
                    currSupremicaAlphabet.addEvent(currSupremicaEvent);
                    break;
            }
        }

        // Create states
        for (final StateProxy currWatersState : aut.getStates())
        {
            State currSupremicaState = new State(currWatersState.getName());
            // Set attributes
            // Initial?
            currSupremicaState.setInitial(currWatersState.isInitial());
            // Find marked status (only one type of marking here!!!)
            for (final EventProxy event : currWatersState.getPropositions())
            {
                if (event == marking)
                {
                    currSupremicaState.setAccepting(true);
                }
                else if (event == forbidden)
                {
                    currSupremicaState.setForbidden(true);
                }
            }
            // If the marking proposition is not in alphabet: mark all states!
            if (marking == null)
            {
                currSupremicaState.setAccepting(true);
            }
            // Add to automaton
            supaut.addState(currSupremicaState);
        }

        // Create transitions
        for (final TransitionProxy currWatersTransition :
            aut.getTransitions())
        {
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

        addCostToStates(supaut);
        addProbabilityToTransitions(supaut);

        return supaut;
    }

    /**
     * Converts a collection of WATERS events to a Supremica alphabet.
     * @param  events   The WATERS events to be converted.
     * @return An alphabet containing Supremica event labels corresponding
     *         to all the given events, except for the propositions.
     */
    public Alphabet buildAlphabet
        (final Collection<? extends EventProxy> events)
    {
        final Alphabet alphabet = new Alphabet();
        for (final EventProxy event : events)
        {
            if (event.getKind() != EventKind.PROPOSITION)
            {
                final LabeledEvent label = new LabeledEvent(event);
                alphabet.addEvent(label);
            }
        }
        return alphabet;
    }

    /**
     * Goes through the states of the supplied automaton and adds costs if the
     * code name, "cost", is found in the WATERS product DES.
     *
     * @param   aut The automaton that may need addition of cost to its states
     */
    private void addCostToStates(Automaton aut)
        throws EvalException
    {
        for (Iterator<State> stateIt = aut.iterator(); stateIt.hasNext();)
        {
            State state = stateIt.next();
            String stateName = state.getName();
            if (stateName.contains("cost") && stateName.contains("="))
            {
                int pivotIndex = stateName.indexOf("cost");
                String prefixStr = stateName.substring(0, pivotIndex);
                String suffixStr = stateName.substring(pivotIndex);
                double costValue = -1;

                // Find the first numerical value, following the 'cost'-keyword (that is our state cost)
                try
                {
                    StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(suffixStr));
                    tokenizer.parseNumbers();

                    int type = tokenizer.nextToken();
                    while (type != tokenizer.TT_EOF)
                    {
                        if (type == tokenizer.TT_NUMBER)
                        {
                            costValue = tokenizer.nval;
                        }

                        type = tokenizer.nextToken();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (costValue == -1)
                {
                    throw new EvalException("The cost, defined in state '" + stateName + "', could not be parsed");
                }
                else
                {
                    // Remove comma before the 'cost'-keyword
                    if (prefixStr.trim().endsWith(","))
                    {
                        prefixStr = prefixStr.substring(0, prefixStr.lastIndexOf(","));
                    }

                    // Find the actual cost string (note that integer cost values must be re-casted into int.
                    String costStr = "cost=" + costValue;
                    if (!suffixStr.startsWith(costStr))
                    {
                        costStr = "cost=" + (int)costValue;
                    }

                    // Remove the cost string from the suffix and construct the state name
                    suffixStr = suffixStr.substring(costStr.length());
                    state.setName(prefixStr + suffixStr);
                    // Set the state cost
                    state.setCost(costValue);
                }
            }
        }
    }

    private void addProbabilityToTransitions(Automaton aut)
    {
        ArrayList<Arc> arcsToBeRemoved = new ArrayList<Arc>();
        ArrayList<Arc> arcsToBeAdded = new ArrayList<Arc>();

        for (Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext();)
        {
            Arc arc = arcIt.next();
            String label = arc.getLabel();
            if (label.contains("prob_"))
            {
                Double percentage = null;
                try
                {
                    percentage = new Double(label.substring(label.lastIndexOf("prob_") + 5).trim());
                }
                catch (NumberFormatException e)
                {
                    logger.error("Parsing of transition named " + label + " failed.");
                }

                if (percentage != null)
                {
                    label = label.substring(0, label.indexOf("prob_")).trim();
                    if (label.endsWith("_"))
                    {
                        label = label.substring(0, label.length()-1);
                    }

                    LabeledEvent newEvent = new LabeledEvent(label);
                    Arc newArc = new Arc(arc.getSource(), arc.getTarget(), newEvent, percentage/100);
                    if (! aut.getAlphabet().contains(newEvent))
                    {
                        aut.getAlphabet().addEvent(newEvent);
                    }

                    arcsToBeAdded.add(newArc);
                    arcsToBeRemoved.add(arc);
                }
            }
        }

        for (int i=0; i<arcsToBeRemoved.size(); i++)
        {
            aut.removeArc(arcsToBeRemoved.get(i));
        }
        for (int i=0; i<arcsToBeAdded.size(); i++)
        {
            aut.addArc(arcsToBeAdded.get(i));
        }
    }
}

