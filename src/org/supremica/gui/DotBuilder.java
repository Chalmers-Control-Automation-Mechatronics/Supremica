package org.supremica.gui;

import org.supremica.log.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import att.grappa.*;
import org.supremica.properties.SupremicaProperties;

public class DotBuilder
	extends Thread
{
	private DotViewerInterface theViewer = null;
	private final static int BUILD = 1;
	private final static int DRAW = 2;
	private int mode = BUILD;
	private static Logger logger = LoggerFactory.createLogger(DotBuilder.class);

	public DotBuilder(DotViewerInterface theViewer)
	{
		this.theViewer = theViewer;

		setPriority(Thread.MIN_PRIORITY);
	}

	public void run()
	{
		if (mode == BUILD)
		{
			theViewer.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			try
			{
				theViewer.internalBuild();
			}
			catch (Exception ex)
			{
				logger.error("Cannot display object.");
				logger.debug(ex.getStackTrace());
				theViewer.setCursor(java.awt.Cursor.getDefaultCursor());

				return;
			}

			mode = DRAW;

			java.awt.EventQueue.invokeLater(this);
		}
		else if (mode == DRAW)
		{
			// theViewer.setCursor(java.awt.Cursor.WAIT_CURSOR);
			theViewer.draw();
			theViewer.setCursor(java.awt.Cursor.getDefaultCursor());
		}
	}

	public void stopProcess()
	{
		if (theViewer != null)
		{
			theViewer.stopProcess();
			theViewer.setCursor(java.awt.Cursor.getDefaultCursor());
		}
	}
}