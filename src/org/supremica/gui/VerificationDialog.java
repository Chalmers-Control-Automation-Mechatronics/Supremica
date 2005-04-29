
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
import org.supremica.automata.algorithms.MinimizationOptions;
import org.supremica.automata.algorithms.MinimizationStrategy;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

interface VerificationPanel
{
	void update(VerificationOptions v);

	void regain(VerificationOptions v);
}

public class VerificationDialog
	implements ActionListener
{
	private JButton okButton;
	private JButton cancelButton;
	private VerificationOptions verificationOptions;
	private VerificationDialogStandardPanel standardPanel;
	private VerificationDialogAdvancedPanelControllability advancedPanelControllability;
	private VerificationDialogAdvancedPanelModularNonblocking advancedPanelNonblocking;
	private MinimizationOptions minimizationOptions;
	private JDialog dialog;

	private JTabbedPane tabbedPane;

	/**
	 * Creates modal dialog box for input of options for verification.
	 */
	public VerificationDialog(JFrame parentFrame, VerificationOptions verificationOptions, 
							  MinimizationOptions minimizationOptions)
	{
		dialog = new JDialog(parentFrame, true);    // modal
		this.verificationOptions = verificationOptions;
		this.minimizationOptions = minimizationOptions;

		dialog.setTitle("Verification options");
		dialog.setSize(new Dimension(400, 300));

		// dialog.setResizable(false);
		Container contentPane = dialog.getContentPane();

		standardPanel = new VerificationDialogStandardPanel();
		advancedPanelControllability = new VerificationDialogAdvancedPanelControllability();
		advancedPanelNonblocking = new VerificationDialogAdvancedPanelModularNonblocking();

		tabbedPane = new JTabbedPane();

		tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");
		tabbedPane.addTab("Advanced options", null, advancedPanelControllability, "Advanced options");

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
		advancedPanelControllability.update(verificationOptions);
		advancedPanelNonblocking.update(minimizationOptions);
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
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();

		if (source == okButton)
		{
			// Remember the selections
			standardPanel.regain(verificationOptions);
			advancedPanelControllability.regain(verificationOptions);
			advancedPanelNonblocking.regain(minimizationOptions);
			verificationOptions.saveOptions();
			verificationOptions.setDialogOK(true);
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

	private static class AlgorithmSelector
		extends JComboBox
	{
		private static final long serialVersionUID = 1L;

		public AlgorithmSelector()
		{
			super(VerificationAlgorithm.toArray());
		}

		public void forceMonolithic()
		{
			allowAll();
			removeItem(VerificationAlgorithm.Modular);
		}

		public void forceModular()
		{
			allowAll();
			removeItem(VerificationAlgorithm.Monolithic);
		}

		public void allowAll()
		{
			VerificationAlgorithm selected = (VerificationAlgorithm) getSelectedItem();

			removeAllItems();

			Object[] alternatives = VerificationAlgorithm.toArray();

			for (int i = 0; i < alternatives.length; i++)
			{
				addItem(alternatives[i]);
			}

			setSelectedItem(selected);
		}
	}
	
	private class VerificationDialogStandardPanel
		extends JPanel
		implements VerificationPanel, ActionListener
	{
		private static final long serialVersionUID = 1L;

		private JComboBox verificationTypeBox;
		private AlgorithmSelector algorithmSelector;
		private JTextArea note;

		//private JTextArea note;// = new JTextArea("Bananas...");
		//final String[] verificationData = { "Controllability",  // keep them in this order, for God's sake!
		//   "nonblocking",        // No! God has nothing to do with programming!!
		//   "Language inclusion"};// Programming is fate-driven!

		public VerificationDialogStandardPanel()
		{
			verificationTypeBox = new JComboBox(VerificationType.toArray());
			verificationTypeBox.addActionListener(this);

			algorithmSelector = new AlgorithmSelector();
			algorithmSelector.addActionListener(this);

			note = new JTextArea("Note:\n" + "Currently, modular nonblocking\n" + "verification is not supported.");
			note.setBackground(this.getBackground());

			Box standardBox = Box.createVerticalBox();

			standardBox.add(verificationTypeBox);
			standardBox.add(algorithmSelector);

			// NEW TRY
			this.setLayout(new GridLayout(2, 1));

			JPanel choicePanel = new JPanel();

			choicePanel.setLayout(new FlowLayout());
			choicePanel.add(standardBox);
			this.add(choicePanel);

			JPanel notePanel = new JPanel();

			notePanel.setLayout(new FlowLayout());
			notePanel.add(note);
			note.setVisible(false);
			this.add(notePanel);
		}

		public void update(VerificationOptions verificationOptions)
		{
			verificationTypeBox.setSelectedItem(verificationOptions.getVerificationType());
			algorithmSelector.setSelectedItem(verificationOptions.getAlgorithmType());
			updatePanel();
			updateNote();
		}

		/**
		 * Changes the available options on the panel based on the current choice.
		 */
		private void updatePanel()
		{
			// Some kind of ugly stuff goes on here, the "advanced panel" is supposed to be the one
			// with index 1.
			int advancedTabIndex = 1;

			// Change the advanced panel
			if ((verificationTypeBox.getSelectedItem() == VerificationType.Controllability ||
				 verificationTypeBox.getSelectedItem() == VerificationType.InverseControllability) && 
				(algorithmSelector.getSelectedItem() == VerificationAlgorithm.Modular))
			{
				// Show advanced controllability options!
				//tabbedPane.add("Advanced options", null, advancedPanelControllability, "Advanced options");
				//int index = tabbedPane.indexOfComponent(advancedPanelControllability);
				tabbedPane.setComponentAt(advancedTabIndex, advancedPanelControllability);
				tabbedPane.setEnabledAt(advancedTabIndex, true);
			}
			else if ((verificationTypeBox.getSelectedItem() == VerificationType.Nonblocking) &&
				  (algorithmSelector.getSelectedItem() == VerificationAlgorithm.Modular))
			{
				// Show advanced nonblocking options!
				//tabbedPane.remove(advancedPanelControllability);
				//int index = tabbedPane.indexOfComponent(advancedPanelControllability);
				tabbedPane.setComponentAt(advancedTabIndex, advancedPanelNonblocking);
				tabbedPane.setEnabledAt(advancedTabIndex, true);
			}
			else
			{
				tabbedPane.setEnabledAt(advancedTabIndex, false);
			}

			// Force things depending on earlier choice
			if (verificationTypeBox.getSelectedItem() == VerificationType.MutuallyNonblocking)
			{
				// Force the modular algorithm
				algorithmSelector.forceModular();
			}
			else
			{
				// Allow all
				algorithmSelector.allowAll();
			}
		}

		/**
		 * Changes the displayed note depending on the current choice.
		 */
		private void updateNote()
		{
			// Change the note
			if (verificationTypeBox.getSelectedItem() == VerificationType.Nonblocking &&
				algorithmSelector.getSelectedItem() == VerificationAlgorithm.Modular)
			{
				note.setText("Note:\n" + "This algorithm uses incremental\n" +
							 "composition and minimization with\n" +
							 "respect to conflict equivalence.");
				note.setVisible(true);
			}
			else if (verificationTypeBox.getSelectedItem() == VerificationType.MutuallyNonblocking)
			{
				note.setText("Note:\n" + "Mutual nonblocking is inherently modular\n" + 
							 "and hence there is no monolithic algoritm.");
				note.setVisible(true);
			}
			else if (verificationTypeBox.getSelectedItem() == VerificationType.LanguageInclusion)
			{
				note.setText("Note:\n" + "This verifies whether the language of the unselected\n" + 
							 "automata is included in the inverse projection of\n" + 
							 "the language of the selected automata.\n" + 
							 "  The alphabet of the unselected automata must\n" + 
							 "include the alphabet of the selected automata.");
				note.setVisible(true);
			}
			else if (verificationTypeBox.getSelectedItem() == VerificationType.InverseControllability)
			{
				note.setText("Note:\n" + "This verifies whether the controllable events in the\n" +
							 "supervisor candidate are always accepted by\n" + 
							 "the plant. That is, the supervisor is considered\n" + 
							 "to be a controller as in the input/output approach\n" + 
							 "to control of DES presented by Balemi.");
				note.setVisible(true);
			}
			else // Something else is selected
			{
				note.setVisible(false);
			}
		}

		public void regain(VerificationOptions verificationOptions)
		{
			verificationOptions.setVerificationType((VerificationType) verificationTypeBox.getSelectedItem());
			verificationOptions.setAlgorithmType((VerificationAlgorithm) algorithmSelector.getSelectedItem());
		}

		public void actionPerformed(ActionEvent e)
		{
			updatePanel();
			updateNote();
		}
	}

	class VerificationDialogAdvancedPanelModularNonblocking
		extends JPanel
		// implements MinimizationDialog.MinimizationPanel
	{
		private static final long serialVersionUID = 1L;

		JComboBox minimizationStrategy;
		JCheckBox ruleA;
		JCheckBox ruleAA;
		JCheckBox ruleB;
		JCheckBox ruleF;

		public VerificationDialogAdvancedPanelModularNonblocking()
		{
			minimizationStrategy = new JComboBox(MinimizationStrategy.toArray());
			Box strategyBox = Box.createHorizontalBox();
			strategyBox.add(new JLabel("      ")); // Ugly fix to get stuff centered
			strategyBox.add(new JLabel("Minimization strategy: "));
			strategyBox.add(minimizationStrategy);
			strategyBox.add(new JLabel("      ")); // Ugly fix to get stuff centered
			this.add(strategyBox);

			ruleA = new JCheckBox("Rule A");
			ruleAA = new JCheckBox("Rule AA");
			ruleB = new JCheckBox("Rule B");
			ruleF = new JCheckBox("Rule F");
			this.add(ruleA);
			this.add(ruleAA);
			this.add(ruleB);
			this.add(ruleF);
		}

		public void update(MinimizationOptions options)
		{
			minimizationStrategy.setSelectedItem(options.getMinimizationStrategy());
			ruleA.setSelected(options.getUseRuleA());
			ruleAA.setSelected(options.getUseRuleAA());
			ruleB.setSelected(options.getUseRuleB());
			ruleF.setSelected(options.getUseRuleF());
		}
	
		public void regain(MinimizationOptions options)
		{
			options.setMinimizationStrategy((MinimizationStrategy) minimizationStrategy.getSelectedItem());
			options.setUseRuleA(ruleA.isSelected());
			options.setUseRuleA(ruleAA.isSelected());
			options.setUseRuleB(ruleB.isSelected());
			options.setUseRuleF(ruleF.isSelected());
		}
	}

	class VerificationDialogAdvancedPanelControllability
		extends JPanel
		implements VerificationPanel
	{
		private static final long serialVersionUID = 1L;

		private JTextField exclusionStateLimit;
		private JTextField reachabilityStateLimit;
		private JCheckBox oneEventAtATimeBox;
		private JCheckBox skipUncontrollabilityBox;
		private JTextField nbrOfAttempts;

		public VerificationDialogAdvancedPanelControllability()
		{
			Box advancedBox = Box.createVerticalBox();
			JLabel exclusionStateLimitText = new JLabel("Initial state limit for state exclusion");

			exclusionStateLimit = new JTextField();

			JLabel reachabilityStateLimitText = new JLabel("Initial state limit for reachability verification");

			reachabilityStateLimit = new JTextField();
			oneEventAtATimeBox = new JCheckBox("Verify one uncontrollable event at a time");
			skipUncontrollabilityBox = new JCheckBox("Skip uncontrollability check");

			JLabel nbrOfAttemptsText = new JLabel("Number of verification attempts");

			nbrOfAttempts = new JTextField();

			advancedBox.add(exclusionStateLimitText);
			advancedBox.add(exclusionStateLimit);
			advancedBox.add(reachabilityStateLimitText);
			advancedBox.add(reachabilityStateLimit);
			advancedBox.add(oneEventAtATimeBox);
			advancedBox.add(skipUncontrollabilityBox);
			advancedBox.add(nbrOfAttemptsText);
			advancedBox.add(nbrOfAttempts);
			this.add(advancedBox, BorderLayout.CENTER);
		}

		public void update(VerificationOptions verificationOptions)
		{
			exclusionStateLimit.setText(Integer.toString(verificationOptions.getExclusionStateLimit()));
			reachabilityStateLimit.setText(Integer.toString(verificationOptions.getReachabilityStateLimit()));
			oneEventAtATimeBox.setSelected(verificationOptions.getOneEventAtATime());
			skipUncontrollabilityBox.setSelected(verificationOptions.getSkipUncontrollabilityCheck());
			nbrOfAttempts.setText(Integer.toString(verificationOptions.getNbrOfAttempts()));
		}

		public void regain(VerificationOptions verificationOptions)
		{
			verificationOptions.setExclusionStateLimit(PreferencesDialog.getInt("Exclusion state limit", exclusionStateLimit.getText(), 10));
			verificationOptions.setReachabilityStateLimit(PreferencesDialog.getInt("Reachability state limit", reachabilityStateLimit.getText(), 10));
			verificationOptions.setOneEventAtATime(oneEventAtATimeBox.isSelected());
			verificationOptions.setSkipUncontrollabilityCheck(skipUncontrollabilityBox.isSelected());
			verificationOptions.setNbrOfAttempts(PreferencesDialog.getInt("Nbr of attempts limit", nbrOfAttempts.getText(), 1));
		}
	}
}
