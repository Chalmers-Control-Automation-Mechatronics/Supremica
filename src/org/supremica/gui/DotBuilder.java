package org.supremica.gui;

import org.supremica.log.*;
import java.io.*;
import att.grappa.*;
import org.supremica.properties.Config;
import org.supremica.automata.IO.AutomataSerializer;

public class DotBuilder
	extends Thread
{
	private static Logger logger = LoggerFactory.createLogger(DotBuilder.class);

	private DotBuilderGraphObserver theGraphObserver = null;
	private DotBuilderStreamObserver theStreamObserver = null;
	private AutomataSerializer theSerializer = null;
	private final static int BUILD = 1;
	private final static int DRAW = 2;
	private int mode = BUILD;
	private PrintWriter toDotWriter;
	private InputStream fromDotStream;
	private Process dotProcess;
	private String dotArguments;
	Graph theGraph = null;

	private DotBuilder(DotBuilderStreamObserver theStreamObserver, DotBuilderGraphObserver theGraphObserver, AutomataSerializer theSerializer, String dotArguments)
	{
		this.theStreamObserver = theStreamObserver;
		this.theGraphObserver = theGraphObserver;
		this.theSerializer = theSerializer;
		this.dotArguments = dotArguments;

		setPriority(Thread.MIN_PRIORITY);
	}

	public static DotBuilder getDotBuilder(DotBuilderStreamObserver theStreamObserver, DotBuilderGraphObserver theGraphObserver, AutomataSerializer theSerializer, String dotArguments)
	{
		DotBuilder dotBuilder = new DotBuilder(theStreamObserver, theGraphObserver, theSerializer, dotArguments);
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

		if (theStreamObserver != null)
		{
			theStreamObserver.setInputStream(fromDotStream);
		}
		if (theGraphObserver != null)
		{
			parseResponse(fromDotStream);
		}
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
	{
		try
		{
			dotProcess = Runtime.getRuntime().exec(Config.DOT_EXECUTE_COMMAND.get() + " " + arguments);
		}
		catch (IOException ex)
		{
			logger.error("Cannot run dot. Make sure dot is in the path.", ex);
			return;
		}

		OutputStream pOut = dotProcess.getOutputStream();
		BufferedOutputStream pBuffOut = new BufferedOutputStream(pOut);

		toDotWriter = new PrintWriter(pBuffOut);
		fromDotStream = dotProcess.getInputStream();
	}

}