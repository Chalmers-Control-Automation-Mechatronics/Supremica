
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.apps;

import java.awt.*;
import org.supremica.log.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.gui.*;

// import org.jgrafchart.*;
public class SupremicaWithGui
{
	static
	{
		SupremicaProperties.setLogToConsole(false);
		SupremicaProperties.setLogToGUI(true);
	}

	private static Logger logger = LoggerFactory.createLogger(org.supremica.gui.Supremica.class);
	private final static InterfaceManager interfaceManager = InterfaceManager.getInstance();

	private SupremicaWithGui() {}

	public static org.supremica.gui.Supremica startSupremica()
	{
		return startSupremica(null);
	}

	public static org.supremica.gui.Supremica startSupremica(String[] args)
	{
		SplashWindow splash = new SplashWindow();

		splash.setVisible(true);

		org.supremica.gui.Supremica workbench = null;

		if (args != null)
		{
			if (args.length > 0)
			{
				workbench = new org.supremica.gui.Supremica(args[0]);
			}
			else
			{
				workbench = new org.supremica.gui.Supremica(null);
			}
		}
		else
		{
			workbench = new org.supremica.gui.Supremica(null);
		}

		boolean packFrame = false;

		// Validate frames that have preset sizes
		// Pack frames that have useful preferred size info, e.g. from their layout
		if (packFrame)
		{
			workbench.pack();
		}
		else
		{
			workbench.validate();
		}

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = workbench.getSize();

		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}

		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}

		workbench.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		workbench.initialize();
		splash.setVisible(false);
		workbench.setVisible(true);

		PreLoader preLoader = PreLoader.getPreLoader();

		return workbench;
	}
}
