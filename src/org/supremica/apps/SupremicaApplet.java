
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
import javax.swing.*;
import org.supremica.log.*;
import org.supremica.gui.*;
import org.supremica.properties.SupremicaProperties;

public class SupremicaApplet
	extends JApplet
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(org.supremica.gui.Supremica.class);
	private org.supremica.gui.Supremica workbench;
	private SplashWindow splash;

	public SupremicaApplet() {}

	private void doSplash()
	{
		splash = new SplashWindow();

		splash.setVisible(true);
	}

	private void setProperties()
	{
		SupremicaProperties.setFileAllowOpen(false);
		SupremicaProperties.setFileAllowSave(false);
		SupremicaProperties.setFileAllowImport(false);
		SupremicaProperties.setFileAllowExport(false);
		SupremicaProperties.setFileAllowQuit(false);
		SupremicaProperties.setFileAllowExport(false);
		SupremicaProperties.setXmlRpcActive(false);
		SupremicaProperties.setUseDot(false);
	}

	public void init()
	{
		setProperties();
		setSize(200, 100);
		setBackground(Color.gray);

		Panel topPanel = new Panel();

		topPanel.setLayout(new BorderLayout());
		getContentPane().add(topPanel);

		Label labelHello = new Label("Starting Supremica...");

		topPanel.add(labelHello, BorderLayout.NORTH);
		doSplash();

		if (workbench == null)
		{
			workbench = new org.supremica.gui.Supremica();
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
	}

	public void destroy()
	{
		if (workbench != null)
		{
			workbench.destroy();
		}
	}
}
