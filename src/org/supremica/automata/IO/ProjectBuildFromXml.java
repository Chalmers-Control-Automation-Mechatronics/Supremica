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
import java.net.MalformedURLException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.supremica.automata.*;
import org.supremica.automata.execution.*;
import org.supremica.log.*;

public class ProjectBuildFromXml
	extends DefaultHandler
{
	private static Logger logger = LoggerFactory.createLogger(ProjectBuildFromXml.class);

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
	private final static String immediateStr = "immediate";
	private final static String owner = "owner";
	private final static String hash = "hash";
	private final static String projectStr = "SupremicaProject";
	private final static String layoutStr = "Layout";
	private final static String statesLayoutStr = "StatesLayout";
	private final static String stateLayoutStr = "StateLayout";
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
	private final static String interfacesStr = "Interfaces";
	private final static String interfaceStr = "Interface";
	private final static String mastersStr = "Masters";
	private final static String masterStr = "Master";
	private final static String slavesStr = "Slaves";
	private final static String slaveStr = "Slave";
	private final static String animationStr = "Animation";
	private final static String expressionStr = "Expression";
	private final static String orStr = "Or";
	private final static String andStr = "And";
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
	private Map idStateMap = new HashMap();
	private Map idEventMap = new HashMap();

	public ProjectBuildFromXml()
	{
		this.theProjectFactory = new DefaultProjectFactory();
	}

	public ProjectBuildFromXml(ProjectFactory theProjectFactory)
	{
		this.theProjectFactory = theProjectFactory;
	}

	public Project build(URL url)
		throws Exception
	{
		String protocol = url.getProtocol();

		if (protocol.equals("file"))
		{
			inputProtocol = InputProtocol.FileProtocol;
			String fileName = url.getFile();
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

		InputStream stream = url.openStream();
		return build(stream);
	}

	public Project build(File file)
		throws Exception
	{
		return build(file.toURL());
	}

	private Project build(InputStream is)
		throws Exception
	{
		return build(is, false);
	}

	private Project build(File file, boolean validate)
		throws Exception
	{
		return build(file.getCanonicalPath(), validate);
	}

	private Project build(InputStream is, boolean validate)
		throws Exception
	{
		InputSource source = new InputSource(is);

		return build(source, validate);
	}


	private Project build(String fileName)
		throws Exception
	{
		return build(fileName, false);
	}

	private Project build(Reader r)
		throws Exception
	{
		return build(r, false);
	}



	private  Project build(Reader r, boolean validate)
		throws Exception
	{
		InputSource source = new InputSource(r);

		return build(source, validate);
	}

	private Project build(String fileName, boolean validate)
		throws Exception
	{
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setValidating(validate);

		SAXParser parser = parserFactory.newSAXParser();

		try
		{
			parser.parse(new File(fileName), this);
		}
		catch (SAXException ex)
		{
			logger.error(ex.getMessage());
			throw new Exception(ex.getMessage());
		}

		return currProject;
	}

	private Project build(InputSource is, boolean validate)
		throws Exception
	{
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setValidating(validate);

		SAXParser parser = parserFactory.newSAXParser();

		try
		{
			parser.parse(is, this);
		}
		catch (SAXException ex)
		{
			logger.error(ex.getMessage());
			throw new Exception(ex.getMessage());
		}

		return currProject;
	}

	public void setDocumentLocator(Locator locator)
	{
		this.locator = locator;
	}

	public void startElement(String uri, String localName, String name, Attributes attributes)
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
		else if (layoutStr.equals(name))
		{
			doLayout(attributes);
		}
		else if (stateLayoutStr.equals(name))
		{
			doStateLayout(attributes);
		}
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
		else if (eventsStr.equals(name)) {}
		else if (statesStr.equals(name)) {}
		else if (transitionsStr.equals(name)) {}
		else if (statesLayoutStr.equals(name)) {}
		else
		{
			throwException("Unknown element: " + name);
		}
	}

	public final void doAutomata(Attributes attributes)
		throws SAXException
	{
		doProject(attributes);
	}

	public final void doAutomaton(Attributes attributes)
		throws SAXException
	{
		currAutomaton = new Automaton();
		currAlphabet = currAutomaton.getAlphabet();

		String name = attributes.getValue("name");

		if (name == null)
		{
			throwException("name attribute is missing");
		}
		currAutomaton.setName(name);
		String type = attributes.getValue("type");

		AutomatonType currType = AutomatonType.Undefined;
		if (type != null)
		{
			currType = AutomatonType.toType(type);
		}
		currAutomaton.setType(currType);

		String comment = attributes.getValue("comment");

		if (comment != null)
		{
			currAutomaton.setComment(comment);
		}

		//currAutomaton.setAlphabet(currAlphabet);
		currProject.addAutomaton(currAutomaton);
	}

	public final void doEvent(Attributes attributes)
		throws SAXException
	{
		String id = null;
		String label = null;
		boolean controllable = true;
		boolean prioritized = true;
		boolean immediate = false;
		int length = attributes.getLength();
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
			else if (immediateStr.equals(currName))
			{
				immediate = Boolean.valueOf(attributes.getValue(i)) == Boolean.TRUE;
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

		LabeledEvent currEvent = new LabeledEvent(label, id);
//		currEvent.setId(id);
//		currEvent.setLabel(label);
		currEvent.setControllable(controllable);
		currEvent.setPrioritized(prioritized);
		currEvent.setImmediate(immediate);

		// Associate the id with the event
		idEventMap.put(id, currEvent);

		try
		{
			currAlphabet.addEvent(currEvent);
		}
		catch (Exception ex)
		{
			// logger.error("Exception adding event. " + ex);
			// logger.debug(ex.getStackTrace());
			throw new RuntimeException(ex);
		}
	}

	public final void doState(Attributes attributes)
		throws SAXException
	{
		String id = null;
		String name = null;
		boolean initial = false;
		boolean accepting = false;
		boolean forbidden = false;
		int cost = State.UNDEF_COST;
		int length = attributes.getLength();
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
				name = attributes.getValue(i);
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
			else if(costStr.equals(currName))
			{
				cost = Integer.valueOf(attributes.getValue(i)).intValue();
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

		State currState = new State(name);
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

	public final void doTransition(Attributes attributes)
		throws SAXException
	{
		// Transition ::source
		String sourceId = attributes.getValue("source");
		if (sourceId == null)
		{
			throwException("source attribute is missing");
		}
		// Get the state corresponding to this id
		State sourceState = (State)idStateMap.get(sourceId);
		// State sourceState = currAutomaton.getStateWithId(sourceId);
		if (sourceState == null)
		{
			throwException("Cannot find source state: " + sourceId);
		}
		// Transition::dest
		String destId = attributes.getValue("dest");
		if (destId == null)
		{
			throwException("dest attribute is missing");
		}
		// Get the state corresponding to this id
		State destState = (State)idStateMap.get(destId);
		// State destState = currAutomaton.getStateWithId(destId);
		if (destState == null)
		{
			throwException("Cannot find dest state: " + destId);
		}
		// Transition::event
		String eventId = attributes.getValue("event");
		if (eventId == null)
		{
			throwException("event attribute is missing");
		}
		// Get the event corresponding to this id
		if (!idEventMap.containsKey(eventId))
		{
			throwException("event id \"" + eventId + "\" is not a valid event id.");
		}
		LabeledEvent event = (LabeledEvent)idEventMap.get(eventId);
		// Create and add the arc
		Arc arc = new Arc(sourceState, destState, event);

		// Arc arc = new Arc(sourceState, destState, eventId);

		try
		{
			currAutomaton.addArc(arc);
		}
		catch (Exception ex)
		{
			throw new SAXException(ex);
		}
	}

	public final void throwException(String msg)
		throws SAXException
	{
		int line = locator.getLineNumber();
		int column = locator.getColumnNumber();
		String exMsg = "Error while parsing at line: " + line + ". Reason: \"" + msg + "\"";

		throw new SAXException(exMsg);
	}


	public final void doProject(Attributes attributes)
		throws SAXException
	{
		currProject = theProjectFactory.getProject();

		String name = attributes.getValue("name");

		if (name != null)
		{
			currProject.setName(name);
		}

		String owner = attributes.getValue("owner");

		if (name != null)
		{
			currProject.setOwner(owner);
		}

		String hash = attributes.getValue("hash");

		if (hash != null)
		{
			currProject.setHash(hash);
		}

		int majorVersion = 0;
		String majorStringVersion = attributes.getValue("major");

		if (majorStringVersion != null)
		{
			majorVersion = Integer.parseInt(majorStringVersion);
		}

		int minorVersion = 0;
		String minorStringVersion = attributes.getValue("minor");

		if (minorStringVersion != null)
		{
			minorVersion = Integer.parseInt(minorStringVersion);
		}

		if (majorVersion > 0)
		{
			throw new SAXException("Unsupported file format.");
		}

		if (minorVersion > 10)
		{
			throw new SAXException("Unsupported file format.");
		}
	}


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

	public final void doStateLayout(Attributes attributes)
		throws SAXException
	{
		String id = attributes.getValue("id");

		if (id == null)
		{
			throwException("id attribute is missing");
		}

		State currState = (State)idStateMap.get(id);
		// State currState = currAutomaton.getStateWithId(id);

		if (currState == null)
		{
			throwException("Cannot find state: " + id);
		}

		String x = attributes.getValue("x");
		String y = attributes.getValue("y");
		if (x != null && y != null)
		{
			int iX = Integer.parseInt(x);
			int iY = Integer.parseInt(y);
			currState.setXY(iX, iY);
		}
	}

	public final void doTransitionLayout(Attributes attributes)
		throws SAXException
	{
	}

	public final void doExecution(Attributes attributes)
		throws SAXException
	{
	}

	public final void doInputSignals(Attributes attributes)
		throws SAXException
	{
		currSignals = currProject.getInputSignals();
	}

	public final void doOutputSignals(Attributes attributes)
		throws SAXException
	{
		currSignals = currProject.getOutputSignals();
	}

	public final void doSignal(Attributes attributes)
		throws SAXException
	{
		String label = attributes.getValue("label");

		if (label == null)
		{
			throwException("label attribute is missing");
		}
		if (currSignals.hasSignal(label))
		{
			throwException("Multiple signals of " + label);
		}

		String portStr = attributes.getValue("port");
		if (portStr == null)
		{
			throwException("port attribute is missing");
		}
		int port = Integer.parseInt(portStr);

		Signal newSignal = new BinarySignal(label, port);

		//System.err.println("Signal added:" + label);
		currSignals.addSignal(newSignal);
	}

	public final void doActions(Attributes attributes)
		throws SAXException
	{
		if (currProject == null)
		{
			throwException("Project section is missing");
		}
		currActions = currProject.getActions();
	}

	public final void doAction(Attributes attributes)
		throws SAXException
	{
		if (currActions == null)
		{
			throwException("Actions section is missing");
		}

		String label = attributes.getValue("label");

		if (label == null)
		{
			throwException("label attribute is missing");
		}

		if (currActions.hasAction(label))
		{
			throwException("Multiple actions of " + label);
		}
		currAction = new Action(label);

		currActions.addAction(currAction);
	}

	public final void doControls(Attributes attributes)
		throws SAXException
	{
		if (currProject == null)
		{
			throwException("Project section is missing");
		}
		currControls = currProject.getControls();
	}

	public final void doControl(Attributes attributes)
		throws SAXException
	{
		if (currControls == null)
		{
			throwException("Controls section is missing");
		}
		String label = attributes.getValue("label");

		if (label == null)
		{
			throwException("label attribute is missing");
		}

		boolean invert = false;
		String invertStr = attributes.getValue("invert");

		if (invertStr != null)
		{
			if (invertStr.equalsIgnoreCase("true"))
			{
				invert = true;
			}
		}

		if (currControls.hasControl(label))
		{
			throwException("Multiple controls of " + label);
		}
		currControl = new Control(label, invert);

		currControls.addControl(currControl);
	}

	public final void doTimers(Attributes attributes)
		throws SAXException
	{
		if (currProject == null)
		{
			throwException("Project section is missing");
		}
		currTimers = currProject.getTimers();
	}

	public final void doTimer(Attributes attributes)
		throws SAXException
	{
		if (currTimers == null)
		{
			throwException("Timers section is missing");
		}

		String name = attributes.getValue("name");
		if (name == null)
		{
			throwException("name attribute is missing");
		}

		String startEvent = attributes.getValue("startEvent");
		if (startEvent == null)
		{
			throwException("startEvent attribute is missing");
		}

		String timeoutEvent = attributes.getValue("timeoutEvent");
		if (timeoutEvent == null)
		{
			throwException("timeoutEvent attribute is missing");
		}

		String delayStr = attributes.getValue("delay");
		if (delayStr == null)
		{
			throwException("delay attribute is missing");
		}
		int delay = Integer.parseInt(delayStr);

		if (currTimers.hasTimer(name))
		{
			throwException("Multiple timers of " + name);
		}
		EventTimer currTimer = new EventTimer(name, startEvent, timeoutEvent, delay);

		currTimers.addTimer(currTimer);
	}

	public final void doCommand(Attributes attributes)
		throws SAXException
	{
		if (currAction == null)
		{
			throwException("Action section is missing");
		}
		String command = attributes.getValue("command");

		if (command == null)
		{
			throwException("command attribute is missing");
		}

		boolean value = true;
		String valueStr = attributes.getValue("value");

		if (valueStr != null)
		{
			if (valueStr.equalsIgnoreCase("false"))
			{
				value = false;
			}
		}

		currAction.addCommand(new Command(command, value));
	}

	public final void doCondition(Attributes attributes)
		throws SAXException
	{
		if (currControl == null)
		{
			throwException("Control section is missing");
		}
		String condition = attributes.getValue("condition");

		if (condition == null)
		{
			throwException("condition attribute is missing");
		}

		boolean invert = false;
		String invertStr = attributes.getValue("invert");

		if (invertStr != null)
		{
			if (invertStr.equalsIgnoreCase("true"))
			{
				invert = true;
			}
		}
		currControl.addCondition(new Condition(condition, invert));
	}

	public final void doAnimation(Attributes attributes)
		throws SAXException
	{
		if (currProject == null)
		{
			throwException("Project section is missing");
		}
		String path = attributes.getValue("path");

		if (path == null)
		{
			throwException("path attribute is missing");
		}

		URL url = null;
		try
		{
			url = new URL(path);
		}
		catch (MalformedURLException ex)
		{ // This was not an url
			url = null;
		}

		try
		{
			if (url == null && inputProtocol == InputProtocol.FileProtocol)
			{
				File theAnimFile = new File(path);
				if (theAnimFile.isAbsolute())
				{
					url = theAnimFile.toURL();
				}
				else
				{ // Make it absolute
					if (thisFile != null)
					{
						File newAnimFile = new File(thisFile.getParentFile(), path);
						url = newAnimFile.toURL();
					}
					else
					{ // What to do
					}
				}

			}
			else if (url == null && inputProtocol == InputProtocol.JarProtocol)
			{
				url = ProjectBuildFromXml.class.getResource(path);
			}
		}
		catch (MalformedURLException ex)
		{ // This was not an url
			url = null;
		}
		currProject.setAnimationURL(url);
	}
}
