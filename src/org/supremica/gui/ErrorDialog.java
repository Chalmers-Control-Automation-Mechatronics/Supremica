
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

package org.supremica.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ErrorDialog
	extends JDialog
{
	String newComment = null;
	JEditorPane text;

	private ErrorDialog()
	{
	}

	public ErrorDialog(JFrame frame, String message, Exception ex)
	{

	}

	public ErrorDialog(JFrame frame, String message)
	{
		super(frame, "Error", true);

		setSize(new Dimension(350, 250));

		//setResizable(false);
		// Design labels and buttons
		JPanel labelPane = new JPanel();
		JLabel label;

		if (message.equals(""))
		{
			label = new JLabel("Enter new project comment");
		}
		else
		{
			label = new JLabel("Edit the project comment");
		}

		labelPane.add(label);

		text = new JEditorPane("text/plain", message);

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
		show();
	}

	public String getComment()
	{
		return newComment;
	}
}
