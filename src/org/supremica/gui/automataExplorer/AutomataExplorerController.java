
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
package org.supremica.gui.automataExplorer;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.automata.Automata;

public class AutomataExplorerController
	extends JPanel
{
	private AutomataStateViewer stateViewer;
	private Automata theAutomata;
	private JButton undoButton;
	private JButton redoButton;

	public AutomataExplorerController(AutomataStateViewer stateViewer, AutomataSynchronizerHelper synchHelper)
	{
		setLayout(new BorderLayout());

		this.stateViewer = stateViewer;
		this.theAutomata = synchHelper.getAutomata();

		Box redoBox = new Box(BoxLayout.X_AXIS);
		ImageIcon forwardImg = new ImageIcon(AutomataExplorerController.class.getResource("/toolbarButtonGraphics/navigation/Forward24.gif"));
		ImageIcon backwardImg = new ImageIcon(AutomataExplorerController.class.getResource("/toolbarButtonGraphics/navigation/Back24.gif"));
		ImageIcon homeImg = new ImageIcon(AutomataExplorerController.class.getResource("/toolbarButtonGraphics/navigation/Home24.gif"));

		undoButton = new JButton(backwardImg);

		undoButton.setToolTipText("Back");

		redoButton = new JButton(forwardImg);

		redoButton.setToolTipText("Forward");

		JButton resetButton = new JButton(homeImg);

		resetButton.setToolTipText("Go to the initial state");
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(undoButton);
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(redoButton);
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(resetButton);
		redoBox.add(Box.createHorizontalGlue());
		redoBox.add(Box.createHorizontalGlue());
		add(redoBox, BorderLayout.NORTH);
		undoButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				undo_actionPerformed(e);
			}
		});
		redoButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				redo_actionPerformed(e);
			}
		});
		resetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				reset_actionPerformed(e);
			}
		});
	}

	public void reset_actionPerformed(ActionEvent e)
	{
		stateViewer.goToInitialState();

		// stateViewer.initialize();
	}

	public void undo_actionPerformed(ActionEvent e)
	{
		stateViewer.undoState();
	}

	public void redo_actionPerformed(ActionEvent e)
	{
		stateViewer.redoState();
	}

	public void update()
	{
		undoButton.setEnabled(stateViewer.undoEnabled());
		redoButton.setEnabled(stateViewer.redoEnabled());
	}
}
