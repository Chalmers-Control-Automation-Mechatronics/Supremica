package org.supremica.gui;

import org.supremica.log.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import att.grappa.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.IO.AutomataSerializer;

public class DotBuilder
	extends Thread
{
	private static Logger logger = LoggerFactory.createLogger(DotBuilder.class);

	private DotBuilderObserver theObserver = null;
	private AutomataSerializer theSerializer = null;
	private final static int BUILD = 1;
	private final static int DRAW = 2;
	private int mode = BUILD;
	private PrintWriter toDotWriter;
	private InputStream fromDotStream;
	private Process dotProcess;
	Graph theGraph = null;

	private DotBuilder(DotBuilderObserver theObserver, AutomataSerializer theSerializer)
	{
		this.theObserver = theObserver;
		this.theSerializer = theSerializer;

		setPriority(Thread.MIN_PRIORITY);
	}

	public static DotBuilder getDotBuilder(DotBuilderObserver theObserver, AutomataSerializer theSerializer)
	{
		DotBuilder dotBuilder = new DotBuilder(theObserver, theSerializer);
		dotBuilder.start();
		return dotBuilder;
	}

	public void run()
	{
		if (mode == BUILD)
		{
			//theObserver.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			try
			{
				internalBuild();
			}
			catch (Exception ex)
			{
				logger.error("Cannot display object.");
				logger.debug(ex.getStackTrace());
//				theObserver.setCursor(java.awt.Cursor.getDefaultCursor());

				return;
			}

			mode = DRAW;

			java.awt.EventQueue.invokeLater(this);
		}
		else if (mode == DRAW)
		{
			// theObserver.setCursor(java.awt.Cursor.WAIT_CURSOR);
			theObserver.setGraph(theGraph);
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
			initializeStreams("");
		}
		catch (Exception ex)
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
		catch (Exception ex)
		{
			logger.error("Exception while serializing ", ex);
			logger.debug(ex.getStackTrace());

			return;
		}
		finally
		{
			toDotWriter.close();
		}

		parseResponse(fromDotStream);
	}

	private void parseResponse(InputStream inputStream)
	{

		// Parse the response from dot
		Parser parser = new Parser(inputStream);

		try
		{
			parser.parse();
		}
		catch (Exception ex)
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
			catch (IOException ex)
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
		catch (Exception ex)
		{
			logger.error("Exception while getting dot graph", ex);
			logger.debug(ex.getStackTrace());
			return;
		}
	}

	private void initializeStreams(String arguments)
		throws Exception
	{
		try
		{
			dotProcess = Runtime.getRuntime().exec(SupremicaProperties.getDotExecuteCommand() + " " + arguments);
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
	}

}