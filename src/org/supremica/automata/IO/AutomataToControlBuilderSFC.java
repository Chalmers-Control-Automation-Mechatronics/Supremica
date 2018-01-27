
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
package org.supremica.automata.IO;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.EnumerateStates;

public class AutomataToControlBuilderSFC
    implements AutomataSerializer
{
    private static Logger logger = LogManager.getLogger(AutomataToControlBuilderSFC.class);

    protected ControlBuilderHelper theHelper = null;
    protected Project theProject;
    protected boolean debugMode = false;
    protected int transitionCounter = 0;
    protected int eventMonitorCounter = 0;
    protected int automatonCounter = 1;

    public AutomataToControlBuilderSFC(final Project theProject)
    {
        this(theProject, ControlBuilderHelper.getInstance());
    }

    public AutomataToControlBuilderSFC(final Project theProject, final IEC61131Helper theHelper)
    {
        this.theProject = theProject;
        if (theHelper instanceof ControlBuilderHelper)
        {
            this.theHelper = (ControlBuilderHelper)theHelper;
        }
        else
        {
            logger.error("Helper must be of type ControlBuilderHelper");
        }
    }

    @Override
    public void serialize(final String fileName)
    {    // Empty
    }

    @Override
    public void serialize(final PrintWriter pw)
    {    // Empty
    }

    public void serializeApp(final File theFile, final String filename)
    {
        try
        {
            final FileWriter theWriter = new FileWriter(theFile);
            final PrintWriter thePrintWriter = new PrintWriter(theWriter);
            final String theFileName = theFile.getName();

            serializeApp(thePrintWriter, theFileName.substring(0, theFileName.length() - 4));
            thePrintWriter.close();
        }
        catch (final Exception ex)
        {
            logger.error("Exception while generating ControlBuilder code. " + ex);
            logger.debug(ex.getStackTrace());
        }
    }

    public void serializeApp(final PrintWriter pw, final String filename)
    {

        // Start of file header
        //Date theDate = new Date();

        // Should perhaps get current date and time, but how do I format it?
        //logger.info(theDate.toString());
        pw.println("HEADER SyntaxVersion_ '3.1' ChangedDate_ '2002-01-25-22:20:41.631'");
        pw.println("OfficialDate_ '2002-01-25-22:20:41.631'");
        pw.println("ProductVersion_ '2.2-0'");
        pw.println("FileName_ ''");
        pw.println("FileHistory_");
        pw.println("(* This source code unit was created 2002-01-25 22:20 by Supremica. *)");
        pw.println("ENDDEF");

        // End of file header
        // Start of Program invocation
        pw.println(filename);
        pw.println("Invocation ( 0.0 , 0.0 , 0.0 , 1.0 , 1.0 )");
        pw.println(": ROOT_MODULE");

        // Use generic Program1 for now
        pw.println("PROGRAM Program1 : SINGLE_PROGRAM");

        // Start of variable declarations
        pw.println("VAR");

        Alphabet unionAlphabet = null;

        try
        {
            unionAlphabet = AlphabetHelpers.getUnionAlphabet(theProject);
        }
        catch (final Exception ex)
        {
            logger.error("Failed getting union of alphabets of the selected automata. Code generation aborted. " + ex);
            logger.debug(ex.getStackTrace());

            return;
        }

        // . is not allowed in simple variable names, replaced with _
        // #"@|*: Max identfier length (variable, step name etc) = 32.
        for (final Iterator<LabeledEvent> alphaIt = unionAlphabet.iterator(); alphaIt.hasNext(); )
        {
            final LabeledEvent currEvent = alphaIt.next();

            if (currEvent.getLabel().length() > 32)
            {
                logger.warn("Event label " + currEvent.getLabel() + " too long. ControlBuilder's maximum identifier length is 32. CB will truncate, duplicates possible. (Please rename event label yourself.)");
            }

            pw.println(currEvent.getLabel().replace('.', '_') + " : bool;");
        }

        pw.println("END_VAR\n");

        // End of variable declarations.
        // Here comes the automata, the tricky part.
        automatonConverter(theProject, pw);

        // Event Monitors should be generated here.
        generateEventMonitors(theProject, pw);

        // End of Program code
        pw.println("END_PROGRAM;\n");
        pw.println("ModuleDef");
        pw.println("ClippingBounds := ( -10.0 , -10.0 ) ( 10.0 , 10.0 )");
        pw.println("ZoomLimits := 0.0 0.01\n");

        // End of Module definition
        pw.println("END_MODULE");
    }

    public void serializePrj(final File theFile, final String filename)
    throws Exception
    {
        final PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));

        serializePrj(theWriter, filename);
        theWriter.close();
    }

    public void serializePrj(final PrintWriter pw, final String filename)
    {
        pw.println("'2002-01-11-16:24:38.775'");
        pw.println("Header");
        pw.println(" ( SyntaxVersion '3.0'");
        pw.println("   SavedDate '2002-01-11-16:47:35.825'");
        pw.println("   ChangedDate '2002-01-11-16:24:38.775'");
        pw.println("   FileName '" + filename + "'\n\n  )");
        pw.println("FileUnits");
        pw.println(" ( Application");
        pw.println("    ( Name '" + filename + "'");
        pw.println("      Directory '' ) )");
        pw.println("ControlSystem");
        pw.println(" ( Name\n Directory '' )");
        pw.println("ColorTable");
        pw.println(" ( ColorModel HLS\n )");
    }

    protected void automatonConverter(final Project theProject, final PrintWriter pw)
    {
        final EnumerateStates enumer = new EnumerateStates(theProject, "q");

        enumer.execute();

        for (final Iterator<Automaton> automataIt = theProject.iterator();
        automataIt.hasNext(); )
        {
            // Each automaton is translated into a ControlBuilder Sequence.
            // A sequence has the following structure. Step - Transition - Step - Transition ...
            // A step may be followed by an ALTERNATIVSEQuence which has ALTERNATIVEBRANCHes.
            // This is the case if there is more than one transition from a state.
            // The difficulty is to know when the alternative branches merge, and if they do it the "ControlBuilder way".
            // A transition may be followed by a PARALLELSEQuence which has PARALLELBRANCHes.
            // This cannot happen for an automaton.
            final Automaton aut = automataIt.next();

            aut.clearVisitedStates();

            transitionCounter = 1;

            if (aut.getName().length() > theHelper.getIdentifierLengthLimit())
            {
                logger.warn("The name of automaton " + aut.getName() + theHelper.getIdentifierLengthErrorMessage() + automatonCounter);
                aut.setName("Automaton_" + automatonCounter++);
            }

            // If there is _no_ ordinary, that is, non-fork, arc to the first step in drawing order it is an OPENSEQUENCE.
            // Is this reaaly correct? Won't check that for now ...
            // OPENSEQUENCE might not be supported in ControlBuilder
            // COORD must be same for all sequences? Should probably be obsoleted.
            final State initState = aut.getInitialState();

            if (initState.nbrOfIncomingArcs() > 0)
            {
                pw.println("SEQUENCE " + aut.getName().replace('.', '_') + theHelper.getSequenceControlString() + theHelper.getCoord());
            }
            else
            {
                pw.println("OPENSEQUENCE " + aut.getName().replace('.', '_') + theHelper.getSequenceControlString() + theHelper.getCoord());
            }

            printSequence(aut, initState, pw);
            aut.clearVisitedStates();

            if (initState.nbrOfIncomingArcs() > 0)
            {
                pw.println("ENDSEQUENCE\n\n");
            }
            else
            {
                pw.println("ENDOPENSEQUENCE\n\n");
            }
        }    // End of automata conversion
    }

    protected void generateEventMonitors(final Project theProject, final PrintWriter pw)
    {

        // Step 1. Get alphabet
        Alphabet unionAlphabet = null;

        try
        {
            unionAlphabet = AlphabetHelpers.getUnionAlphabet(theProject);
        }
        catch (final Exception ex)
        {
            logger.error("Failed getting union of alphabets of the selected automata. Code generation aborted. " + ex);
            logger.debug(ex.getStackTrace());

            return;
        }

        final Alphabet testAlphabet = new Alphabet(unionAlphabet);

        // Step 2. Pick an event
        for (final Iterator<LabeledEvent> alphaIt = unionAlphabet.iterator(); alphaIt.hasNext(); )
        {
            final LabeledEvent theEvent = alphaIt.next();

            if (testAlphabet.contains(theEvent.getLabel()))
            {

                // Step 3. Compute ExtendedConflict(event)
                logger.debug(theEvent.getLabel());

                final Alphabet extConfAlphabet = extendedConflict(theProject, theEvent, testAlphabet);

                testAlphabet.minus(extConfAlphabet);
                logger.debug(Integer.toString(testAlphabet.size()));

                                /* Step 4. Compute EventMonitor()
                                 We must take care of the controllability of the events in this
                                 step. Only controllable events should be generated. The uncontrollable
                                 ones should disable the generation of the controllable. */
                printEventMonitor(theProject, extConfAlphabet, pw);
            }
        }    // Step 5. Terminate if event set exhausted
    }

    protected Alphabet extendedConflict(final Project theProject, final LabeledEvent theEvent, final Alphabet iteratorAlphabet)
    {

        // Step 1. Initialise. C = {theEvent}, D = empty.
        final Alphabet theExtConfAlphabet = new Alphabet();
        final Alphabet testAlphabet = new Alphabet();

        try
        {
            theExtConfAlphabet.addEvent(theEvent);
        }
        catch (final Exception ex)
        {

            // This should not happen since theExtConfAlphabet is empty.
            logger.error("Failed adding event when computing extended conflict. Code generation erroneous. " + ex);
            logger.debug(ex.getStackTrace());

            return theExtConfAlphabet;
        }

        boolean ready = false;

        while (!ready)
        {

            // Step 2. Pick e in C \ D.
            for (final Iterator<LabeledEvent> alphaIt = iteratorAlphabet.iterator();
            alphaIt.hasNext(); )
            {
                final LabeledEvent confEvent = alphaIt.next();

                if (theExtConfAlphabet.contains(confEvent.getLabel()) &&!testAlphabet.contains(confEvent.getLabel()))
                {

                    // Step 3. Let C = C + Conflict(e), D = D + {e}.
                    final Alphabet conflictAlphabet = computeConflict(theProject, confEvent);

                    theExtConfAlphabet.union(conflictAlphabet);

                    try
                    {
                        testAlphabet.addEvent(confEvent);
                    }
                    catch (final Exception ex)
                    {

                        // This should not happen since testAlphabet didn't contain the event.
                        logger.error("Failed adding event when computing extended conflict. Code generation erroneous " + ex);
                        logger.debug(ex.getStackTrace());

                        return theExtConfAlphabet;
                    }
                }
            }

            // Step 4. If C = D return, else repeat from step 2.
            if (theExtConfAlphabet.size() == testAlphabet.size())
            {
                ready = true;

                logger.debug("Finished computing extended conflict");
            }
        }

        return theExtConfAlphabet;
    }

    protected Alphabet computeConflict(final Project theProject, final LabeledEvent theEvent)
    {
        final Alphabet confAlphabet = new Alphabet();

        try
        {
            confAlphabet.addEvent(theEvent);
        }
        catch (final Exception ex)
        {

            // This should not happen since confAlphabet is empty.
            logger.error("Failed adding event when computing conflict. " + ex);
            logger.debug(ex.getStackTrace());

            return confAlphabet;
        }

        // Iterate over the automata, if the event is present we must find all states
        // that have transitions with this event. For each state, an iterator over the
        // outgoing arcs find such events.
        for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
        {
            final Automaton aut = autIt.next();
            final Alphabet theAlphabet = aut.getAlphabet();

            logger.debug(aut.getName().replace('.', '_'));

            if (theAlphabet.contains(theEvent.getLabel()))
            {
                logger.debug("The event " + theEvent.getLabel() + " exsits in the automaton " + aut.getName().replace('.', '_'));

                // The event exists in this automaton. What arcs?
                for (final Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
                {
                    final Arc anArc = arcIt.next();

                    try
                    {
                        final LabeledEvent arcEvent = anArc.getEvent();    // (LabeledEvent) aut.getEvent(anArc.getEventId());

                        if (arcEvent.getLabel().equals(theEvent.getLabel()))
                        {
                            logger.debug("Event " + theEvent.getLabel() + " labels arc");

                            // The event labels this arc. Get conflicting arcs.
                            final State sourceState = anArc.getFromState();

                            if (!sourceState.isVisited())
                            {

                                // It is only necessary to get the conflicting transitions for this state once?
                                sourceState.setVisited(true);

                                for (final Iterator<Arc> outgoingIt = sourceState.outgoingArcsIterator();
                                outgoingIt.hasNext(); )
                                {
                                    final Arc currArc = outgoingIt.next();

                                    try
                                    {
                                        final LabeledEvent currArcEvent = currArc.getEvent();    // (LabeledEvent) aut.getEvent(currArc.getEventId());
                                        final Alphabet dummyAlphabet = new Alphabet();

                                        try
                                        {
                                            dummyAlphabet.addEvent(currArcEvent);
                                            logger.debug("Event " + currArcEvent.getLabel() + " is in conflict with " + theEvent.getLabel());
                                            confAlphabet.union(dummyAlphabet);
                                        }
                                        catch (final Exception ex)
                                        {

                                            // This should not happen since dummyAlphabet is empty.
                                            logger.error("Failed adding event when computing conflict. " + ex);
                                            logger.debug(ex.getStackTrace());

                                            return confAlphabet;
                                        }
                                    }
                                    catch (final Exception ex)
                                    {

                                        // This should not happen since the event exists in the automaton.
                                        logger.error("Failed getting event label. Code generation erroneous. " + ex);
                                        logger.debug(ex.getStackTrace());

                                        return confAlphabet;
                                    }
                                }
                            }
                        }
                    }
                    catch (final Exception ex)
                    {

                        // This should not happen since the event exists in the automaton.
                        logger.error("Failed getting event label. Code generation erroneous. " + ex);
                        logger.debug(ex.getStackTrace());

                        return confAlphabet;
                    }
                }
            }
        }

        return confAlphabet;
    }

    protected void printEventMonitor(final Project theProject, final Alphabet theAlphabet, final PrintWriter pw)
    {

                /* Step 1. Initialise. Create initial step
                 Now we have to be careful. We should only create an event monitor
                 in the case that there are controllable events in theAlphabet. Perhaps
                 it is better to handle this in generateEventMonitor, but I think it is
                 easier to do it here. */
        if (theAlphabet.nbrOfControllableEvents() == 0)
        {

            // Nothing to generate.
            return;
        }

                /* OK, there is at least one controllable event in theAlphabet.
                 We have something to generate. */
        int stepCounter = 0;
        boolean firstEvent = true;

        transitionCounter = 1;

        // eventMonitorCounter++;
        pw.println("SEQUENCE EventMonitor_" + ++eventMonitorCounter + theHelper.getCoord());
        pw.println("SEQINITSTEP EM" + eventMonitorCounter + "_" + stepCounter++);

        /* Step 2. For each controllable event e in theAlphabet */
        if (theAlphabet.nbrOfControllableEvents() > 1)
        {
            pw.println("ALTERNATIVESEQ");
        }

        for (final Iterator<LabeledEvent> eventIt = theAlphabet.controllableEventIterator();
        eventIt.hasNext(); )
        {
            final LabeledEvent currEvent = eventIt.next();

            if (firstEvent)
            {
                firstEvent = false;
            }
            else
            {
                pw.println("ALTERNATIVEBRANCH");
            }

                        /* (a) Create transition t with t.C = preset() && NOT uncontrollableEvents
                         That is, a controllable event should not be generated when an uncontrollable
                         event in extended conflict has occurred. */
            String transitionCondition = computeGenerationCondition(theProject, theAlphabet, currEvent);

            transitionCounter = printEventMonitorTransition(transitionCounter, eventMonitorCounter, transitionCondition, pw);

            // (b) Create step with action e
            pw.println("SEQSTEP EM" + eventMonitorCounter + "_" + stepCounter++);
            printEventMonitorAction(currEvent, pw);

            // (c) Create transition t' with t'.C = not preset()
            transitionCondition = computeCeaseCondition(theProject, currEvent);
            transitionCounter = printEventMonitorTransition(transitionCounter, eventMonitorCounter, transitionCondition, pw);
        }

        if (theAlphabet.nbrOfControllableEvents() > 1)
        {
            pw.println("ENDALTERNATIVE");
        }

        pw.println("ENDSEQUENCE\n\n");
        logger.debug("Printing Event Monitor");
    }

    protected int printEventMonitorTransition(int transitionCounter, final int eventMonitorCounter, final String transitionCondition, final PrintWriter pw)
    {
        pw.println("SEQTRANSITION EM" + eventMonitorCounter + "_Tr" + transitionCounter++ + theHelper.getTransitionConditionPrefix() + transitionCondition + theHelper.getTransitionConditionSuffix());

        return transitionCounter;
    }

    protected void printEventMonitorAction(final LabeledEvent theEvent, final PrintWriter pw)
    {
        pw.println(theHelper.getActionP1Prefix() + theEvent.getLabel().replace('.', '_') + theHelper.getAssignmentOperator() + "True;" + theHelper.getActionP1Suffix());
        pw.println(theHelper.getActionP0Prefix() + theEvent.getLabel().replace('.', '_') + theHelper.getAssignmentOperator() + "False;" + theHelper.getActionP0Suffix());
    }

    protected String computeGenerationCondition(final Project theProject, final Alphabet theExtConfAlphabet, final LabeledEvent theEvent)
    {
        final StringBuilder theCondition = new StringBuilder();
        boolean firstAutomaton = true;
        boolean nextAutomaton = false;

                /* We create the uncontrollable disablement condition first.
                   Only the uc events in conflict with theEvent are relevant here. */
        if (theExtConfAlphabet.nbrOfUncontrollableEvents() > 0)
        {
            final String theUcCondition = ucDisablementCondition(theExtConfAlphabet);

            theCondition.append(theUcCondition);
        }

        /* And then the actual generation condition. */
        for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
        {
            final Automaton aut = autIt.next();
            final Alphabet theAlphabet = aut.getAlphabet();

            if (theAlphabet.contains(theEvent.getLabel()))
            {

                // The event exists in this automaton
                logger.debug("The event " + theEvent.getLabel() + " exists in " + aut.getName().replace('.', '_'));

                boolean stateFound = false;
                boolean firstState = true;

                for (final Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
                {
                    final Arc anArc = arcIt.next();

                    try
                    {
                        final LabeledEvent arcEvent = anArc.getEvent();    // (LabeledEvent) aut.getEvent(anArc.getEventId());

                        if (arcEvent.getLabel().equals(theEvent.getLabel()))
                        {

                            // The event labels this arc. Get preset (a singleton!).
                            logger.debug("The event labels arc");

                            stateFound = true;

                            final State sourceState = anArc.getFromState();

                            if (firstAutomaton)
                            {
                                firstAutomaton = false;
                            }
                            else if (nextAutomaton)
                            {
                                nextAutomaton = false;

                                theCondition.append(" AND ");
                            }

                            if (firstState)
                            {
                                firstState = false;

                                theCondition.append("(" + aut.getName().replace('.', '_') + "__" + sourceState.getName() + ".X");
                                logger.debug("Current transition condition: " + theCondition);
                            }
                            else
                            {
                                theCondition.append(" OR " + aut.getName().replace('.', '_') + "__" + sourceState.getName() + ".X");
                            }
                        }
                    }
                    catch (final Exception ex)
                    {

                        // This should not happen since the event exists in the automaton.
                        logger.error("Failed getting event label. Code generation erroneous. " + ex);
                        logger.debug(ex.getStackTrace());

                        return theCondition.toString();
                    }
                }

                if (!stateFound)
                {
                    return "False";
                }
                else
                {
                    theCondition.append(")");
                }

                nextAutomaton = true;
            }
        }

        return theCondition.toString();
    }

    protected String ucDisablementCondition(final Alphabet theAlphabet)
    {

                /* We have to compute a conflict set first. Note that this is,
                   in fact, a dynamic property. It may well be the case that
                   statically the rising and falling edge of an input signal
                   is in conflict with an output event. Note in particular that
                   we cannot use all uncontrollable events in the extended conflict.
                   Nevertheless, let's not bother about this now ... */
        final StringBuilder theCondition = new StringBuilder();
        boolean firstUcEvent = true;

        theCondition.append("NOT (");

        for (final Iterator<LabeledEvent> ucEventIt = theAlphabet.uncontrollableEventIterator();
        ucEventIt.hasNext(); )
        {
            final LabeledEvent theUcEvent = ucEventIt.next();

            if (firstUcEvent)
            {
                firstUcEvent = false;
            }
            else
            {
                theCondition.append(" OR ");
            }

            theCondition.append(theUcEvent.getLabel().replace('.', '_'));
        }

        theCondition.append(") AND ");

        return theCondition.toString();
    }

    protected String computeCeaseCondition(final Project theProject, final LabeledEvent theEvent)
    {
        final StringBuilder theCondition = new StringBuilder();
        boolean firstAutomaton = true;
        boolean nextAutomaton = false;

        for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
        {
            final Automaton aut = autIt.next();
            final Alphabet theAlphabet = aut.getAlphabet();

            if (theAlphabet.contains(theEvent.getLabel()))
            {

                // The event exists in this automaton
                logger.debug("The event " + theEvent.getLabel() + " exists in " + aut.getName().replace('.', '_'));

                boolean stateFound = false;
                boolean firstState = true;

                for (final Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext(); )
                {
                    final Arc anArc = arcIt.next();

                    try
                    {
                        final LabeledEvent arcEvent = anArc.getEvent();    // (LabeledEvent) aut.getEvent(anArc.getEventId());

                        if (arcEvent.getLabel().equals(theEvent.getLabel()))
                        {

                            // The event labels this arc. Get preset (a singleton!).
                            logger.debug("The event labels arc");

                            stateFound = true;

                            final State sourceState = anArc.getFromState();

                            if (firstAutomaton)
                            {
                                firstAutomaton = false;

                                theCondition.append("NOT (");
                            }
                            else if (nextAutomaton)
                            {
                                nextAutomaton = false;

                                theCondition.append(" OR ");
                            }

                            if (firstState)
                            {
                                firstState = false;

                                theCondition.append("(" + aut.getName().replace('.', '_') + "__" + sourceState.getName() + ".X");
                                logger.debug("Current transition condition: " + theCondition);
                            }
                            else
                            {
                                theCondition.append(" AND " + aut.getName().replace('.', '_') + "__" + sourceState.getName() + ".X");
                            }
                        }
                    }
                    catch (final Exception ex)
                    {

                        // This should not happen since the event exists in the automaton.
                        logger.error("Failed getting event label. Code generation erroneous. " + ex);
                        logger.debug(ex.getStackTrace());

                        return theCondition.toString();
                    }
                }

                if (!stateFound)
                {
                    return "False";
                }
                else
                {
                    theCondition.append(")");
                }

                nextAutomaton = true;
            }
        }

        theCondition.append(")");

        return theCondition.toString();
    }

    protected void printSequence(final Automaton theAutomaton, final State theState, final PrintWriter pw)
    {
        printStep(theAutomaton, theState, pw);
        theState.setVisited(true);

        int endAlternativeLevel = 0;
        boolean alternativeEnded = false;

        if (theState.nbrOfOutgoingArcs() > 1)
        {
            pw.println("ALTERNATIVESEQ");

            endAlternativeLevel = theState.nbrOfOutgoingArcs();
        }

        boolean firstArc = true;

        for (final Iterator<Arc> outgoingArcsIt = theState.outgoingArcsIterator();
        outgoingArcsIt.hasNext(); )
        {
            if (firstArc)
            {
                firstArc = false;
            }
            else
            {
                pw.println("ALTERNATIVEBRANCH");

                endAlternativeLevel--;

                // logger.debug("endAlternativeLevel = " + endAlternativeLevel);
            }

            final Arc arc = outgoingArcsIt.next();

            printTransition(theAutomaton, arc, pw);

            final State nextState = arc.getToState();

            if (!nextState.isVisited())
            {
                printSequence(theAutomaton, nextState, pw);
            }
            else if (!nextState.isInitial())
            {
                printFork(theAutomaton, nextState, pw);
            }
            else if (endAlternativeLevel == 1)
            {
                pw.println("ENDALTERNATIVE");    // End of this subsequence

                alternativeEnded = true;

                // logger.debug("EndAlternative");
            }
        }

        if ((endAlternativeLevel == 1) &&!alternativeEnded)
        {
            pw.println("ENDALTERNATIVE");    // End of this subsequence

            // logger.debug("EndAlternative");
        }
    }

    protected void printStep(final Automaton theAutomaton, final State theState, final PrintWriter pw)
    {
        if (theState.isInitial())
        {
            pw.println("SEQINITSTEP " + theAutomaton.getName().replace('.', '_') + "__" + theState.getName());
        }
        else
        {
            pw.println("SEQSTEP " + theAutomaton.getName().replace('.', '_') + "__" + theState.getName());
        }
    }

    protected void printTransition(final Automaton theAutomaton, final Arc theArc, final PrintWriter pw)
    {
        try
        {
            final LabeledEvent event = theArc.getEvent();    // theAutomaton.getEvent(theArc.getEventId());

            pw.println("SEQTRANSITION " + theAutomaton.getName().replace('.', '_') + "_Tr" + transitionCounter++ + theHelper.getTransitionConditionPrefix() + event.getLabel().replace('.', '_') + theHelper.getTransitionConditionSuffix());
        }
        catch (final Exception ex)
        {
            logger.error("Failed getting event label. Code generation aborted. " + ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    protected void printFork(final Automaton theAutomaton, final State theState, final PrintWriter pw)
    {
        pw.println("SEQFORK " + theAutomaton.getName().replace('.', '_') + "__" + theState.getName() + " SEQBREAK");
    }
}
