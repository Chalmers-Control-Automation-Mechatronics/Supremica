
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



import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.Stoppable;

import java.util.*;

import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.GridLayout;
import java.awt.BorderLayout;

import javax.swing.*;


public class ExecutionDialog
	extends JDialog
	implements ActionListener, Runnable
{

	private List executers;
	private JPanel contentPanel = null;
	private JLabel operationLabel = null;
	private JPanel infoPanel = null;
	private JPanel progressPanel = null;
	private JLabel infoHeader = null;
	private JLabel infoValue = null;
	private JProgressBar progressBar = null;
	private JPanel currCenterPanel = null;
	private JButton stopButton = null;
	private int progressMin = -1;
	private int progressMax = -1;
	private int progressValue = -1;
	private int value = -1;

	// -- MF -- Changed to use a Gui instead
	// -- MF --      private Supremica workbench;
	private ExecutionDialogMode currentMode = null;
	private int nbrOfFoundStates = -1;
	private boolean newMode = true;

	/**
	 * Creates dialog box for canceling the Stoppable classes in the supplied List.
	 * @see Stoppable
	 */
	public ExecutionDialog(	/* Supremica workbench */Gui workbench, String title, List executers)
	{

		super(workbench.getFrame());

		// this.workbench = workbench;
		this.executers = executers;

		setTitle(title);
		setSize(new Dimension(240, 110));
		setResizable(false);

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

		contentPanel = (JPanel) getContentPane();
		operationLabel = new JLabel();

		contentPanel.add(operationLabel, BorderLayout.NORTH);

		// We have two panels that we switch between
		infoPanel = new JPanel(new GridLayout(2, 1));
		infoHeader = new JLabel();

		JPanel infoHeaderPanel = new JPanel();

		infoValue = new JLabel();

		JPanel infoValuePanel = new JPanel();

		infoHeaderPanel.add(infoHeader, BorderLayout.CENTER);
		infoValuePanel.add(infoValue, BorderLayout.CENTER);
		infoPanel.add(infoHeaderPanel);
		infoPanel.add(infoValuePanel);

		progressPanel = new JPanel();
		progressBar = new JProgressBar();

		progressPanel.add(progressBar, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		stopButton = new JButton("Abort");

		stopButton.addActionListener(this);
		buttonPanel.add(stopButton);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		setMode(ExecutionDialogMode.uninitialized);
		show();
	}

	public void setMode(ExecutionDialogMode mode)
	{

		currentMode = mode;

		updateMode();
	}

	/**
	 * This must be called before changing mode to a progressMode.
	 */
	public void initProgressBar(int min, int max)
	{
		progressMin = min;
		progressMax = max;
	}

	public void setProgress(int progressValue)
	{

		this.progressValue = progressValue;

		update();
	}

	public void setValue(int value)
	{

		this.value = value;

		update();
	}

	private void update()
	{
		java.awt.EventQueue.invokeLater(this);
	}

	private void updateMode()
	{

		newMode = true;

		update();
	}

	public void run()
	{

		// Update labels
		if (newMode)
		{
			if (currCenterPanel != null)
			{
				contentPanel.remove(currCenterPanel);
			}

			if (currentMode == ExecutionDialogMode.synchronizing)
			{
				operationLabel.setText("Synchronizing...");
				infoHeader.setText("Number of states:");
				contentPanel.add(infoPanel, BorderLayout.CENTER);

				currCenterPanel = infoPanel;
			}
			else if (currentMode == ExecutionDialogMode.verifying)
			{
				operationLabel.setText("Verifying...");
				infoHeader.setText("Number of states:");
				contentPanel.add(infoPanel, BorderLayout.CENTER);

				currCenterPanel = infoPanel;
			}
			else if (currentMode == ExecutionDialogMode.synthesizing)
			{
				operationLabel.setText("Synthesizing...");
				infoHeader.setText("Number of states:");
				contentPanel.add(infoPanel, BorderLayout.CENTER);

				currCenterPanel = infoPanel;
			}
			else if (currentMode == ExecutionDialogMode.buildingStates)
			{
				operationLabel.setText("Building states...");
				contentPanel.add(progressPanel, BorderLayout.CENTER);

				currCenterPanel = infoPanel;
			}
			else if (currentMode == ExecutionDialogMode.buildingTransitions)
			{
				operationLabel.setText("Building transitions...");
				contentPanel.add(progressPanel, BorderLayout.CENTER);

				currCenterPanel = infoPanel;
			}
			else if (currentMode == ExecutionDialogMode.hide)
			{		// Do nothing
			}

			newMode = false;
		}

		// Update labels
		boolean showValues = ((currentMode == ExecutionDialogMode.synchronizing) || (currentMode == ExecutionDialogMode.verifying) || (currentMode == ExecutionDialogMode.synthesizing));
		boolean showProgress = ((currentMode == ExecutionDialogMode.buildingStates) || (currentMode == ExecutionDialogMode.buildingTransitions));

		if (showValues)
		{
			infoValue.setText(String.valueOf(value));
		}
		else if (showProgress)
		{
			progressBar.setValue(progressValue);
		}
		else if (currentMode == ExecutionDialogMode.hide)
		{
			setVisible(false);
			dispose();
		}
	}

	public void actionPerformed(ActionEvent event)
	{

		Object source = event.getSource();

		if (source == stopButton)
		{
			if (executers != null)
			{
				for (Iterator exIt = executers.iterator(); exIt.hasNext(); )
				{
					((Stoppable) exIt.next()).requestStop();
				}

				executers = null;		// Helping the garbage collector...
			}

			setMode(ExecutionDialogMode.hide);
		}
		else
		{
			System.err.println("Error in ExecutionDialog, unknown event occurred.");
		}
	}
}
