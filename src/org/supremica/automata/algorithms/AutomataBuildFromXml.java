
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
package org.supremica.automata.algorithms;

import java.util.*;
import java.io.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class AutomataBuildFromXml
	extends HandlerBase
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
	private final static String labelStr = "label";
	private final static String controllableStr = "controllable";
	private final static String prioritizedStr = "prioritized";
	private final static String immediateStr = "immediate";
	private final static String owner = "owner";
	private final static String hash = "hash";
	private Automata currAutomata = null;
	private Automaton currAutomaton = null;
	private Alphabet currAlphabet = null;
	private Locator locator = null;

	public AutomataBuildFromXml() {}

	public Automata build(File file)
		throws Exception
	{
		return build(file, false);
	}

	public Automata build(File file, boolean validate)
		throws Exception
	{
		return build(file.getCanonicalPath(), validate);
	}

	public Automata build(String fileName)
		throws Exception
	{
		return build(fileName, false);
	}

	public Automata build(InputStream is)
		throws Exception
	{
		return build(is, false);
	}

	public Automata build(Reader r)
		throws Exception
	{
		return build(r, false);
	}

	public Automata build(InputStream is, boolean validate)
		throws Exception
	{
		InputSource source = new InputSource(is);

		return build(source, validate);
	}

	public Automata build(Reader r, boolean validate)
		throws Exception
	{
		InputSource source = new InputSource(r);

		return build(source, validate);
	}

	public Automata build(String fileName, boolean validate)
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
			throw new Exception(ex.getMessage());
		}

		return currAutomata;
	}

	public Automata build(InputSource is, boolean validate)
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
			throw new Exception(ex.getMessage());
		}

		return currAutomata;
	}

	public void setDocumentLocator(Locator locator)
	{
		this.locator = locator;
	}

	public void startElement(String name, AttributeList attributes)
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
		else if (automataStr.equals(name))
		{
			doAutomata(attributes);
		}
		else if (eventsStr.equals(name)) {}
		else if (statesStr.equals(name)) {}
		else if (transitionsStr.equals(name)) {}
		else
		{
			throwException("Unknown element: " + name);
		}
	}

	public final void doAutomata(AttributeList attributes)
		throws SAXException
	{
		currAutomata = new Automata();

		String name = attributes.getValue("name");

		if (name != null)
		{
			currAutomata.setName(name);
		}

		String owner = attributes.getValue("owner");

		if (name != null)
		{
			currAutomata.setOwner(owner);
		}

		String hash = attributes.getValue("hash");

		if (hash != null)
		{
			currAutomata.setHash(hash);
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

		if (minorVersion > 9)
		{
			throw new SAXException("Unsupported file format.");
		}
	}

	public final void doAutomaton(AttributeList attributes)
		throws SAXException
	{
		currAutomaton = new Automaton();
		currAlphabet = new Alphabet();

		String name = attributes.getValue("name");

		if (name == null)
		{
			throwException("name attribute is missing");
		}

		String type = attributes.getValue("type");

		if (type != null)
		{
			String lowerCaseType = type.toLowerCase();
			String plantLowerCase = AutomatonType.Plant.toString().toLowerCase();
			String specificationLowerCase = AutomatonType.Specification.toString().toLowerCase();
			String supervisorLowerCase = AutomatonType.Supervisor.toString().toLowerCase();
			AutomatonType currType = AutomatonType.Undefined;

			if (lowerCaseType.equals(plantLowerCase))
			{
				currType = AutomatonType.Plant;
			}

			if (lowerCaseType.equals(specificationLowerCase))
			{
				currType = AutomatonType.Specification;
			}

			if (lowerCaseType.equals(supervisorLowerCase))
			{
				currType = AutomatonType.Supervisor;
			}

			currAutomaton.setType(currType);
		}

		currAutomaton.setName(name);
		currAutomaton.setAlphabet(currAlphabet);
		currAutomata.addAutomaton(currAutomaton);
	}

	public final void doEvent(AttributeList attributes)
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
			currName = attributes.getName(i);

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

		LabeledEvent currEvent = new LabeledEvent();

		if (id == null)
		{
			throwException("id attribute is missing");
		}

		if (label == null)
		{
			label = id;
		}

		currEvent.setId(id);
		currEvent.setLabel(label);
		currEvent.setControllable(controllable);
		currEvent.setPrioritized(prioritized);
		currEvent.setImmediate(immediate);

		try
		{
			currAlphabet.addEvent(currEvent);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	public final void doState(AttributeList attributes)
		throws SAXException
	{
		String id = null;
		String name = null;
		boolean initial = false;
		boolean accepting = false;
		boolean forbidden = false;
		int length = attributes.getLength();
		String currName;

		for (int i = 0; i < length; i++)
		{
			currName = attributes.getName(i);

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
		}

		if (id == null)
		{
			throwException("id attribute is missing");
		}

		if (name == null)
		{
			name = id;
		}

		State currState = new State();

		currState.setId(id);
		currState.setName(name);
		currState.setInitial(initial);
		currState.setAccepting(accepting);
		currState.setForbidden(forbidden);
		currAutomaton.addState(currState);
	}

	public final void doTransition(AttributeList attributes)
		throws SAXException
	{
		String source = attributes.getValue("source");

		if (source == null)
		{
			throwException("source attribute is missing");
		}

		String dest = attributes.getValue("dest");

		if (dest == null)
		{
			throwException("dest attribute is missing");
		}

		String event = attributes.getValue("event");

		if (event == null)
		{
			throwException("event attribute is missing");
		}

		State sourceState = currAutomaton.getStateWithId(source);

		if (sourceState == null)
		{
			throwException("Cannot find source state: " + source);
		}

		State destState = currAutomaton.getStateWithId(dest);

		if (destState == null)
		{
			throwException("Cannot find dest state: " + dest);
		}

		Arc a = new Arc(sourceState, destState, event);

		currAutomaton.addArc(a);
	}

	public final void throwException(String msg)
		throws SAXException
	{
		int line = locator.getLineNumber();
		int column = locator.getColumnNumber();
		String exMsg = "Error while parsing at line: " + line + ". Reason: \"" + msg + "\"";

		throw new SAXException(exMsg);
	}
}
