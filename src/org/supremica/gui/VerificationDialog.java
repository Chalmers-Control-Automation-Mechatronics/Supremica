
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

import org.supremica.automata.algorithms.VerificationOptions;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.VerificationAlgorithm;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

interface VerificationPanel
{
	void update(VerificationOptions v);

	void regain(VerificationOptions v);
}


class VerificationDialogStandardPanel
	extends JPanel
	implements VerificationPanel, ActionListener
{
	private JComboBox verificationTypeBox;
	private AlgorithmSelector algorithmSelector;
	private JTextArea nbNote;
	//private JTextArea note;// = new JTextArea("Bananas...");

	//final String[] verificationData = { "Controllability",	// keep them in this order, for God's sake! 
	//									"Non-blocking",         // No! God has nothing to do with programming!! 
	//									"Language inclusion" }; // Programming is fate-driven!


	static class AlgorithmSelector
		extends JComboBox
	{
		//final static int MONOLITHIC = 0;
		//final static int MODULAR = 1;
		//final static int IDD = 2;
		//final static String[] algorithmData = { "Monolithic",  "Modular"};

		public AlgorithmSelector()
		{
			super(VerificationAlgorithm.toArray());
		}
		// Martin: Can you fix this? /Knut
		public void forceMonolithic()
		{
			//removeItemAt(MODULAR);
		}
		public void allowAll()
		{
			//addItem(algorithmData[MODULAR]);
		}
	}

	public VerificationDialogStandardPanel()
	{
		verificationTypeBox = new JComboBox(VerificationType.toArray());
		verificationTypeBox.addActionListener(this);

		algorithmSelector = new AlgorithmSelector();

		nbNote = new JTextArea("Note:\n" +
								"Currently, modular non-blocking\n" +
								"verification is not supported");
		//nbNote.setBackground(new Color(0,0,0,0)); // transparent
		nbNote.setBackground(this.getBackground());

		Box standardBox = Box.createVerticalBox();
		standardBox.add(verificationTypeBox);
		standardBox.add(algorithmSelector);

		// NEW TRY
		this.setLayout(new GridLayout(2,1));
		JPanel choicePanel = new JPanel();
		choicePanel.setLayout(new FlowLayout());
		choicePanel.add(standardBox);
		this.add(choicePanel);
		JPanel notePanel = new JPanel();
		notePanel.setLayout(new FlowLayout());
		notePanel.add(nbNote);
		this.add(notePanel);

		// OLD TRIES
		//this.setLayout(new FlowLayout());// this.setLayout(new BorderLayout());
		//this.add(standardBox);//, BorderLayout.CENTER);
		//this.add(nbNote);//, BorderLayout.SOUTH);
	}

	public void update(VerificationOptions verificationOptions)
	{
		verificationTypeBox.setSelectedItem(verificationOptions.getVerificationType());
		algorithmSelector.setSelectedItem(verificationOptions.getAlgorithmType());
	}

	public void regain(VerificationOptions verificationOptions)
	{
		verificationOptions.setVerificationType((VerificationType)verificationTypeBox.getSelectedItem());
		verificationOptions.setAlgorithmType((VerificationAlgorithm)algorithmSelector.getSelectedItem());
	}

	public void actionPerformed(ActionEvent e)
	{
		if((verificationTypeBox.getSelectedItem()) == VerificationType.Nonblocking) // non-blocking
		{
			// force the monolithic algorithm
			algorithmSelector.forceMonolithic();
			nbNote.setVisible(true);
		}
		else // either controllability or language inclusion selected
		{
			algorithmSelector.allowAll();
			nbNote.setVisible(false);
		}
	}

}

class VerificationDialogAdvancedPanel
	extends JPanel
	implements VerificationPanel
{
	private JTextField exclusionStateLimit;
	private JTextField reachabilityStateLimit;
	private JCheckBox oneEventAtATimeBox;
	private JCheckBox skipUncontrollabilityBox;

	public VerificationDialogAdvancedPanel()
	{
		Box advancedBox = Box.createVerticalBox();
		JLabel exclusionStateLimitText = new JLabel("Initial state limit for state exclusion");

		exclusionStateLimit = new JTextField();

		JLabel reachabilityStateLimitText = new JLabel("Initial state limit for reachability verification");

		reachabilityStateLimit = new JTextField();
		oneEventAtATimeBox = new JCheckBox("Verify one uncontrollable event at a time");
		skipUncontrollabilityBox = new JCheckBox("Skip uncontrollability check");

		advancedBox.add(exclusionStateLimitText);
		advancedBox.add(exclusionStateLimit);
		advancedBox.add(reachabilityStateLimitText);
		advancedBox.add(reachabilityStateLimit);
		advancedBox.add(oneEventAtATimeBox);
		advancedBox.add(skipUncontrollabilityBox);
		this.add(advancedBox, BorderLayout.CENTER);
	}

	public void update(VerificationOptions verificationOptions)
	{
		exclusionStateLimit.setText(Integer.toString(verificationOptions.getExclusionStateLimit()));
		reachabilityStateLimit.setText(Integer.toString(verificationOptions.getReachabilityStateLimit()));
		oneEventAtATimeBox.setSelected(verificationOptions.getOneEventAtATime());
		skipUncontrollabilityBox.setSelected(verificationOptions.getSkipUncontrollabilityCheck());
	}

	public void regain(VerificationOptions verificationOptions)
	{
		verificationOptions.setExclusionStateLimit(PreferencesDialog.getInt("State limit", exclusionStateLimit.getText(), 10));
		verificationOptions.setReachabilityStateLimit(PreferencesDialog.getInt("State limit", reachabilityStateLimit.getText(), 10));
		verificationOptions.setOneEventAtATime(oneEventAtATimeBox.isSelected());
		verificationOptions.setSkipUncontrollabilityCheck(skipUncontrollabilityBox.isSelected());
	}
}

public class VerificationDialog
	implements ActionListener
{
	private JButton okButton;
	private JButton cancelButton;
	private VerificationOptions verificationOptions;
	private VerificationDialogStandardPanel standardPanel;
	private VerificationDialogAdvancedPanel advancedPanel;
	private JDialog dialog;

	/**
	 * Creates modal dialog box for input of synthesizer options.
	 */
	public VerificationDialog(JFrame parentFrame, VerificationOptions verificationOptions)
	{
		dialog = new JDialog(parentFrame, true);    // modal
		this.verificationOptions = verificationOptions;

		dialog.setTitle("Verification options");
		dialog.setSize(new Dimension(400, 300));

		// dialog.setResizable(false);
		Container contentPane = dialog.getContentPane();

		standardPanel = new VerificationDialogStandardPanel();
		advancedPanel = new VerificationDialogAdvancedPanel();

		JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");
		tabbedPane.addTab("Advanced options", null, advancedPanel, "Advanced options");

		// buttonPanel;
		JPanel buttonPanel = new JPanel();

		okButton = addButton(buttonPanel, "OK");
		cancelButton = addButton(buttonPanel, "Cancel");

		contentPane.add("Center", tabbedPane);
		contentPane.add("South", buttonPanel);

		Utility.setDefaultButton(dialog, okButton);

		// ** MF ** Fix to get the frigging thing centered
		Dimension dim = dialog.getMinimumSize();

		dialog.setLocation(Utility.getPosForCenter(dim));
		dialog.setResizable(false);
		update();
	}

	/**
	 * Updates the information in the dialog from what is recorded in VerificationOptions.
	 * @see VerificationOptions
	 */
	public void update()
	{
		standardPanel.update(verificationOptions);
		advancedPanel.update(verificationOptions);
	}

	private JButton addButton(Container container, String name)
	{
		JButton button = new JButton(name);

		button.addActionListener(this);
		container.add(button);

		return button;
	}

	public void show()
	{
		dialog.show();
	}

	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();

		if (source == okButton)
		{                                              // Remember the selections
			verificationOptions.setDialogOK(true);
			standardPanel.regain(verificationOptions);
			advancedPanel.regain(verificationOptions);
			dialog.setVisible(false);
			dialog.dispose();
		}
		else if (source == cancelButton)
		{
			verificationOptions.setDialogOK(false);    // Already done...
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
}
