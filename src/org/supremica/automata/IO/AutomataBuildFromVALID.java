
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

import org.supremica.util.SupremicaException;
import java.util.*;
import java.io.*;
import org.jdom.*;
import org.jdom.input.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import java.util.regex.Pattern;

public class AutomataBuildFromVALID
{
	private static Logger logger = LoggerFactory.createLogger(AutomataBuildFromVALID.class);
	private ProjectFactory theProjectFactory;
	private Project currAutomata = null;
	private SAXBuilder docBuilder;
	private String filePath;
	private int autonum = 0;

	// mappings between id and state/event
	private Map idStateMap = new HashMap();
	private Map idEventMap = new HashMap();

	public AutomataBuildFromVALID(ProjectFactory theProjectFactory)
	{
		this.theProjectFactory = theProjectFactory;
	}
	
	public Automata build(File file)
		throws Exception
	{
		return build(file, false);
	}

	public Automata build(File file, boolean validate)
		throws Exception
	{
		currAutomata = theProjectFactory.getProject();
		filePath = file.getParent();

		try
		{
			// Find SAXparser using JAXP, validation optional
			docBuilder = new SAXBuilder(validate);
			
			// Create the document
			Document doc = docBuilder.build(file);
			Element root = doc.getRootElement();
			
			if (root.getName() == "graph")
			{
				// DGRF-file
				automatonFromDGRF(root, "", "Undefined");
			}
			else if (root.getName() == "module")
			{
				// VMOD-file
				automataFromVMOD(root, "");
			}
			else if (root.getName() == "project")
			{
				// VPRJ-file
				automataFromVPRJ(root);
			}
			else
			{
				throw new SupremicaException("Not a valid VALID file: " + file.getName() + 
											 ", root name not recognised.");
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}

		return currAutomata;
	}

	private void automataFromVPRJ(Element root)
		throws Exception
	{
		logger.debug("Build automataFromVPRJ. " + root);
		Document subDoc = docBuilder.build(new File(filePath + File.separator + 
								root.getChild("uses").getAttributeValue("module") + 
								".vmod"));
		Element subRoot = subDoc.getRootElement();

		automataFromVMOD(subRoot, "");
	}

	/**
	 * Builds automata in currAutomata
	 * Brasklapp: I'm not quite pleased with this name-argument
	 * everywhere... but it works (it's needed in the recursion)
	 */
	private void automataFromVMOD(Element root, String name)
		throws Exception
	{
		logger.debug("Build automataFromVMOD. " + root);
		HashMap definitionHash = new HashMap();
		Element element;

		// Note! This variable is used many times in different situations
		Iterator i;

		// Read "typeDefinition"s and store in HashMap as lists of strings
		if (root.getChild("definitions") != null)
		{
			List definitionList = root.getChild("definitions").getChildren("typeDefinition");
			i = definitionList.iterator();
			while (i.hasNext())
			{
				element = (Element) i.next();

				// Build range list
				List stringList = buildRangeList(element.getAttributeValue("expression"));
				definitionHash.put(element.getAttributeValue("name"), stringList);
			}
		}

		// Loop over DGRF-files ("component"s)
		List componentList = root.getChild("parts").getChildren("component");
		i = componentList.iterator();
		while (i.hasNext())
		{
			element = (Element) i.next();

			Document subDoc = docBuilder.build(new File(filePath + File.separator + element.getAttributeValue("graph") + ".dgrf"));
			Element subRoot = subDoc.getRootElement();

			automatonFromDGRF(subRoot, name, element.getAttributeValue("kind"));
		}

		// LOOP over VMOD-files ("instance"s)
		List instanceList = root.getChild("parts").getChildren("foreach-instance");
		i = instanceList.iterator();
		while (i.hasNext())
		{
			element = (Element) i.next();

			// Here we need to examine two files at the same time (for renaming events)
			Document subDoc = docBuilder.build(new File(filePath + File.separator + 
								element.getChild("instance").getAttributeValue("module") + ".vmod"));
			Element subRoot = subDoc.getRootElement();
			String dummyVariable = element.getAttributeValue("dummy");

			// Get the list from the HashMap OR from the range-attribute in some cases!!
			List modificationList;
			if (element.getAttributeValue("range").startsWith("$"))
			{
				modificationList = (List) definitionHash.get(element.getAttributeValue("range").substring(1));
			}
			else
			{
				modificationList = buildRangeList(element.getAttributeValue("range"));
			}

			// Get first part of the instance name for renaming later
			StringTokenizer st = new StringTokenizer(element.getChild("instance").getAttributeValue("name"), "[$]");
			String oldAutomatonName = st.nextToken();
			
			if (!dummyVariable.equals(st.nextToken()))
			{
				throw new SupremicaException("Something is wrong with the dummy variables in " + 
											 root.getAttributeValue("module") + ".vmod.");
			}

			// Expand templates (build automata and modify names)
			Iterator j = modificationList.iterator();
			while (j.hasNext())
			{
				String modification = (String) j.next();

				String newAutomatonName = oldAutomatonName + "." + modification;

				// Builds automaton for this template name
				automataFromVMOD(subRoot, newAutomatonName);

				// Change event names
				List nameParamList = element.getChild("instance").getChildren("nameParam");
				Iterator k = nameParamList.iterator();
				while (k.hasNext())
				{
					Element nameParamElement = (Element) k.next();
					
					// The second token (if existing) after this tokenization should be the dummy 
					// if it is to be replaced!
					st = new StringTokenizer(nameParamElement.getAttributeValue("value"), "[$]");
					String newEventName = st.nextToken();
					if (st.hasMoreTokens())
					{
						// Find out what the new name should be
						String appendix = st.nextToken();
						if (appendix.equals(dummyVariable))
						{
							newEventName = newEventName + "." + modification;
						}
						else
						{
							newEventName = newEventName + "." + appendix;
						}

						// Find out what the event name really is (not as straightforward as one might think)
						Iterator l = subRoot.getChild("local").getChildren("alias").iterator();
						String oldEventName;
						while (true)
						{
							Element alias = (Element) l.next();

							if (alias.getAttributeValue("new").equals(nameParamElement.getAttributeValue("name")))
							{
								oldEventName = alias.getAttributeValue("old");
								 
								break;
							}

							if (!l.hasNext())
							{
								throw new SupremicaException("Something is wrong with the aliases in " + subRoot.getAttributeValue("name") + ".vmod.");
							}
						}

						// Change name on the label in the automaton
						// currAutomata.getAutomaton(newAutomatonName).getEvent(oldEventName).setLabel(newEventName);

						// Nowadays it's more complicated than the above. Oh woe!
						Automaton currAutomaton = currAutomata.getAutomaton(newAutomatonName);
						LabeledEvent currEvent = currAutomaton.getAlphabet().getEvent(oldEventName);

						// Make the new event a copy of the old one but with new label
						LabeledEvent newEvent = new LabeledEvent(currEvent);
						newEvent.setLabel(newEventName);

						// Modify each arc that has this event (in the good old days nothing like this
						// was necessary, it was enough to change the label (and leave the id untouched))
						ArcIterator arcIt = currAutomaton.arcIterator();
						while (arcIt.hasNext())
						{
							Arc currArc = arcIt.nextArc();

							if (currArc.getEvent().equals(currEvent))
							{
								currArc.setEvent(newEvent);
							}
						}

						Alphabet currAlphabet = currAutomaton.getAlphabet();
						currAlphabet.removeEvent(currEvent);
						currAlphabet.addEvent(newEvent);
					}
				}
			}
		}
	}

	/**
	 * Parses VALID's range lists. They appear to be either of the type "[A B C D]", i.e. an enumeration
	 * or of the type "[1..4]", i.e. a range of integers. May there also be ranges of characters?
	 */
	private List buildRangeList(String string)
	{
		StringTokenizer st = new StringTokenizer(string, "{ }");
		
		List stringList = new ArrayList();
		
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			
			// Is this a simple token or a range (e.g. "1..4")?
			if (token.matches("\\d+\\.\\.\\d+"))
			{
				String[] limits = token.split("\\.\\.", 2);
				int a = Integer.parseInt(limits[0]);
				int b = Integer.parseInt(limits[1]);
				
				for (int index=a; index<=b; index++)
				{
					stringList.add(index + "");
				}
			}
			else
			{
				// Add the simple token
				stringList.add(token);
			}
		}

		return stringList;
	}

	/**
	 * Builds one automaton and returns it
	 * @param name Suggestion for the automaton's name
	 */
	private void automatonFromDGRF(Element root, String name, String type)
		throws Exception
	{
		logger.debug("Build automatonFromDGRF. " + root);
		Automaton currAutomaton = new Automaton();
		Alphabet currAlphabet = new Alphabet();
		Element element;

		// Name automaton, primarily with supplied name, otherwise with name attribute from file
		if (name.equals(""))
		{
			// Fix here to handle no-named graphs
			name = root.getAttributeValue("name");

			/*
			if(name == null || name.equals(""))
			{
				name = "Automaton " + ++autonum;
			}
			*/
		}
		currAutomaton.setName(name);

		// Set automaton type
		if (type.toLowerCase().equals("plant"))
		{
			currAutomaton.setType(AutomatonType.Plant);
		}
		else if (type.toLowerCase().equals("spec"))
		{
			currAutomaton.setType(AutomatonType.Specification);
		}
		else if (type.toLowerCase().equals("sup"))
		{
			currAutomaton.setType(AutomatonType.Supervisor);
		}
		else
		{
			currAutomaton.setType(AutomatonType.Undefined);
		}

		// Build alphabet
		List eventList = root.getChild("events").getChildren("event");
		Iterator i = eventList.iterator();
		
		while (i.hasNext())
		{
			element = (Element) i.next();
			
			String eventName = element.getAttributeValue("name");
			
			// currEvent.setId(eventName);
			// currEvent.setLabel(eventName);

			LabeledEvent currEvent = new LabeledEvent(eventName);
			currEvent.setControllable(element.getAttributeValue("controllable").equals("1"));
			currEvent.setPrioritized(true);
			idEventMap.put(eventName, currEvent);
			currAlphabet.addEvent(currEvent);
		}

		currAutomaton.getAlphabet().union(currAlphabet);

		// Build states
		List stateList = root.getChild("nodes").getChildren("node");
		i = stateList.iterator();
		while (i.hasNext())
		{
			element = (Element) i.next();

			String stateName = element.getChild("label").getAttributeValue("name");

			// currState.setId(stateName);
			// currState.setName(stateName);   // id and name, always the same
			State currState = new State(stateName);

			currState.setInitial(element.getAttributeValue("initial").equals("1"));
			currState.setAccepting(element.getAttributeValue("marked").equals("1"));
			idStateMap.put(stateName, currState);
			currAutomaton.addState(currState);
		}

		// Build arcs
		List arcList = root.getChild("edges").getChildren("edge");
		i = arcList.iterator();
		while (i.hasNext())
		{
			// State sourceState = null; // new State();
			State destState = null;    // new State();

			// String event = "";
			element = (Element) i.next();

			// Grouped states
			if (element.getChild("source").getAttributeValue("name").startsWith("$"))
			{
				stateList = element.getChild("labelGroup").getChild("label").getChildren("actualSource");
			}
			else
			{
				stateList = element.getChildren("source");
			}

			// Self loops
			if ((element.getAttributeValue("isLoop") != null) && (element.getAttributeValue("isLoop").equals("1")))
			{
				destState = (State) idStateMap.get(element.getChild("source").getAttributeValue("name"));
			}
			else
			{
				destState = (State) idStateMap.get(element.getChild("target").getAttributeValue("name"));
			}

			eventList = element.getChild("labelGroup").getChildren("label");

			Iterator j = eventList.iterator();
			while (j.hasNext())
			{
				element = (Element) j.next();

				String eventId = element.getAttributeValue("name");
				LabeledEvent event = (LabeledEvent) idEventMap.get(eventId);
				Iterator k = stateList.iterator();

				while (k.hasNext())
				{
					element = (Element) k.next();

					State sourceState = (State) idStateMap.get(element.getAttributeValue("name"));

					// sourceState = currAutomaton.getStateWithId(element.getAttributeValue("name"));
					currAutomaton.addArc(new Arc(sourceState, destState, event));
				}
			}
		}

		currAutomata.addAutomaton(currAutomaton);
	}
}
