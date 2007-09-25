
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
package org.supremica.gui.ide;

import org.supremica.Version;
import org.supremica.gui.Utility;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AboutDialog
	extends JDialog
	implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private JButton okButton;
	public AboutDialog(Frame parent)
	{
		super(parent, true);
		Utility.setupDialog(this, 600, 500);
		setTitle("About Supremica");

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		Container thisPanel = getContentPane();

		//thisPanel.add(new JLabel(new ImageIcon(getClass().getResource("/icons/Supremica.gif"))), BorderLayout.WEST);
		JTextArea textArea = new JTextArea();
		textArea.append("Supremica\n");
		textArea.append("Version: " + Version.version() + "\n");
		textArea.append("Copyright (c) 1999-2007\n");
		textArea.append("Knut \u00c5kesson, Martin Fabian, Hugo Flordal, Robi Malik\n");
		textArea.append("Arash Vahidi, Markus Sk\u00F6ldstam, Goran \u010Cengi\u0107\n\n");
		textArea.append("Supremica is currently a joint project between Department\nof Signals and Systems, Chalmers University of Technology, Sweden\nand Department of Computer Science, University of Waikato, New Zealand\n\n");
		textArea.append("Supremica includes technology from the Waters project developed at\n");
		textArea.append("Department of Computer Science, University of Waikato, New Zealand\n\n");
		textArea.append("Supremica is release using the Supremica Software License Agreement\n\n");
		textArea.append(license);

		textArea.setEditable(false);
		textArea.setSelectionStart(0);
		textArea.setSelectionEnd(0);
		JScrollPane scrollPanel = new JScrollPane(textArea);
		thisPanel.add(scrollPanel, BorderLayout.CENTER);
		scrollPanel.getVerticalScrollBar().setValue(0);
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
		setVisible(false);
		dispose();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == okButton)
		{
			cancel();
		}
	}

	private static final String license = "Supremica Software License Agreement\nThe Supremica software is not in the public domain\nHowever, it is freely available without fee for education,\nresearch, and non-profit purposes.  By obtaining copies of\nthis and other files that comprise the Supremica software,\nyou, the Licensee, agree to abide by the following\nconditions and understandings with respect to the\ncopyrighted software:\n\nThe software is copyrighted in the name of Supremica,\nand ownership of the software remains with Supremica.\n\nPermission to use, copy, and modify this software and its\ndocumentation for education, research, and non-profit\npurposes is hereby granted to Licensee, provided that the\ncopyright notice, the original author's names and unit\nidentification, and this permission notice appear on all\nsuch copies, and that no charge be made for such copies.\nAny entity desiring permission to incorporate this software\ninto commercial products or to use it for commercial\npurposes should contact:\n\nKnut Akesson (KA), knut@supremica.org\nSupremica,\nNorra Breviksvägen 10\nSE-421 67 Västra Frölunda\nSWEDEN\n\nto discuss license terms. No cost evaluation licenses are\navailable.\n\nLicensee may not use the name, logo, or any other symbol\nof Supremica nor the names of any of its employees nor\nany adaptation thereof in advertising or publicity\npertaining to the software without specific prior written\napproval of the Supremica.\n\nSUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE\nSUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.\nIT IS PROVIDED \"AS IS\" WITHOUT EXPRESS OR IMPLIED WARRANTY.\n\nSupremica or KA shall not be liable for any damages\nsuffered by Licensee from the use of this software.\n\nSupremica is owned and represented by KA.\n";
}
