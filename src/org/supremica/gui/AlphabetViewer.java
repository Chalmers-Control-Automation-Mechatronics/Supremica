
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

import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;
import org.supremica.automata.Automaton;

public class AlphabetViewer
	extends JFrame
{
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private AlphabetViewerPanel alphabetPanel;

	public AlphabetViewer(Automaton theAutomaton)
		throws Exception
	{
		alphabetPanel = new AlphabetViewerPanel(theAutomaton);
		contentPane = (JPanel) getContentPane();

		// contentPane.setLayout(new BorderLayout());
		// contentPane.add(toolBar, BorderLayout.NORTH);
		setTitle("Alphabet Viewer: " + theAutomaton.getName());
		setSize(400, 500);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				setVisible(false);
				dispose();
			}
		});

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();

		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}

		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}

		setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		setIconImage(Supremica.cornerImage);
		initMenubar();
		contentPane.add(alphabetPanel, BorderLayout.CENTER);
	}

	private void initMenubar()
	{
		setJMenuBar(menuBar);

		// File
		JMenu menuFile = new JMenu();

		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);

		// File.Close
		JMenuItem menuFileClose = new JMenuItem();

		menuFileClose.setText("Close");
		menuFile.add(menuFileClose);
		menuBar.add(menuFile);
		menuFileClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				dispose();
			}
		});
	}

	public void initialize() {}
}
