//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
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

public class EditCommentDialog
	extends JDialog
{
	private static final long serialVersionUID = 1L;
	String newComment = null;
	JEditorPane text;

	public EditCommentDialog(JFrame frame, String oldComment)
	{
		super(frame, "Edit comment", true);

		setSize(new Dimension(350, 250));

		//setResizable(false);
		// Design labels and buttons
		JPanel labelPane = new JPanel();
		JLabel label;

		if (oldComment.equals(""))
		{
			label = new JLabel("Enter new project comment");
		}
		else
		{
			label = new JLabel("Edit the project comment");
		}

		labelPane.add(label);

		text = new JEditorPane("text/plain", oldComment);

		JScrollPane textPane = new JScrollPane(text);
		JPanel buttonPane = new JPanel();
		JButton okButton = new JButton("OK");

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				newComment = text.getText();

				dispose();
			}
		});

		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);

		// Add the stuff to the dialog
		Container pane = getContentPane();

		pane.setLayout(new BorderLayout(10, 10));
		pane.add(labelPane, BorderLayout.NORTH);
		pane.add(textPane, BorderLayout.CENTER);
		pane.add(buttonPane, BorderLayout.SOUTH);

		// Center over the Supremica window
		Point point = Utility.getPosForCenter(getSize());

		setLocation(point);
		setVisible(true);
	}

	public String getComment()
	{
		return newComment;
	}
}
