
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
package org.supremica.gui;

import org.supremica.log.*;
import java.awt.*;
import com.pietjonas.wmfwriter2d.*;
import java.io.*;
import javax.swing.*;

public class GraphicsToClipboard
{
	private static Logger logger = LoggerFactory.createLogger(DotViewer.class);
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
