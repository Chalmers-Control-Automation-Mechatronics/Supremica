//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica Automata
//# PACKAGE: org.supremica.automata.IO
//# CLASS:   ProjectBuildFromXML
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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.DefaultProjectFactory;
import org.supremica.automata.InputProtocol;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.ProjectFactory;
import org.supremica.automata.State;
import org.supremica.automata.execution.Action;
import org.supremica.automata.execution.Actions;
import org.supremica.automata.execution.BinarySignal;
import org.supremica.automata.execution.Command;
import org.supremica.automata.execution.Condition;
import org.supremica.automata.execution.Control;
import org.supremica.automata.execution.Controls;
import org.supremica.automata.execution.EventTimer;
import org.supremica.automata.execution.Signal;
import org.supremica.automata.execution.Signals;
import org.supremica.automata.execution.Timers;
import org.supremica.util.SupremicaException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class ProjectBuildFromXML
    extends DefaultHandler
{
    private final static String automataStr = "Automata";
    private final static String automatonStr = "Automaton";
    private final static String eventsStr = "Events";
    private final static String eventStr = "Event";
    private final static String statesStr = "States";
    private final static String stateStr = "State";
    private final static String transitionsStr = "Transitions";
    private final static String transitionStr = "Transition";
    private final static String idStr = "id";
    private final static String nameStr = "name";
    private final static String initialStr = "initial";
    private final static String acceptingStr = "accepting";
    private final static String forbiddenStr = "forbidden";
    private final static String costStr = "cost";
    private final static String labelStr = "label";
    private final static String controllableStr = "controllable";
    private final static String prioritizedStr = "prioritized";
    private final static String observableStr = "observable";
    private final static String operatorIncreaseStr = "operatorIncrease";
    private final static String operatorResetStr = "operatorReset";
    private final static String immediateStr = "immediate";
    private final static String epsilonStr = "epsilon";
    private final static String projectStr = "SupremicaProject";
    @SuppressWarnings("unused")
	private final static String layoutStr = "Layout";
    private final static String statesLayoutStr = "StatesLayout";
    @SuppressWarnings("unused")
	private final static String stateLayoutStr = "StateLayout";
    @SuppressWarnings("unused")
	private final static String supremicaLayoutStr = "SupremicaLayout";
    private final static String executionStr = "Execution";
    private final static String inputSignalsStr = "InputSignals";
    private final static String outputSignalsStr = "OutputSignals";
    private final static String signalStr = "Signal";
    private final static String actionsStr = "Actions";
    private final static String actionStr = "Action";
    private final static String controlsStr = "Controls";
    private final static String controlStr = "Control";
    private final static String timersStr = "Timers";
    private final static String timerStr = "Timer";
    private final static String commandStr = "Command";
    private final static String conditionStr = "Condition";
    @SuppressWarnings("unused")
	private final static String interfacesStr = "Interfaces";
    @SuppressWarnings("unused")
	private final static String interfaceStr = "Interface";
    @SuppressWarnings("unused")
	private final static String mastersStr = "Masters";
    @SuppressWarnings("unused")
	private final static String masterStr = "Master";
    @SuppressWarnings("unused")
	private final static String slavesStr = "Slaves";
    @SuppressWarnings("unused")
	private final static String slaveStr = "Slave";
    private final static String animationStr = "Animation";
    private final static String userInterfaceStr = "UserInterface";
    @SuppressWarnings("unused")
	private final static String expressionStr = "Expression";
    @SuppressWarnings("unused")
	private final static String orStr = "Or";
    @SuppressWarnings("unused")
	private final static String andStr = "And";
    @SuppressWarnings("unused")
	private final static String notStr = "Not";
    private ProjectFactory theProjectFactory = null;
    private Project currProject = null;
    private Automaton currAutomaton = null;
    private Alphabet currAlphabet = null;
    private Locator locator = null;
    private Controls currControls = null;
    private Actions currActions = null;
    private Action currAction = null;
    private Control currControl = null;
    private Signals currSignals = null;
    private Timers currTimers = null;
    private InputProtocol inputProtocol = InputProtocol.UnknownProtocol;
    private File thisFile = null;

    // mappings between id and state/event
    private Map<String, State> idStateMap = new HashMap<String, State>();
    private Map<String, LabeledEvent> idEventMap = new HashMap<String, LabeledEvent>();

    public ProjectBuildFromXML()
    {
        this.theProjectFactory = new DefaultProjectFactory();
    }

    public ProjectBuildFromXML(final ProjectFactory theProjectFactory)
    {
        this.theProjectFactory = theProjectFactory;
    }

    public Project build(final URL url)
    throws Exception
    {
        final String protocol = url.getProtocol();

        if (protocol.equals("file"))
        {
            inputProtocol = InputProtocol.FileProtocol;

            final String fileName = url.getFile();

            thisFile = new File(fileName);
        }
        else if (protocol.equals("jar"))
        {
            inputProtocol = InputProtocol.JarProtocol;
        }
        else
        {
            inputProtocol = InputProtocol.UnknownProtocol;

            System.err.println("Unknown protocol: " + protocol);

            return null;
        }

        final InputStream stream = url.openStream();

        return build(stream);
    }

    @SuppressWarnings("deprecation")
	public Project build(final File file)
    throws Exception
    {
        return build(file.toURL());
    }

    public Project build(final InputStream is)
    throws Exception
    {
        return build(is, false);
    }

//    private Project build(File file, boolean validate)
//    throws Exception
//    {
//        return build(file.getCanonicalPath(), validate);
//    }

    private Project build(final InputStream is, final boolean validate)
    throws Exception
    {
        final InputSource source = new InputSource(is);

        return build(source, validate);
    }

//    private Project build(String fileName)
//    throws Exception
//    {
//        return build(fileName, false);
//    }

    // changed to public by Arash, we need to load from streams in XML-RPC interface!
    public Project build(final Reader r)
    throws Exception
    {
        return build(r, false);
    }

    private Project build(final Reader r, final boolean validate)
    throws Exception
    {
        final InputSource source = new InputSource(r);

        return build(source, validate);
    }

    @SuppressWarnings("unused")
	private Project build(final String fileName, final boolean validate)
    	throws Exception
    {
        final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

        parserFactory.setValidating(validate);

        final SAXParser parser = parserFactory.newSAXParser();

        try
        {
            parser.parse(new File(fileName), this);
        }
        catch (final SAXException ex)
        {
            System.out.println(ex.getMessage());

            throw new SupremicaException(ex.getMessage());
        }

        return currProject;
    }

    private Project build(final InputSource is, final boolean validate)
    throws Exception
    {
        final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

        parserFactory.setValidating(validate);

        final SAXParser parser = parserFactory.newSAXParser();

        try
        {
            parser.parse(is, this);
        }
        catch (final SAXException ex)
        {
            System.err.println(ex.getMessage());

            throw new SupremicaException(ex.getMessage());
        }

        return currProject;
    }

    public void setDocumentLocator(final Locator locator)
    {
        this.locator = locator;
    }

    public void startElement(final String uri, final String localName, final String name, final Attributes attributes)
    throws SAXException
    {
        // in order of frequency
        if (transitionStr.equals(name))
        {
            doTransition(attributes);
        }
        else if (stateStr.equals(name))
        {
            doState(attributes);
        }
        else if (eventStr.equals(name))
        {
            doEvent(attributes);
        }
        else if (automatonStr.equals(name))
        {
            // Reset the id maps when parsing a new automaton
            idEventMap = new HashMap<String, LabeledEvent>();
            idStateMap = new HashMap<String, State>();

            doAutomaton(attributes);
        }
        else if (projectStr.equals(name))
        {
            doProject(attributes);
        }
        else if (automataStr.equals(name))
        {
            doAutomata(attributes);
        }
 /*
        else if (layoutStr.equals(name))
        {
            doLayout(attributes);
        }
        else if (stateLayoutStr.equals(name))
        {
            doStateLayout(attributes);
        }
  */
        else if (executionStr.equals(name))
        {
            doExecution(attributes);
        }
        else if (inputSignalsStr.equals(name))
        {
            doInputSignals(attributes);
        }
        else if (outputSignalsStr.equals(name))
        {
            doOutputSignals(attributes);
        }
        else if (signalStr.equals(name))
        {
            doSignal(attributes);
        }
        else if (actionsStr.equals(name))
        {
            doActions(attributes);
        }
        else if (actionStr.equals(name))
        {
            doAction(attributes);
        }
        else if (controlsStr.equals(name))
        {
            doControls(attributes);
        }
        else if (controlStr.equals(name))
        {
            doControl(attributes);
        }
        else if (timersStr.equals(name))
        {
            doTimers(attributes);
        }
        else if (timerStr.equals(name))
        {
            doTimer(attributes);
        }
        else if (commandStr.equals(name))
        {
            doCommand(attributes);
        }
        else if (conditionStr.equals(name))
        {
            doCondition(attributes);
        }
        else if (animationStr.equals(name))
        {
            doAnimation(attributes);
        }
        else if (userInterfaceStr.equals(name))
        {
            doUserInterface(attributes);
        }
        else if (eventsStr.equals(name))
        {}
        else if (statesStr.equals(name))
        {}
        else if (transitionsStr.equals(name))
        {}
        else if (statesLayoutStr.equals(name))
        {}
        else
        {
            throwException("unknown element: " + name);
        }
    }

    public final void doAutomata(final Attributes attributes)
    throws SAXException
    {
        doProject(attributes);
    }

    public final void doAutomaton(final Attributes attributes)
    throws SAXException
    {
        final String name = attributes.getValue("name");
        if (name == null)
        {
            throwException("name attribute is missing");
        }

        currAutomaton = new Automaton(name);
        currAlphabet = currAutomaton.getAlphabet();


        final String type = attributes.getValue("type");

        // AutomatonType currType = AutomatonType.UNDEFINED; // Changed to specification
        AutomatonType currType = AutomatonType.SPECIFICATION;

        if (type != null)
        {
            currType = AutomatonType.toType(type);
        }

        // To deal with old files
        if (currType == AutomatonType.UNDEFINED)
        {
            currType = AutomatonType.SPECIFICATION;
        }

        currAutomaton.setType(currType);

        // Automaton comment
        final String comment = attributes.getValue("comment");
        if ((comment != null) &&!comment.equals(""))
        {
            currAutomaton.setComment(comment);
        }

        //currAutomaton.setAlphabet(currAlphabet);
        if (currProject.containsAutomaton(currAutomaton.getName()))
        {
            // Already there!?!
            System.err.println("Name conflict, multiple automata with name " +
                currAutomaton + ", discarding last one added.");
        }
        else
        {
            currProject.addAutomaton(currAutomaton);
        }
    }

    public final void doEvent(final Attributes attributes)
    throws SAXException
    {
        String id = null;
        String label = null;
        boolean controllable = true;
        boolean prioritized = true;
        boolean observable = true;
        boolean operatorIncrease = false;
        boolean operatorReset = false;
        boolean immediate = false;
        boolean epsilon = false;
        final int length = attributes.getLength();
        String currName;

        for (int i = 0; i < length; i++)
        {
            currName = attributes.getQName(i);

            if (idStr.equals(currName))
            {
                id = attributes.getValue(i);
            }
            else if (labelStr.equals(currName))
            {
                label = attributes.getValue(i);
            }
            else if (controllableStr.equals(currName))
            {
                controllable = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
            else if (prioritizedStr.equals(currName))
            {
                prioritized = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
            else if (observableStr.equals(currName))
            {
                observable = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
            else if (operatorIncreaseStr.equals(currName))
            {
                operatorIncrease = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
            else if (operatorResetStr.equals(currName))
            {
                operatorReset = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
            else if (immediateStr.equals(currName))
            {
                immediate = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
            else if (epsilonStr.equals(currName))
            {
                epsilon = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
        }

        if (id == null)
        {
            throwException("id attribute is missing");
        }

        if (label == null)
        {
            label = id;
        }

        // LabeledEvent currEvent = new LabeledEvent(label, id);
        final LabeledEvent currEvent = new LabeledEvent(label);

        //              currEvent.setId(id);
        //              currEvent.setLabel(label);
        currEvent.setControllable(controllable);
        currEvent.setPrioritized(prioritized);
        // TODO Not sure how to interpret the epsilon attribute  ~~~Robi
        currEvent.setObservable(observable && !epsilon);
        currEvent.setOperatorIncrease(operatorIncrease);
        currEvent.setOperatorReset(operatorReset);
        currEvent.setImmediate(immediate);

        // Associate the id with the event
        idEventMap.put(id, currEvent);

        try
        {
            currAlphabet.addEvent(currEvent);
        }
        catch (final Exception ex)
        {

            // System.err.println("Exception adding event. " + ex);
            // logger.debug(ex.getStackTrace());
            throw new RuntimeException(ex);
        }
    }

    public final void doState(final Attributes attributes)
    throws SAXException
    {
        String id = null;
        String name = null;
        boolean initial = false;
        boolean accepting = false;
        boolean forbidden = false;
        double cost = State.UNDEF_COST;
        final int length = attributes.getLength();
        String currName;

        for (int i = 0; i < length; i++)
        {
            currName = attributes.getQName(i);

            if (idStr.equals(currName))
            {
                id = attributes.getValue(i);
            }
            else if (nameStr.equals(currName))
            {
                final String stateName = attributes.getValue(i);
                if (name == null)
                {
                    name = stateName;
                }
                else
                {
                    name = stateName + name;
                }
            }
            else if (initialStr.equals(currName))
            {
                initial = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
            else if (acceptingStr.equals(currName))
            {
                accepting = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
            else if (forbiddenStr.equals(currName))
            {
                forbidden = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
            }
            else if (costStr.equals(currName))
            {
                cost = Double.valueOf(attributes.getValue(i)).doubleValue();
            }
        }

        if (id == null)
        {
            throwException("id attribute is missing");
        }

        if (name == null)
        {
            name = id;
        }

        if (cost != State.UNDEF_COST)
        {
            name += ", cost=" + cost;
        }

        if (currAutomaton.containsStateWithName(name))
        {
            throwException("several states with the same name (" + name + ")");
        }

        final State currState = new State(name);

        // currState.setId(id);
        // currState.setName(name);
        currState.setInitial(initial);
        currState.setAccepting(accepting);
        currState.setForbidden(forbidden);
        currState.setCost(cost);

        // Associate the id with the state
        idStateMap.put(id, currState);
        currAutomaton.addState(currState);
    }

    public final void doTransition(final Attributes attributes)
    throws SAXException
    {
        // Transition ::source
        final String sourceId = attributes.getValue("source");

        if (sourceId == null)
        {
            throwException("source attribute is missing");
        }

        // Get the state corresponding to this id
        final State sourceState = idStateMap.get(sourceId);

        // State sourceState = currAutomaton.getStateWithId(sourceId);
        if (sourceState == null)
        {
            throwException("cannot find source state: " + sourceId);
        }

        // Transition::dest
        final String destId = attributes.getValue("dest");

        if (destId == null)
        {
            throwException("dest attribute is missing");
        }

        // Get the state corresponding to this id
        final State destState = idStateMap.get(destId);

        // State destState = currAutomaton.getStateWithId(destId);
        if (destState == null)
        {
            throwException("cannot find dest state: " + destId);
        }

        // Transition::event
        final String eventId = attributes.getValue("event");

        if (eventId == null)
        {
            throwException("event attribute is missing");
        }

        // Get the event corresponding to this id
        if (!idEventMap.containsKey(eventId))
        {
            throwException("event id '" + eventId + "' is not a valid event id");
        }

        LabeledEvent event = idEventMap.get(eventId);

        // TEMP-solution (use EFA instead)
        double probability = Arc.DEFAULT_PROBABILITY;
        final String probabilityStr = attributes.getValue("probability");
        if (probabilityStr != null)
        {
            probability = Double.valueOf(probabilityStr).doubleValue();
            if (probability < 0 || probability > 1)
            {
                throwException("the probability value is out of range");
            }

//            currAutomaton.getAlphabet().removeEvent(event);
            event = new LabeledEvent(event.getLabel() + "_prob_" + (int)(probability*100)); // Multiplication with 100 since Waters cannot accept digital numbers in events names
            if (! currAutomaton.getAlphabet().contains(event))
            {
                currAutomaton.getAlphabet().addEvent(event);
            }
        }

        // Create and add the arc
        final Arc arc = new Arc(sourceState, destState, event, probability);

        // Arc arc = new Arc(sourceState, destState, eventId);
        try
        {
            currAutomaton.addArc(arc);
        }
        catch (final Exception ex)
        {
            throw new SAXException(ex);
        }
    }

    public final void throwException(final String msg)
    throws SAXException
    {
        final int line = locator.getLineNumber();
        @SuppressWarnings("unused")
        final
		int column = locator.getColumnNumber();
        final String exMsg = "Error while parsing at line: " + line + ". Reason: \"" + msg + "\".";
        throw new SAXException(exMsg);
    }

    public final void doProject(final Attributes attributes)
    throws SAXException
    {
        currProject = theProjectFactory.getProject();

        final String name = attributes.getValue("name");

        if (name != null)
        {
            currProject.setName(name);
        }

        final String comment = attributes.getValue("comment");
        if ((comment != null) &&!comment.equals(""))
        {
            currProject.setComment(comment);
        }

        int majorVersion = 0;
        final String majorStringVersion = attributes.getValue("major");

        if (majorStringVersion != null)
        {
            majorVersion = Integer.parseInt(majorStringVersion);
        }

        if (majorVersion > 0)
        {
            throw new SAXException("Unsupported file format.");
        }

        int minorVersion = 0;
        final String minorStringVersion = attributes.getValue("minor");

        if (minorStringVersion != null)
        {
            minorVersion = Integer.parseInt(minorStringVersion);
        }

        if (minorVersion > 10)
        {
            throw new SAXException("Unsupported file format.");
        }
    }

    /*
    public final void doLayout(Attributes attributes)
    throws SAXException
    {
        String width = attributes.getValue("width");

        if (width != null)
        {
            int iWidth = Integer.parseInt(width);

            currAutomaton.setWidth(iWidth);
        }

        String height = attributes.getValue("height");

        if (height != null)
        {
            int iHeight = Integer.parseInt(height);

            currAutomaton.setHeight(iHeight);
        }

        currAutomaton.setHasLayout(true);
    }
     */

    /*
    public final void doStateLayout(Attributes attributes)
    throws SAXException
    {
        String id = attributes.getValue("id");

        if (id == null)
        {
            throwException("id attribute is missing");
        }

        State currState = (State) idStateMap.get(id);

        // State currState = currAutomaton.getStateWithId(id);
        if (currState == null)
        {
            throwException("Cannot find state: " + id);
        }

        String x = attributes.getValue("x");
        String y = attributes.getValue("y");

        if ((x != null) && (y != null))
        {
            int iX = Integer.parseInt(x);
            int iY = Integer.parseInt(y);

            currState.setXY(iX, iY);
        }
    }
     **/

    /*
    public final void doTransitionLayout(Attributes attributes)
    throws SAXException
    {}
     */

    public final void doExecution(final Attributes attributes)
    throws SAXException
    {}

    public final void doInputSignals(final Attributes attributes)
    throws SAXException
    {
        currSignals = currProject.getInputSignals();
    }

    public final void doOutputSignals(final Attributes attributes)
    throws SAXException
    {
        currSignals = currProject.getOutputSignals();
    }

    public final void doSignal(final Attributes attributes)
    throws SAXException
    {
        final String label = attributes.getValue("label");

        if (label == null)
        {
            throwException("label attribute is missing");
        }

        if (currSignals.hasSignal(label))
        {
            throwException("multiple signals of " + label);
        }

        final String portStr = attributes.getValue("port");

        if (portStr == null)
        {
            throwException("port attribute is missing");
        }

        final int port = Integer.parseInt(portStr);
        final Signal newSignal = new BinarySignal(label, port);

        //System.err.println("Signal added:" + label);
        currSignals.addSignal(newSignal);
    }

    public final void doActions(final Attributes attributes)
    throws SAXException
    {
        if (currProject == null)
        {
            throwException("project section is missing");
        }

        currActions = currProject.getActions();
    }

    public final void doAction(final Attributes attributes)
    throws SAXException
    {
        if (currActions == null)
        {
            throwException("actions section is missing");
        }

        final String label = attributes.getValue("label");

        if (label == null)
        {
            throwException("label attribute is missing");
        }

        if (currActions.hasAction(label))
        {
            throwException("multiple actions of " + label);
        }

        currAction = new Action(label);

        currActions.addAction(currAction);
    }

    public final void doControls(final Attributes attributes)
    throws SAXException
    {
        if (currProject == null)
        {
            throwException("project section is missing");
        }

        currControls = currProject.getControls();
    }

    public final void doControl(final Attributes attributes)
    throws SAXException
    {
        if (currControls == null)
        {
            throwException("controls section is missing");
        }

        final String label = attributes.getValue("label");

        if (label == null)
        {
            throwException("label attribute is missing");
        }

        boolean invert = false;
        final String invertStr = attributes.getValue("invert");

        if (invertStr != null)
        {
            if (invertStr.equalsIgnoreCase("true"))
            {
                invert = true;
            }
        }

        if (currControls.hasControl(label))
        {
            throwException("multiple controls of " + label);
        }

        currControl = new Control(label, invert);

        currControls.addControl(currControl);
    }

    public final void doTimers(final Attributes attributes)
    throws SAXException
    {
        if (currProject == null)
        {
            throwException("project section is missing");
        }

        currTimers = currProject.getTimers();
    }

    public final void doTimer(final Attributes attributes)
    throws SAXException
    {
        if (currTimers == null)
        {
            throwException("timers section is missing");
        }

        final String name = attributes.getValue("name");

        if (name == null)
        {
            throwException("name attribute is missing");
        }

        final String startEvent = attributes.getValue("startEvent");

        if (startEvent == null)
        {
            throwException("startEvent attribute is missing");
        }

        final String timeoutEvent = attributes.getValue("timeoutEvent");

        if (timeoutEvent == null)
        {
            throwException("timeoutEvent attribute is missing");
        }

        final String delayStr = attributes.getValue("delay");

        if (delayStr == null)
        {
            throwException("delay attribute is missing");
        }

        final int delay = Integer.parseInt(delayStr);

        if (currTimers.hasTimer(name))
        {
            throwException("multiple timers of " + name);
        }

        final EventTimer currTimer = new EventTimer(name, startEvent, timeoutEvent, delay);

        currTimers.addTimer(currTimer);
    }

    public final void doCommand(final Attributes attributes)
    throws SAXException
    {
        if (currAction == null)
        {
            throwException("action section is missing");
        }

        final String command = attributes.getValue("command");

        if (command == null)
        {
            throwException("command attribute is missing");
        }

        boolean value = true;
        final String valueStr = attributes.getValue("value");

        if (valueStr != null)
        {
            if (valueStr.equalsIgnoreCase("false"))
            {
                value = false;
            }
        }

        currAction.addCommand(new Command(command, value));
    }

    public final void doCondition(final Attributes attributes)
    throws SAXException
    {
        if (currControl == null)
        {
            throwException("control section is missing");
        }

        final String condition = attributes.getValue("condition");

        if (condition == null)
        {
            throwException("condition attribute is missing");
        }

        boolean invert = false;
        final String invertStr = attributes.getValue("invert");

        if (invertStr != null)
        {
            if (invertStr.equalsIgnoreCase("true"))
            {
                invert = true;
            }
        }

        currControl.addCondition(new Condition(condition, invert));
    }

    @SuppressWarnings("deprecation")
	public final void doAnimation(final Attributes attributes)
    throws SAXException
    {
        if (currProject == null)
        {
            throwException("project section is missing");
        }

        final String path = attributes.getValue("path");

        if (path == null)
        {
            throwException("path attribute is missing");
        }

        URL url = null;

        try
        {
            url = new URL(path);
        }
        catch (final MalformedURLException ex)
        {    // This was not an url
            url = null;
            //System.err.println(ex);
        }

        try
        {
            if ((url == null) && (inputProtocol == InputProtocol.FileProtocol))
            {
                final File theAnimFile = new File(path);

                if (theAnimFile.isAbsolute())
                {
                    url = theAnimFile.toURL();
                }
                else
                {    // Make it absolute
                    if (thisFile != null)
                    {
                        final File newAnimFile = new File(thisFile.getParentFile(), path);

                        url = newAnimFile.toURL();
                    }
                    else
                    {    // What to do
                    }
                }
            }
            else if ((url == null) && (inputProtocol == InputProtocol.JarProtocol))
            {
                url = ProjectBuildFromXML.class.getResource(path);
            }
        }
        catch (final MalformedURLException ex)
        {    // This was not an url
            url = null;
            //System.err.println(ex);
        }

        currProject.setAnimationURL(url);
    }



    @SuppressWarnings("deprecation")
	public final void doUserInterface(final Attributes attributes)
    throws SAXException
    {
        if (currProject == null)
        {
            throwException("project section is missing");
        }

        final String path = attributes.getValue("path");

        if (path == null)
        {
            throwException("path attribute is missing");
        }

        URL url = null;

        try
        {
            url = new URL(path);
        }
        catch (final MalformedURLException ex)
        {    // This was not an url
            url = null;
            //System.err.println(ex);
        }

        try
        {
            if ((url == null) && (inputProtocol == InputProtocol.FileProtocol))
            {
                final File theUserInterfaceFile = new File(path);

                if (theUserInterfaceFile.isAbsolute())
                {
                    url = theUserInterfaceFile.toURL();
                }
                else
                {    // Make it absolute
                    if (thisFile != null)
                    {
                        final File newUserInterfaceFile = new File(thisFile.getParentFile(), path);

                        url = newUserInterfaceFile.toURL();
                    }
                    else
                    {    // What to do
                    }
                }
            }
            else if ((url == null) && (inputProtocol == InputProtocol.JarProtocol))
            {
                url = ProjectBuildFromXML.class.getResource(path);
            }
        }
        catch (final MalformedURLException ex)
        {    // This was not an url
            url = null;
            System.err.println(ex);
        }

        currProject.setUserInterfaceURL(url);
    }
}
