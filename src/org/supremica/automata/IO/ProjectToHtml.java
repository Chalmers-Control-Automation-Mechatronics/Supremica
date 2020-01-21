
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.gui.ExportFormat;
import org.supremica.properties.Config;


public class ProjectToHtml
{
	private static Logger logger = LogManager.getLogger(ProjectToHtml.class);
	private final Project project;
	private final File directory;
	@SuppressWarnings("unused")
	private final boolean overwrite;
	private final int maxNbrOfStatesInPng;
	private Process dotProcess;
	private PrintWriter toDotWriter;
	private InputStream fromDotStream;

	public ProjectToHtml(final Project project, final File directory)
	{
		this.overwrite = false;
		this.project = project;
		this.directory = directory;

		if (!directory.isDirectory())
		{
			logger.error("ProjectToHtml: You must export to a directory");
		}

		maxNbrOfStatesInPng = Config.DOT_MAX_NBR_OF_STATES.getValue();
	}

	public void serialize()
		throws Exception
	{
		project.setIndices();
		serializeProject();

		// Serialize all automata
		for (final Iterator<Automaton> projectIt = project.iterator(); projectIt.hasNext(); )
		{
			final Automaton aut = projectIt.next();

			serializeAutomaton(aut);
		}

		// Serialize all events
		final Alphabet projectAlphabet = AlphabetHelpers.getUnionAlphabet(project, true, true);    // project.getUnionAlphabet();

		for (final Iterator<LabeledEvent> alphIt = projectAlphabet.iterator(); alphIt.hasNext(); )
		{
			final LabeledEvent ev = alphIt.next();

			serializeEvent(ev);
		}
	}

	public void serializeProject()
		throws Exception
	{
		final PrintWriter pw = getPrintWriter("index.html");

		printHtmlBegin(pw, "Supremica Project: " + project.getName());
		pw.println("<h1>Project: " + EncodingHelper.normalize(project.getName(), ExportFormat.HTML) + "</h1>");
		pw.println("<h2>Plants:</h2>");
		pw.println("<ul>");

		for (final Iterator<Automaton> projectIt = project.plantIterator();
				projectIt.hasNext(); )
		{
			final Automaton aut = projectIt.next();

			pw.println("<li> <a href=\"automaton" + aut.getIndex() + ".html\">" + EncodingHelper.normalize(aut.getName(), ExportFormat.HTML) + "</a></li>");
		}

		pw.println("</ul>");
		pw.println("<h2>Specifications:</h2>");
		pw.println("<ul>");

		for (final Iterator<Automaton> projectIt = project.specificationIterator();
				projectIt.hasNext(); )
		{
			final Automaton aut = projectIt.next();

			pw.println("<li> <a href=\"automaton" + aut.getIndex() + ".html\">" + EncodingHelper.normalize(aut.getName(), ExportFormat.HTML) + "</a></li>");
		}

		pw.println("</ul>");
		pw.println("<h2>Supervisors:</h2>");
		pw.println("<ul>");

		for (final Iterator<Automaton> projectIt = project.supervisorIterator();
				projectIt.hasNext(); )
		{
			final Automaton aut = projectIt.next();

			pw.println("<li> <a href=\"automaton" + aut.getIndex() + ".html\">" + EncodingHelper.normalize(aut.getName(), ExportFormat.HTML) + "</a></li>");
		}

		pw.println("</ul>");
		printHtmlEnd(pw);
	}

	public void serializeAutomaton(final Automaton theAutomaton)
		throws Exception
	{
		final PrintWriter pw = getPrintWriter("automaton" + theAutomaton.getSynchIndex() + ".html");

		printHtmlBegin(pw, "Supremica Automaton: " + theAutomaton.getName());
		pw.println("<h1>Automaton: " + EncodingHelper.normalize(theAutomaton.getName(), ExportFormat.HTML) + "</h1>");
		pw.println("<h2>Type: " + theAutomaton.getType() + "</h2>");
		pw.println("<h2>Alphabet:</h2>");
		pw.println("<ul>");

		for (final Iterator<LabeledEvent> eventIt = theAutomaton.eventIterator();
				eventIt.hasNext(); )
		{
			final LabeledEvent event = eventIt.next();

			pw.println("<li> <a href=\"event" + event.getIndex() + ".html\">" + EncodingHelper.normalize(event.getLabel(), ExportFormat.HTML) + "</a></li>");
		}

		pw.println("</ul>");

		final boolean pngCreated = createPngFile(theAutomaton);

		if (pngCreated)
		{
			pw.println("<center>");
			pw.println("<img src=\"automaton" + theAutomaton.getSynchIndex() + ".png\"</img>");
			pw.println("</center>");
		}

		printHtmlEnd(pw);
	}

	public void serializeEvent(final LabeledEvent theEvent)
		throws Exception
	{
		final PrintWriter pw = getPrintWriter("event" + theEvent.getIndex() + ".html");

		printHtmlBegin(pw, "Supremica Event: " + theEvent.getLabel());
		pw.println("<h1>Event: " + EncodingHelper.normalize(theEvent.getLabel(), ExportFormat.HTML) + "</h1>");
		pw.println("<ul>");
		pw.println("<li>" + "Controllable: " + theEvent.isControllable() + "</li>");
		pw.println("<li>" + "Immediate: " + theEvent.isImmediate() + "</li>");
		pw.println("</ul>");
		pw.println("<h2>Prioritized in:</h2>");
		pw.println("<h2>Broadcast in:</h2>");
		printHtmlEnd(pw);
	}

	private void printHtmlBegin(final PrintWriter pw, final String title)
	{
		pw.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
		pw.println("<html>");
		pw.println("<head>");
		pw.println("<title>" + EncodingHelper.normalize(title, ExportFormat.HTML) + "</title>");
		pw.println("</head>");
		pw.println("<body bgcolor=\"#FFFFFF\">");
	}

	private void printHtmlEnd(final PrintWriter pw)
	{
		pw.println("</body>");
		pw.println("</html>");
		pw.flush();
		pw.close();
	}

	private boolean createPngFile(final Automaton theAutomaton)
		throws Exception
	{
		if (theAutomaton.nbrOfStates() > maxNbrOfStatesInPng)
		{
			return false;
		}

		final File currFile = getFile("automaton" + theAutomaton.getSynchIndex() + ".png");

		if (currFile != null)
		{
			if (!currFile.isDirectory())
			{
				try
				{
					final AutomatonToDot exporter = new AutomatonToDot(theAutomaton);

					exporter.setUseStateColors(true);
					exporter.setUseArcColors(true);

					try
					{
						dotProcess = Runtime.getRuntime().exec(Config.DOT_EXECUTE_COMMAND.getValue() + " -Tpng");
					}
					catch (final IOException ex)
					{
						logger.error("Cannot run dot. Make sure dot is in the path.");
						logger.debug(ex.getStackTrace());

						throw ex;
					}

					final OutputStream pOut = dotProcess.getOutputStream();
					final BufferedOutputStream pBuffOut = new BufferedOutputStream(pOut);

					toDotWriter = new PrintWriter(pBuffOut);
					fromDotStream = dotProcess.getInputStream();

					// Send the file to dot
					exporter.serialize(toDotWriter);
					toDotWriter.close();

					// Send the response to a file
					final FileOutputStream fw = new FileOutputStream(currFile);
					final BufferedOutputStream buffOutStream = new BufferedOutputStream(fw);
					final BufferedInputStream buffInStream = new BufferedInputStream(fromDotStream);
					int currChar = buffInStream.read();

					while (currChar != -1)
					{
						buffOutStream.write(currChar);

						currChar = buffInStream.read();
					}

					buffInStream.close();
					buffOutStream.close();
				}
				catch (final Exception ex)
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
	private File getFile(final String name)
		throws Exception
	{
		final File newFile = new File(directory, name);

		return newFile;
	}

	/**
	 * Given a name create the corresponding file and return a PrintWriter to that object..
	 */
	private PrintWriter getPrintWriter(final String name)
		throws Exception
	{
		return new PrintWriter(new FileWriter(getFile(name)));
	}
}
