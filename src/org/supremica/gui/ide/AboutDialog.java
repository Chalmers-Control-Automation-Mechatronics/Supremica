
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
package org.supremica.gui.ide;

import org.supremica.Version;
import org.supremica.gui.Utility;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


public class AboutDialog
	extends JDialog
	implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private JButton okButton;
	public AboutDialog(Frame parent)
	{
		super(parent, true);
		Utility.setupDialog(this, 400, 300);

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		Container thisPanel = getContentPane();

		thisPanel.add(new JLabel(new ImageIcon(getClass().getResource("/icons/Supremica.gif"))), BorderLayout.WEST);
		JTextArea textArea = new JTextArea();
		textArea.append("<b>Supremica</b>\n");
		textArea.append("Version: " + Version.version() + "\n");
		textArea.append("Copyright (c) 1999-2005\n");
		textArea.append("Knut \u00c5kesson, Martin Fabian, Hugo Flordal,\n");
		textArea.append("Anders Hellgren, Robi Malik, Arash Vahidi.\n");
		textArea.setEditable(false);
		thisPanel.add(textArea, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		okButton = new JButton("Ok");
		okButton.addActionListener(this);

		buttonPanel.add(Utility.setDefaultButton(this, okButton));

		thisPanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	protected void processWindowEvent(WindowEvent e)
	{
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			cancel();
		}

		super.processWindowEvent(e);
	}

	void cancel()
	{
		hide();
		dispose();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == okButton)
		{
			cancel();
		}
	}
}
