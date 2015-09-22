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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ErrorDialog
	extends JDialog
{
	private static final long serialVersionUID = 1L;
	JEditorPane text;

	public ErrorDialog(JFrame frame, String message, Exception ex)
	{
		super(frame, "Unexpected error", true);

		setSize(new Dimension(350, 250));

		// Add the stuff to the dialog
		Container pane = getContentPane();

		pane.setLayout(new BorderLayout(10, 10));

		JPanel labelPane = new JPanel();

		// Create the label
		JLabel label = new JLabel(message);
		labelPane.add(label);
		pane.add(labelPane, BorderLayout.NORTH);

		// Show stacktrace if there is an exception
		if (ex != null)
		{
			text = new JEditorPane("text/plain", message);
			text.setEditable(false);
			JScrollPane textPane = new JScrollPane(text);
			pane.add(textPane, BorderLayout.CENTER);
		}


		// Create labels
		JPanel buttonPane = new JPanel();
		JButton okButton = new JButton("OK");

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		buttonPane.add(okButton);
		pane.add(buttonPane, BorderLayout.SOUTH);


		// Center over the Supremica window
		Point point = Utility.getPosForCenter(getSize());

		setLocation(point);
		setVisible(true);
	}

	public ErrorDialog(JFrame frame, String message)
	{
		this(frame, message, null);
	}

	public static String getStackTrace(Throwable aThrowable)
	{
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}


}





