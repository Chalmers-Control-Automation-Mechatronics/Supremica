
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
 * Supremica is owned and represented by KA.setProgress
 */
package org.supremica.gui;

import org.supremica.automata.algorithms.Stoppable;
import java.util.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.*;

public final class ExecutionDialog
	extends JDialog
	implements ActionListener, Runnable
{
	private List threadsToStop;
	private JPanel contentPanel = null;
	private JLabel operationLabel = null;
	private JPanel infoPanel = null;
	private JPanel progressPanel = null;
	private JLabel operationHeader = null;
	private JLabel infoValue = null;
	private JProgressBar progressBar = null;
	private JPanel currCenterPanel = null;
	private JButton stopButton = null;

	// private int progressMin = -1;
	// private int progressMax = -1;
	private int progressValue = -1;
	private int value = -1;

	// -- MF -- Changed to use a Gui instead
	// -- MF --      private Supremica workbench;
	private ExecutionDialogMode currentMode = null;
	private int nbrOfFoundStates = -1;
	private boolean newMode = true;

	private void Init(String title)
	{
		setTitle(title);
		setSize(new Dimension(250, 120));
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

		JPanel operationPanel = new JPanel(new GridLayout(2, 1));

		operationLabel = new JLabel();

		operationLabel.setHorizontalAlignment(JLabel.LEFT);

		operationHeader = new JLabel();

		operationHeader.setHorizontalAlignment(JLabel.CENTER);
		operationPanel.add(operationLabel);
		operationPanel.add(operationHeader);
		contentPanel.add(operationPanel, BorderLayout.NORTH);

		// We have two panels that we switch between, infoPanel and progressPanel
		// The infoPanel
		//infoPanel = new JPanel(new GridLayout(2, 1));
		infoPanel = new JPanel();

		//JPanel operationHeaderPanel = new JPanel();
		//operationHeader = new JLabel();
		//operationHeaderPanel.add(operationHeader, BorderLayout.CENTER);
		//JPanel infoValuePanel = new JPanel();
		infoValue = new JLabel();

		//infoValuePanel.add(infoValue, BorderLayout.CENTER);
		//infoPanel.add(operationHeaderPanel);
		//infoPanel.add(infoValuePanel);
		infoPanel.add(infoValue, BorderLayout.CENTER);

		// The progressPanel
		progressPanel = new JPanel();
		progressBar = new JProgressBar();

		progressBar.setStringPainted(true);

		//JPanel progressHeaderPanel = new JPanel();
		//progressHeaderPanel.add(operationHeader, BorderLayout.CENTER);
		//progressPanel.add(progressHeaderPanel);
		progressPanel.add(progressBar, BorderLayout.CENTER);

		// And there are some buttons (one)
		JPanel buttonPanel = new JPanel();
		stopButton = new JButton("Abort");
		stopButton.addActionListener(this);
		buttonPanel.add(stopButton);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Hit it!
		setMode(ExecutionDialogMode.uninitialized);
		show();

		/*
		setTitle(title);
		setSize(new Dimension(250, 120));
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
		operationHeader = new JLabel();

		JPanel operationHeaderPanel = new JPanel();

		infoValue = new JLabel();

		JPanel infoValuePanel = new JPanel();

		operationHeaderPanel.add(operationHeader, BorderLayout.CENTER);
		infoValuePanel.add(infoValue, BorderLayout.CENTER);
		infoPanel.add(operationHeaderPanel);
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
		*/
	}

	/**
	 * Creates dialog box for canceling the Stoppable classes in the supplied List.
	 * @see Stoppable
	 */
	public ExecutionDialog(JFrame frame, String title, List threadsToStop)
	{
		super(frame);

		setVisible(false);

		this.threadsToStop = threadsToStop;

		Init(title);
	}

	// -- MF -- Special case when you've got only one thread to watch
	public ExecutionDialog(JFrame frame, String title, Stoppable threadToStop)
	{
		this(frame, title, new ArrayList());

		addThreadToStop(threadToStop);
	}

	public void addThreadToStop(Stoppable threadToStop)
	{
		threadsToStop.add(threadToStop);
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
		// progressMin = min;
		// progressMax = max;
		progressBar.setMinimum(min);
		progressBar.setMaximum(max);

		this.progressValue = 0;

		update();
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

		// Should we replace the "value panel"
		if (currCenterPanel != null)
		{
			contentPanel.remove(currCenterPanel);
		}

		update();
	}

	public void run()
	{
		// Update labels
		if (newMode)
		{
			if (currentMode == ExecutionDialogMode.hide)
			{
				setVisible(false);

				return;
			}
			else if (!isVisible())
			{
				setVisible(true);
			}

			// Should we replace the "value panel"
			if (currCenterPanel != null)
			{
				contentPanel.remove(currCenterPanel);
			}

			// Update the dialog with the current mode
			operationLabel.setText(currentMode.getId());
			operationHeader.setText(currentMode.getText());

			if (currentMode.showValue())
			{
				contentPanel.add(infoPanel, BorderLayout.CENTER);

				currCenterPanel = infoPanel;
			}
			else if (currentMode.showProgress())
			{
				contentPanel.add(progressPanel, BorderLayout.CENTER);

				currCenterPanel = progressPanel;
			}

			/*
			if (currentMode == ExecutionDialogMode.synchronizing)
			{
					operationLabel.setText(currentMode.getId());    // "Synchronizing...");
					operationHeader.setText(currentMode.getText());    // "Number of states:");
					contentPanel.add(infoPanel, BorderLayout.CENTER);

					currCenterPanel = infoPanel;
			}
			else if (currentMode == ExecutionDialogMode.verifying)
			{
					operationLabel.setText(currentMode.getId());    // "Verifying...");
					operationHeader.setText(currentMode.getText());    // "Number of states:");
					contentPanel.add(infoPanel, BorderLayout.CENTER);

					currCenterPanel = infoPanel;
			}
			else if (currentMode == ExecutionDialogMode.synthesizing)
			{
					operationLabel.setText(currentMode.getId());    // "Synthesizing...");
					operationHeader.setText(currentMode.getText());    // "Number of states:");
					contentPanel.add(infoPanel, BorderLayout.CENTER);

					currCenterPanel = infoPanel;
			}
			else if (currentMode == ExecutionDialogMode.buildingStates)
			{
					operationLabel.setText(currentMode.getId());    // "Building states...");
					operationHeader.setText(currentMode.getText());    // "");
					contentPanel.add(progressPanel, BorderLayout.CENTER);

					currCenterPanel = progressPanel;
			}
			else if (currentMode == ExecutionDialogMode.buildingTransitions)
			{
					operationLabel.setText(currentMode.getId());    // "Building transitions...");
					operationHeader.setText(currentMode.getText());    // "");
					contentPanel.add(progressPanel, BorderLayout.CENTER);

					currCenterPanel = progressPanel;
			}
			else if (currentMode == ExecutionDialogMode.matchingStates)
			{
					operationLabel.setText(currentMode.getId());    // "Matching states...");
					operationHeader.setText(currentMode.getText());    // "");
					contentPanel.add(progressPanel, BorderLayout.CENTER);

					currCenterPanel = progressPanel;
			}
			else if (currentMode == ExecutionDialogMode.verifyingNonblocking)
			{
					operationLabel.setText(currentMode.getId());    // "Verifying nonblocking...");
					operationHeader.setText(currentMode.getText());    // "");
					contentPanel.add(progressPanel, BorderLayout.CENTER);

					currCenterPanel = progressPanel;
			}
			else if (currentMode == ExecutionDialogMode.verifyingMutualNonblockingFirstRun)
			{
					operationLabel.setText(currentMode.getId());    // "Verifying mutual nonblocking...");
					operationHeader.setText(currentMode.getText());    // "");
					contentPanel.add(progressPanel, BorderLayout.CENTER);

					currCenterPanel = progressPanel;
			}
			else if (currentMode == ExecutionDialogMode.verifyingMutualNonblockingSecondRun)
			{
					operationLabel.setText(currentMode.getId());    // "Verifying mutual nonblocking...");
					operationHeader.setText(currentMode.getText());    // "");
					contentPanel.add(progressPanel, BorderLayout.CENTER);

					currCenterPanel = progressPanel;
			}
			*/

			/*
			 * This is what it should look like - let the mode keep track of itself
			 *
			 * operationLabel.setText(currentMode.getId());    // "Matching states...");
			 * contentPanel.add(progressPanel, BorderLayout.CENTER);
			 *
			 * currCenterPanel = infoPanel;
			 *
			 */
			newMode = false;
		}

		// Update labels
		boolean showValues = currentMode.showValue();
		boolean showProgress = currentMode.showProgress();

		if (showValues)
		{
			// Don't show negative values in the dialog
			if (value >= 0)
			{
				infoValue.setText(String.valueOf(value));
			}
			else
			{
				infoValue.setText("");
			}
		}
		else if (showProgress)
		{
			progressBar.setValue(progressValue);

			//progressBar.setString(String.valueOf(Math.round(progressBar.getPercentComplete()*1000)/10.0) + "%");
			progressBar.setString(String.valueOf(Math.round(progressBar.getPercentComplete() * 100)) + "%");
		}
	}

	public void stopAllThreads()
	{
		for (Iterator exIt = threadsToStop.iterator(); exIt.hasNext(); )
		{
			((Stoppable) exIt.next()).requestStop();
		}
	}

	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();

		if (source == stopButton)
		{
			if (threadsToStop != null)
			{
				stopAllThreads();

				threadsToStop = null;    // Helping the garbage collector...
			}

			setMode(ExecutionDialogMode.hide);
		}
		else
		{
			System.err.println("Error in ExecutionDialog, unknown event occurred.");
		}
	}
}
