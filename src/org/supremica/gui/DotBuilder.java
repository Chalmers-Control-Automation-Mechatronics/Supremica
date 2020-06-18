//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import att.grappa.Graph;
import att.grappa.Parser;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.IO.AutomataSerializer;
import org.supremica.properties.Config;


public class DotBuilder
	extends Thread
{
	private static Logger logger = LogManager.getLogger(DotBuilder.class);

	private DotBuilderGraphObserver theGraphObserver = null;
	private DotBuilderStreamObserver theStreamObserver = null;
	private AutomataSerializer theSerializer = null;
	private final static int BUILD = 1;
	private final static int DRAW = 2;
	private int mode = BUILD;
	private PrintWriter toDotWriter;
	private InputStream fromDotStream;
	private Process dotProcess;
	private final String dotArguments;
	Graph theGraph = null;

	private DotBuilder(final DotBuilderStreamObserver theStreamObserver, final DotBuilderGraphObserver theGraphObserver, final AutomataSerializer theSerializer, final String dotArguments)
	{
		this.theStreamObserver = theStreamObserver;
		this.theGraphObserver = theGraphObserver;
		this.theSerializer = theSerializer;
		this.dotArguments = dotArguments;

		setPriority(Thread.MIN_PRIORITY);
	}

	public static DotBuilder getDotBuilder(final DotBuilderStreamObserver theStreamObserver, final DotBuilderGraphObserver theGraphObserver, final AutomataSerializer theSerializer, final String dotArguments)
	{
		final DotBuilder dotBuilder = new DotBuilder(theStreamObserver, theGraphObserver, theSerializer, dotArguments);
		dotBuilder.start();
		return dotBuilder;
	}

	@Override
  public void run()
	{
		if (mode == BUILD)
		{
			//theObserver.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			try
			{
				internalBuild();
			}
			catch (final Exception ex)
			{
				logger.error("Cannot display object.");
				logger.debug(ex.getStackTrace());
//				theObserver.setCursor(java.awt.Cursor.getDefaultCursor());

				return;
			}

			mode = DRAW;

			if (theGraphObserver != null)
			{
				java.awt.EventQueue.invokeLater(this);
			}
		}
		else if (mode == DRAW)
		{
			// theObserver.setCursor(java.awt.Cursor.WAIT_CURSOR);
			if (theGraphObserver != null)
			{
				theGraphObserver.setGraph(theGraph);
			}
			//theObserver.setCursor(java.awt.Cursor.getDefaultCursor());
		}
	}


	public void stopProcess()
	{
		if (dotProcess != null)
		{
			dotProcess.destroy();

//			updateNeeded = true;
		}
	}

	private void internalBuild()
	{
		// AutomataSerializer serializer = theObserver.getSerializer();

		try
		{
			initializeStreams(dotArguments);
		}
		catch (final Exception ex)
		{
			toDotWriter.close();
			logger.debug(ex.getStackTrace());
			return;
			//throw ex;
		}

		// Send the file to dot
		try
		{
			theSerializer.serialize(toDotWriter);
		}
		catch (final Exception ex)
		{
			logger.error("Exception while serializing ", ex);
			logger.debug(ex.getStackTrace());

			return;
		}
		finally
		{
			toDotWriter.close();
		}

		if (theStreamObserver != null)
		{
			theStreamObserver.setInputStream(fromDotStream);
		}
		if (theGraphObserver != null)
		{
			parseResponse(fromDotStream);
		}
	}

	private void parseResponse(final InputStream inputStream)
	{
		// Parse the response from dot
		final Parser parser = new Parser(inputStream);

		try
		{
			parser.parse();
		}
		catch (final Exception ex)
		{
			logger.error("Exception while parsing dot file", ex);
			logger.debug(ex.getStackTrace());
			return;
		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch (final IOException ex)
			{
				logger.error("Exception while closing input stream", ex);
				logger.debug(ex.getStackTrace());
				return;
			}
		}

		try
		{
			theGraph = parser.getGraph();
		}
		catch (final Exception ex)
		{
			logger.error("Exception while getting dot graph", ex);
			logger.debug(ex.getStackTrace());
			return;
		}
	}

	private void initializeStreams(final String arguments)
	{
		final String dot_cmd = Config.DOT_EXECUTE_COMMAND.getValue() + " " + arguments;

		try
		{
			dotProcess = Runtime.getRuntime().exec(dot_cmd);
		}
		catch (final IOException ex)
		{
			logger.error("Cannot run (" + dot_cmd + "). Is dot in the path?", ex);
			return;
		}

		final OutputStream pOut = dotProcess.getOutputStream();
		final BufferedOutputStream pBuffOut = new BufferedOutputStream(pOut);

		toDotWriter = new PrintWriter(pBuffOut);
		fromDotStream = dotProcess.getInputStream();
	}

}
