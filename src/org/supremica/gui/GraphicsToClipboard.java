//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
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

import org.supremica.log.*;
import java.awt.*;
import com.pietjonas.wmfwriter2d.*;
import java.io.*;

public class GraphicsToClipboard
{
	private static Logger logger = LoggerFactory.createLogger(GraphicsToClipboard.class);
	private static GraphicsToClipboard theInstance = null;

	private ClipboardCopy clipboardCopy = null;

	private int width = 0;
	private int height = 0;
	private WMF wmf = null;

	private GraphicsToClipboard()
	{
		try
		{
			clipboardCopy = new ClipboardCopy();
		}
		catch (Exception ex)
		{
			logger.error(ex);
		}
	}

	public static GraphicsToClipboard getInstance()
	{
		if (theInstance == null)
		{
			theInstance = new GraphicsToClipboard();
		}
		return theInstance;
	}


	/** First call this to get a graphics object, then send the Graphics object
	 * to paint to fill it in. After this call copyToClipboard.
	 **/
	public Graphics getGraphics(int width, int height)
	{
		wmf = new WMF();
		WMFGraphics2D wmfg = new WMFGraphics2D(wmf, width, height);
		return wmfg;
	}

	public void copyToClipboard()
	{
		File temp = null;
		try
		{
			 temp = File.createTempFile("SupremicaWMF", ".wmf");
		}
		catch (IOException ex)
		{
			logger.error(ex);
			return;
		}

		temp.deleteOnExit();

		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(temp.getAbsolutePath());
		}
		catch (FileNotFoundException ex)
		{
			logger.error(ex);
			return;
		}

		try
		{
			wmf.writeWMF(out);
			//wmf.writePlaceableWMF(out, 0, 0, width, height, Toolkit.getDefaultToolkit().getScreenResolution());
		}
		catch (IOException ex)
		{
			logger.error(ex);
			return;
		}

		try
		{
			out.close();
		}
		catch (IOException ex)
		{
			logger.error(ex);
			return;
		}

		int result = clipboardCopy.copyWithPixelSize(temp.getAbsolutePath(), width, height, false);
		if (result != 0)
		{
			logger.error("Error when copying to clipboard, error code: " + result);
		}
		temp.delete();

		copyToFile();
	}


	public void copyToFile()
	{
		File temp = new File("c:/temp/SupremicaWMF.wmf");

		temp.deleteOnExit();

		//save the WMF to an OutputStream with a placeable WMF header
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(temp.getAbsolutePath());
		}
		catch (FileNotFoundException ex)
		{
			logger.error(ex);
			return;
		}

		try
		{
			wmf.writePlaceableWMF(out, 0, 0, width, height, Toolkit.getDefaultToolkit().getScreenResolution());
		}
		catch (IOException ex)
		{
			logger.error(ex);
			return;
		}

		try
		{
			out.close();
		}
		catch (IOException ex)
		{
			logger.error(ex);
			return;
		}

	}


}





