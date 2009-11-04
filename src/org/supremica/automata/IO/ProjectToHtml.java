
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

import java.io.*;
import java.util.*;
import org.supremica.properties.Config;
import org.supremica.automata.*;
import org.supremica.log.*;

public class ProjectToHtml
{
	private static Logger logger = LoggerFactory.createLogger(ProjectToHtml.class);
	private Project project;
	private File directory;
	@SuppressWarnings("unused")
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

		maxNbrOfStatesInPng = Config.DOT_MAX_NBR_OF_STATES.get();
	}

	public void serialize()
		throws Exception
	{
		project.setIndices();
		serializeProject();

		// Serialize all automata
		for (Iterator<Automaton> projectIt = project.iterator(); projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();

			serializeAutomaton(aut);
		}

		// Serialize all events
		Alphabet projectAlphabet = AlphabetHelpers.getUnionAlphabet(project, true, true);    // project.getUnionAlphabet();

		for (Iterator<LabeledEvent> alphIt = projectAlphabet.iterator(); alphIt.hasNext(); )
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
		pw.println("<h1>Project: " + EncodingHelper.normalize(project.getName()) + "</h1>");
		pw.println("<h2>Plants:</h2>");
		pw.println("<ul>");

		for (Iterator<Automaton> projectIt = project.plantIterator();
				projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();

			pw.println("<li> <a href=\"automaton" + aut.getIndex() + ".html\">" + EncodingHelper.normalize(aut.getName()) + "</a></li>");
		}

		pw.println("</ul>");
		pw.println("<h2>Specifications:</h2>");
		pw.println("<ul>");

		for (Iterator<Automaton> projectIt = project.specificationIterator();
				projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();

			pw.println("<li> <a href=\"automaton" + aut.getIndex() + ".html\">" + EncodingHelper.normalize(aut.getName()) + "</a></li>");
		}

		pw.println("</ul>");
		pw.println("<h2>Supervisors:</h2>");
		pw.println("<ul>");

		for (Iterator<Automaton> projectIt = project.supervisorIterator();
				projectIt.hasNext(); )
		{
			Automaton aut = (Automaton) projectIt.next();

			pw.println("<li> <a href=\"automaton" + aut.getIndex() + ".html\">" + EncodingHelper.normalize(aut.getName()) + "</a></li>");
		}

		pw.println("</ul>");
		printHtmlEnd(pw);
	}

	public void serializeAutomaton(Automaton theAutomaton)
		throws Exception
	{
		PrintWriter pw = getPrintWriter("automaton" + theAutomaton.getSynchIndex() + ".html");

		printHtmlBegin(pw, "Supremica Automaton: " + theAutomaton.getName());
		pw.println("<h1>Automaton: " + EncodingHelper.normalize(theAutomaton.getName()) + "</h1>");
		pw.println("<h2>Type: " + theAutomaton.getType() + "</h2>");
		pw.println("<h2>Alphabet:</h2>");
		pw.println("<ul>");

		for (Iterator<LabeledEvent> eventIt = theAutomaton.eventIterator();
				eventIt.hasNext(); )
		{
			LabeledEvent event = (LabeledEvent) eventIt.next();

			pw.println("<li> <a href=\"event" + event.getIndex() + ".html\">" + EncodingHelper.normalize(event.getLabel()) + "</a></li>");
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
		PrintWriter pw = getPrintWriter("event" + theEvent.getIndex() + ".html");

		printHtmlBegin(pw, "Supremica Event: " + theEvent.getLabel());
		pw.println("<h1>Event: " + EncodingHelper.normalize(theEvent.getLabel()) + "</h1>");
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
		pw.println("<title>" + EncodingHelper.normalize(title) + "</title>");
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

					exporter.setUseStateColors(true);
					exporter.setUseArcColors(true);

					try
					{
						dotProcess = Runtime.getRuntime().exec(Config.DOT_EXECUTE_COMMAND.get() + " -Tpng");
					}
					catch (IOException ex)
					{
						logger.error("Cannot run dot. Make sure dot is in the path.");
						logger.debug(ex.getStackTrace());

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
					logger.debug(ex.getStackTrace());

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
}
