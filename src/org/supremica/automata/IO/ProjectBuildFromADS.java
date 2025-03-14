
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.DefaultProjectFactory;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.ProjectFactory;
import org.supremica.automata.State;


/**
 * Import TCT ADS files
 * From the http://se.wtb.tue.nl/sewiki/wonham/creating_and_editing_state_machines
 *
 * <PRE>
 * Controllable events are odd, uncontrollable are even
 *
 * # Generated CTCT ADS file
 *
 * pq
 *
 * State size (State set will be (0,1....,size-1)):
 * # &lt;-- Enter state size, in range 0 to 2000000, on line below.
 * 2
 *
 * Marker states:
 * # &lt;-- Enter marker states, one per line.
 * # To mark all states, enter *.
 * # If no marker states, leave line blank.
 * # End marker list with blank line.
 * 1
 *
 * Vocal states:
 * # &lt;-- Enter vocal output states, one per line.
 * # Format: State  Vocal_Output.  Vocal_Output in range 10 to 99.
 * # Example: 0 10
 * # If no vocal states, leave line blank.
 * # End vocal list with blank line.
 *
 * Transitions:
 * # &lt;-- Enter transition triple, one per line.
 * # Format: Exit_(Source)_State  Transition_Label  Entrance_(Target)_State.
 * # Transition_Label in range 0 to 999.
 * # Example: 2 0 1 (for transition labeled 0 from state 2 to state 1).
 * 0 1 1
 * 1 0 0
 *
 * Another example:
 * # CTCT ADS Template
 *
 * example
 *
 * State size (State set will be (0,1....,size-1)):
 * # &lt;-- Enter state size, in range 0 to 2000000, on line below.
 * 3
 *
 * Marker states:
 * # &lt;-- Enter marker states, one per line.
 * # To mark all states, enter *.
 * # If no marker states, leave line blank.
 * # End marker list with blank line.
 * 0
 *
 * Vocal states:
 * # &lt;-- Enter vocal output states, one per line.
 * # Format: State  Vocal_Output.  Vocal_Output in range 10 to 99.
 * # Example: 0 10
 * # If no vocal states, leave line blank.
 * # End vocal list with blank line.
 *
 * Transitions:
 * # &lt;-- Enter transition triple, one per line.
 * # Format: Exit_(Source)_State  Transition_Label  Entrance_(Target)_State.
 * # Transition_Label in range 0 to 999.
 * # Example: 2 0 1 (for transition labeled 0 from state 2 to state 1).
 * 0 0 1
 * 0 1 2
 * 1 3 2
 * 2 2 0</PRE>
 */
public class ProjectBuildFromADS
{
    private ProjectFactory theProjectFactory = null;
//	private Project currProject = null;
//	private Automaton currAutomaton = null;
    private Alphabet currAlphabet = null;
    protected String automatonName = "Imported ADS file";
//	private InputProtocol inputProtocol = InputProtocol.UnknownProtocol;
    private File thisFile = null;
    private static int READ_AUTOMATON_NAME = 1;
    private static int STATE_READ_NUMBER_OF_STATES = 2;
    private static int STATE_READ_MARKED_STATES = 3;
    private static int STATE_READ_VOCAL_STATES = 4;
    private static int STATE_READ_TRANSITIONS = 5;

    public ProjectBuildFromADS()
    {
        this.theProjectFactory = new DefaultProjectFactory();
    }

    public ProjectBuildFromADS(final ProjectFactory theProjectFactory)
    {
        this.theProjectFactory = theProjectFactory;
    }

    public Project build(final URL url)
    throws Exception
    {
        final String protocol = url.getProtocol();

        if (protocol.equals("file"))
        {
            //inputProtocol = InputProtocol.FileProtocol;

            final String fileName = url.getFile();

            thisFile = new File(fileName);
            automatonName = thisFile.getName();

            final int lastdot = automatonName.lastIndexOf(".");

            if (lastdot > 0)
            {
                automatonName = automatonName.substring(0, lastdot);
            }
        }
        else if (protocol.equals("jar"))
        {
            //inputProtocol = InputProtocol.JarProtocol;
        }
        else
        {
            //inputProtocol = InputProtocol.UnknownProtocol;

            System.err.println("Unknown protocol: " + protocol);

            return null;
        }

        final InputStream stream = url.openStream();

        return build(stream);
    }

    private Project build(final InputStream is)
    throws Exception
    {
        //System.err.println("build");
        final InputStreamReader isReader = new InputStreamReader(is);
        final BufferedReader reader = new BufferedReader(isReader);
        final Project currProject = theProjectFactory.getProject();
        final Automaton currAutomaton = new Automaton(automatonName);

        currAlphabet = currAutomaton.getAlphabet();

        currProject.addAutomaton(currAutomaton);

        int currParserState = READ_AUTOMATON_NAME;
        int numberOfStates = 0;
        String currLine = reader.readLine();

        while (currLine != null)
        {
            //System.err.println("reading: " + currLine);
            boolean skip = false;

            if (isComment(currLine))
            {
                skip = true;
            }
            else if (currLine.startsWith("State size"))
            {
                currParserState = STATE_READ_NUMBER_OF_STATES;
                skip = true;
            }
            else if (currLine.startsWith("Marker states:"))
            {
                currParserState = STATE_READ_MARKED_STATES;
                skip = true;
            }
            else if (currLine.startsWith("Vocal states:"))
            {
                currParserState = STATE_READ_VOCAL_STATES;
                skip = true;
            }
            else if (currLine.startsWith("Transitions:"))
            {
                currParserState = STATE_READ_TRANSITIONS;
                skip = true;
            }

            final StringTokenizer tokenizer = new StringTokenizer(currLine);

            while (tokenizer.hasMoreTokens() && !skip)
            {
                final String currToken = tokenizer.nextToken();

                if (currParserState == READ_AUTOMATON_NAME)
                {
                    final String automatonName = currToken;

                    if (automatonName == null)
                    {
                        System.err.println("Expected an automaton name");
                    }

                    currAutomaton.setName(automatonName);

                    currParserState = STATE_READ_NUMBER_OF_STATES;
                }
                //System.err.println("umdes: \"" + currToken + "\"");
                else if (currParserState == STATE_READ_NUMBER_OF_STATES)
                {
                    try
                    {
                        numberOfStates = Integer.parseInt(currToken);
                    }
                    catch (final NumberFormatException ex)
                    {
                        System.err.println("Expected the number of states. Read: " + currToken);

                        throw ex;
                    }

                    if (numberOfStates < 1)
                    {
                        System.err.println("The automaton must have at least one state (the initial state)");
                    }

                    // Create all states
                    for (int i = 0; i < numberOfStates; i++)
                    {
                        // Create and add the state
                        final State newState = new State(String.valueOf(i));

                        if (i==0)
                        {
                            newState.setInitial(true);
                        }

                        currAutomaton.addState(newState);
                    }

                    currParserState = STATE_READ_MARKED_STATES;
                }
                else if (currParserState == STATE_READ_MARKED_STATES)
                {
                    int currMarkedState = -1;
                    try
                    {
                        currMarkedState = Integer.parseInt(currToken);
                    }
                    catch (final NumberFormatException ex)
                    {
                        System.err.println("Expected a state number. Read: " + currToken);
                        throw ex;
                    }

                    final State theState = currAutomaton.getStateWithName(String.valueOf(currMarkedState));
                    if (theState != null)
                    {
                        theState.setAccepting(true);
                    }

                }
                else if (currParserState == STATE_READ_VOCAL_STATES)
                {
                    System.err.println("Vocal states not supported. Read: " + currToken);
                    throw new Exception("Vocal states not supported");
                }
                else if (currParserState == STATE_READ_TRANSITIONS)
                {

                    final String sourceStateName = currToken;
                    //System.err.println("source: " + sourceStateName);
                    final String eventLabel = tokenizer.nextToken();
                    //System.err.println("label: " + eventLabel);
                    final String destStateName = tokenizer.nextToken();
                    //System.err.println("dest: " + destStateName);

                    if (sourceStateName == null)
                    {
                        System.err.println("No source state name");
                    }

                    if (eventLabel == null)
                    {
                        System.err.println("No dest state name");
                    }

                    if (destStateName == null)
                    {
                        System.err.println("No dest state name");
                    }

                    int sourceStateNumber = -1;
                    int destStateNumber = -1;
                    int eventLabelNumber = -1;
                    try
                    {
                        sourceStateNumber = Integer.parseInt(sourceStateName);
                        eventLabelNumber = Integer.parseInt(eventLabel);
                        destStateNumber = Integer.parseInt(destStateName);
                    }
                    catch (final NumberFormatException ex)
                    {
                        System.err.println("Expected an integer. Read: " + currToken);
                        throw ex;
                    }

                    LabeledEvent currLabeledEvent = null;
                    if (currAlphabet.contains(eventLabel))
                    {
                        currLabeledEvent = currAlphabet.getEvent(eventLabel);
                    }
                    else
                    {
                        currLabeledEvent = new LabeledEvent(eventLabel);
                        if (eventLabelNumber % 2 == 0)
                        {
                            currLabeledEvent.setControllable(false);
                        }
                        else
                        {
                            currLabeledEvent.setControllable(true);
                        }
                        currAlphabet.addEvent(currLabeledEvent);
                    }

                    final State sourceState = currAutomaton.getStateWithName(String.valueOf(sourceStateNumber));
                    final State destState = currAutomaton.getStateWithName(String.valueOf(destStateNumber));
                    final Arc currArc = new Arc(sourceState, destState, currLabeledEvent);
                    currAutomaton.addArc(currArc);

                }

            }
            //System.err.println("readline");
            currLine = reader.readLine();
        }

        //System.err.println("return project");

        return currProject;
    }

    public boolean isComment(final String line)
    {
        return line.startsWith("#");
    }
}

