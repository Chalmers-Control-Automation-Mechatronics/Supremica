
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

import java.io.*;
import java.util.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.*;
import org.supremica.automata.execution.*;
import org.supremica.log.*;

public class ProjectToHtml
{
	private static Logger logger = LoggerFactory.createLogger(ProjectToHtml.class);
	private Project project;
	private File directory;
	private boolean overwrite;
	private int maxNbrOfStatesInPng;
	private Process dotProcess;
	private PrintWriter toDotWriter;
	private InputStream fromDotStream;

	public ProjectToHtml(Project project, File directory)
	{
		this.overwrite = false;
		this.project = project;
		this.directory = directory;

		if (!directory.isDirectory())
		{
			logger.error("ProjectToHtml: You must export to a directory");
		}

		maxNbrOfStatesInPng = SupremicaProperties.getDotMaxNbrOfStatesWithoutWarning();
	}

	public void serialize()
		throws Exception
	{
		project.setSynchronizationIndicies();
		serializeProject();

		// Serialize all automata
		for (Iterator projectIt = project.iterator(); projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();
			serializeAutomaton(aut);
		}

		// Serialize all events
		Alphabet projectAlphabet = project.getUnionAlphabet();
		for (Iterator alphIt = projectAlphabet.iterator(); alphIt.hasNext(); )
		{
			LabeledEvent ev = (LabeledEvent) alphIt.next();
			serializeEvent(ev);
		}
	}

	public void serializeProject()
		throws Exception
	{
		PrintWriter pw = getPrintWriter("index.html");
		printHtmlBegin(pw, "Supremica Project: " + project.getName());
		pw.println("<h1>Project: " + normalize(project.getName()) + "</h1>");
		pw.println("<h2>Plants:</h2>");
		pw.println("<ul>");
		for (Iterator projectIt = project.plantIterator(); projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();
			pw.println("<li> <a href=\"automaton" + aut.getSynchIndex() + ".html\">" + normalize(aut.getName()) + "</a></li>");

		}
		pw.println("</ul>");
		pw.println("<h2>Specifications:</h2>");
		pw.println("<ul>");
		for (Iterator projectIt = project.specificationIterator(); projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();

			pw.println("<li> <a href=\"automaton" + aut.getSynchIndex() + ".html\">" + normalize(aut.getName()) + "</a></li>");

		}
		pw.println("</ul>");
		pw.println("<h2>Supervisors:</h2>");
		pw.println("<ul>");
		for (Iterator projectIt = project.supervisorIterator(); projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();

			pw.println("<li> <a href=\"automaton" + aut.getSynchIndex() + ".html\">" + normalize(aut.getName()) + "</a></li>");

		}
		pw.println("</ul>");
		pw.println("<h2>Interfaces:</h2>");
		pw.println("<ul>");
		for (Iterator projectIt = project.interfaceIterator(); projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();

			pw.println("<li> <a href=\"automaton" + aut.getSynchIndex() + ".html\">" + normalize(aut.getName()) + "</a></li>");

		}
		pw.println("</ul>");
		printHtmlEnd(pw);
	}

	public void serializeAutomaton(Automaton theAutomaton)
		throws Exception
	{
		PrintWriter pw = getPrintWriter("automaton" + theAutomaton.getSynchIndex() + ".html");
		printHtmlBegin(pw, "Supremica Automaton: " + theAutomaton.getName());
		pw.println("<h1>Automaton: " + normalize(theAutomaton.getName()) + "</h1>");
		pw.println("<h2>Type: " + theAutomaton.getType() + "</h2>");
		pw.println("<h2>Alphabet:</h2>");
		pw.println("<ul>");
		for (Iterator eventIt = theAutomaton.eventIterator(); eventIt.hasNext(); )
		{
			LabeledEvent event = (LabeledEvent) eventIt.next();
			pw.println("<li> <a href=\"event" + event.getSynchIndex() + ".html\">" + normalize(event.getLabel()) + "</a></li>");
		}
		pw.println("</ul>");
		boolean pngCreated = createPngFile(theAutomaton);
		if (pngCreated)
		{
			pw.println("<center>");
			pw.println("<img src=\"automaton" + theAutomaton.getSynchIndex() + ".png\"</img>");
			pw.println("</center>");
		}
		printHtmlEnd(pw);
	}

	public void serializeEvent(LabeledEvent theEvent)
		throws Exception
	{
		PrintWriter pw = getPrintWriter("event" + theEvent.getSynchIndex() + ".html");
		printHtmlBegin(pw, "Supremica Event: " + theEvent.getLabel());

		pw.println("<h1>Event: " + normalize(theEvent.getLabel()) + "</h1>");
		pw.println("<ul>");
		pw.println("<li>" + "Controllable: " + theEvent.isControllable() + "</li>");
		pw.println("<li>" + "Immediate: " + theEvent.isImmediate() + "</li>");
		pw.println("</ul>");
		pw.println("<h2>Prioritized in:</h2>");
		pw.println("<h2>Broadcast in:</h2>");
		printHtmlEnd(pw);
	}


	private void printHtmlBegin(PrintWriter pw, String title)
	{
		pw.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
		pw.println("<html>");
		pw.println("<head>");
		pw.println("<title>" + normalize(title) + "</title>");
		pw.println("</head>");
		pw.println("<body bgcolor=\"#FFFFFF\">");
	}

	private void printHtmlEnd(PrintWriter pw)
	{
		pw.println("</body>");
		pw.println("</html>");
		pw.flush();
		pw.close();
	}


	private boolean createPngFile(Automaton theAutomaton)
		throws Exception
	{
		if (theAutomaton.nbrOfStates() > maxNbrOfStatesInPng)
		{
			return false;
		}
		File currFile = getFile("automaton" + theAutomaton.getSynchIndex() + ".png");

		if (currFile != null)
		{
			if (!currFile.isDirectory())
			{
				try
				{
					AutomatonToDot exporter = new AutomatonToDot(theAutomaton);
					exporter.setUseColors(true);

					try
					{
						dotProcess = Runtime.getRuntime().exec(SupremicaProperties.getDotExecuteCommand() + " -Tpng");
					}
					catch (IOException ex)
					{
						logger.error("Cannot run dot. Make sure dot is in the path.");

						throw ex;
					}

					OutputStream pOut = dotProcess.getOutputStream();
					BufferedOutputStream pBuffOut = new BufferedOutputStream(pOut);

					toDotWriter = new PrintWriter(pBuffOut);
					fromDotStream = dotProcess.getInputStream();

					// Send the file to dot
					exporter.serialize(toDotWriter);
					toDotWriter.close();

					// Send the response to a file
					FileOutputStream fw = new FileOutputStream(currFile);
					BufferedOutputStream buffOutStream = new BufferedOutputStream(fw);
					BufferedInputStream buffInStream = new BufferedInputStream(fromDotStream);
					int currChar = buffInStream.read();

					while (currChar != -1)
					{
						buffOutStream.write(currChar);

						currChar = buffInStream.read();
					}

					buffInStream.close();
					buffOutStream.close();
				}
				catch (Exception ex)
				{
					logger.error("Error while exporting " + currFile.getAbsolutePath() + "\n", ex);

					return false;
				}
			}
		}
		return true;

	}

	/**
	 * Given a name create the corresponding file in the current directory.
	 */
	private File getFile(String name)
		throws Exception
	{
		File newFile = new File(directory, name);
		return newFile;
	}

	/**
	 * Given a name create the corresponding file and return a PrintWriter to that object..
	 */
	private PrintWriter getPrintWriter(String name)
		throws Exception
	{
		return new PrintWriter(new FileWriter(getFile(name)));
	}

/*
	public void serialize(PrintWriter pw)
	{
		pw.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		pw.print("<SupremicaProject");

		if (project.getName() != null)
		{
			pw.print(" name=\"" + normalize(project.getName()) + "\" ");
		}

		if (SupremicaProperties.generalUseSecurity())
		{
			pw.print(" owner=\"" + project.getOwner() + "\"");
			pw.print(" hash=\"" + project.getHash() + "\"");
		}

		pw.println(">");

		for (Iterator projectIt = project.iterator(); projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();

			pw.println("<Automaton name=\"" + aut.getName() + "\" type=\"" + aut.getType().toString() + "\">");

			// Print all events
			pw.println("\t<Events>");

			for (Iterator eventIt = aut.eventIterator(); eventIt.hasNext(); )
			{
				LabeledEvent event = (LabeledEvent) eventIt.next();

				pw.print("\t\t<Event id=\"" + normalize(event.getId()) + "\" label=\"" + normalize(event.getLabel()) + "\"");

				if (!event.isControllable())
				{
					pw.print(" controllable=\"false\"");
				}

				if (!event.isPrioritized())
				{
					pw.print(" prioritized=\"false\"");
				}

				if (event.isImmediate())
				{
					pw.print(" immediate=\"true\"");
				}

				if (debugMode)
				{
					pw.print(" synchIndex=" + event.getSynchIndex());
				}

				pw.println("/>");
			}

			pw.println("\t</Events>");

			// Print all states
			pw.println("\t<States>");

			for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
			{
				State state = (State) stateIt.next();

				pw.print("\t\t<State id=\"" + normalize(state.getId()) + "\"");

				if (!state.getId().equals(state.getName()))
				{
					pw.print(" name=\"" + normalize(state.getName()) + "\"");
				}

				if (state.isInitial())
				{
					pw.print(" initial=\"true\"");
				}

				if (state.isAccepting())
				{
					pw.print(" accepting=\"true\"");
				}

				if (state.isForbidden())
				{
					pw.print(" forbidden=\"true\"");
				}

				if (includeCost)
				{
					int value = state.getCost();

					if (value != State.UNDEF_COST)
					{
						pw.print(" cost=\"" + value + "\"");
					}
				}

				if (debugMode)
				{
					pw.print(" synchIndex=" + state.getIndex());
				}

				pw.println("/>");
			}

			pw.println("\t</States>");

			// Print all transitions
			pw.println("\t<Transitions>");

			for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
			{
				State sourceState = (State) stateIt.next();

				for (Iterator outgoingArcsIt = sourceState.outgoingArcsIterator(); outgoingArcsIt.hasNext(); )
				{
					Arc arc = (Arc) outgoingArcsIt.next();
					State destState = arc.getToState();

					pw.print("\t\t<Transition source=\"" + normalize(sourceState.getId()));
					pw.print("\" dest=\"" + normalize(destState.getId()));
					pw.println("\" event=\"" + normalize(arc.getEventId()) + "\"/>");
				}
			}

			pw.println("\t</Transitions>");

			// Print layout
			if (includeLayout && aut.hasLayout())
			{
				pw.println("\t<Layout>");

				// Print State Layout
				pw.println("\t\t<StatesLayout>");

				for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
				{
					State state = (State) stateIt.next();

					pw.print("\t\t\t<StateLayout id=\"" + normalize(state.getId()) + "\"");
					pw.print(" x=\"" + state.getX() + "\"");
					pw.print(" y=\"" + state.getY() + "\"");
					pw.println("/>");
				}

				pw.println("\t\t</StatesLayout>");

				// Print Transition Layout
				pw.println("\t\t<TransitionsLayout>");

				for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
				{
					State sourceState = (State) stateIt.next();

					pw.println("\t\t</TransitionsLayout>");
				}

				pw.println("\t</Layout>");
			}

			pw.println("</Automaton>");
		}

		if (includeExecution)
		{
			pw.println("<Execution>");
			pw.println("\t<Actions>");

			Actions theActions = project.getActions();

			if (theActions != null)
			{
				for (Iterator actionIt = theActions.iterator(); actionIt.hasNext(); )
				{
					Action currAction = (Action) actionIt.next();

					pw.println("\t\t<Action label=\"" + normalize(currAction.getLabel()) + "\">");

					for (Iterator cmdIt = currAction.commandIterator(); cmdIt.hasNext(); )
					{
						String currCommand = (String) cmdIt.next();

						pw.println("\t\t\t<Command command=\"" + normalize(currCommand) + "\"/>");
					}

					pw.println("\t\t</Action>");
				}
			}

			pw.println("\t</Actions>");
			pw.println("\t<Controls>");

			Controls theControls = project.getControls();

			if (theControls != null)
			{
				for (Iterator controlIt = theControls.iterator(); controlIt.hasNext(); )
				{
					Control currControl = (Control) controlIt.next();

					pw.println("\t\t<Control label=\"" + normalize(currControl.getLabel()) + "\">");

					for (Iterator condIt = currControl.conditionIterator(); condIt.hasNext(); )
					{
						String currCondition = (String) condIt.next();

						pw.println("\t\t\t<Condition condition=\"" + normalize(currCondition) + "\"/>");
					}
					pw.println("\t\t</Control>");
				}
			}

			pw.println("\t</Controls>");

			if (project.hasAnimation())
			{
				pw.println("\t<Animation path=\"" + normalize(project.getAnimationURL().toString()) + "\"/>");
			}

			pw.println("</Execution>");
		}

		pw.println("</SupremicaProject>");
		pw.flush();
	}

	public void serialize(File theFile)
		throws IOException
	{
		serialize(theFile.getAbsolutePath());
	}

	public void serialize(String fileName)
		throws IOException
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}
*/
	private String normalize(String s)
	{
		StringBuffer str = new StringBuffer();
		int len = (s != null)
				  ? s.length()
				  : 0;

		for (int i = 0; i < len; i++)
		{
			char ch = s.charAt(i);

			switch (ch)
			{

			case '<' :
			{
				str.append("&lt;");

				break;
			}
			case '>' :
			{
				str.append("&gt;");

				break;
			}
			case '&' :
			{
				str.append("&amp;");

				break;
			}
			case '"' :
			{
				str.append("&quot;");

				break;
			}
			case '\r' :
			case '\n' :
			{
				// default append char
			}
			default :
			{
				str.append(ch);
			}
			}
		}

		return str.toString();
	}
}
