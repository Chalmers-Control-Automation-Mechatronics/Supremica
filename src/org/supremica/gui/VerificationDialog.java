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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
// import javax.swing.event.*;
// import javax.swing.table.*;

public class VerificationDialog 
	implements ActionListener
{
	private JButton okButton;
	private JButton cancelButton;
	private VerificationOptions verificationOptions;
	private JComboBox verificationTypeBox;
	private JComboBox algorithmTypeBox;
	private JTextField exclusionStateLimit;
	private JTextField reachabilityStateLimit;
	private JCheckBox oneEventAtATimeBox;
	private JCheckBox skipUncontrollabilityBox;
	private JDialog dialog;

	/**
	 * Creates modal dialog box for input of synthesizer options.
	 */
	public VerificationDialog(JFrame parentFrame, VerificationOptions verificationOptions)
	{
		dialog = new JDialog(parentFrame, true); //modal		
		this.verificationOptions = verificationOptions;
		dialog.setTitle("Verification options");
		dialog.setSize(new Dimension(400, 300));
		// dialog.setResizable(false);
		Container contentPane = dialog.getContentPane();

		JPanel standardPanel = new JPanel();
		JPanel advancedPanel = new JPanel();
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Standard options", null, 
                          standardPanel,
                          "Standard options");
        tabbedPane.addTab("Advanced options", null, 
                          advancedPanel,
                          "Advanced options");
		
		// standardPanel
		Box standardBox = Box.createVerticalBox();
		// JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		// JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String[] verificationData = {"controllability", "non-blocking", "language inclusion"};
		verificationTypeBox = new JComboBox(verificationData);
		String[] algorithmData = {"modular", "monolithic", "IDD"};
		algorithmTypeBox = new JComboBox(algorithmData);
		// leftPanel.add(verificationTypeBox);
		// leftPanel.add(algorithmTypeBox);
		// standardPanel.setLayout(new GridLayout(1,2));
		// standardPanel.add(leftPanel);
		// standardPanel.add(rightPanel);
		
		standardBox.add(verificationTypeBox);
		standardBox.add(algorithmTypeBox);

		standardPanel.add(standardBox);
		
		// advancedPanel
		Box advancedBox = Box.createVerticalBox();
		JLabel exclusionStateLimitText =
			new JLabel("Initial state limit for state exclusion");
		exclusionStateLimit = new JTextField();
		JLabel reachabilityStateLimitText =
			new JLabel("Initial state limit for reachability verification");
		reachabilityStateLimit = new JTextField();
		oneEventAtATimeBox = new JCheckBox("Verify one uncontrollable event at a time");
		skipUncontrollabilityBox = new JCheckBox("Skip uncontrollability check");
		
		advancedBox.add(exclusionStateLimitText);
		advancedBox.add(exclusionStateLimit);
		advancedBox.add(reachabilityStateLimitText);
		advancedBox.add(reachabilityStateLimit);
		advancedBox.add(oneEventAtATimeBox);
		advancedBox.add(skipUncontrollabilityBox);

		advancedPanel.add(advancedBox, BorderLayout.CENTER);

		// buttonPanel;
		JPanel buttonPanel = new JPanel();
		okButton = addButton(buttonPanel, "OK");
		cancelButton = addButton(buttonPanel, "Cancel");

		contentPane.add("Center", tabbedPane);
		contentPane.add("South", buttonPanel);

		//** MF ** Fix to get the frigging thing centered
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
		verificationTypeBox.setSelectedIndex(verificationOptions.getVerificationType());
		algorithmTypeBox.setSelectedIndex(verificationOptions.getAlgorithmType());
		exclusionStateLimit.setText(Integer.toString(verificationOptions.getExclusionStateLimit()));
		reachabilityStateLimit.setText(Integer.toString(verificationOptions.getReachabilityStateLimit()));
		oneEventAtATimeBox.setSelected(verificationOptions.getOneEventAtATime());
		skipUncontrollabilityBox.setSelected(verificationOptions.getSkipUncontrollabilityCheck());
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
		{   // Remember the selections
			verificationOptions.setDialogOK(true);
			verificationOptions.setVerificationType(verificationTypeBox.getSelectedIndex());
			verificationOptions.setAlgorithmType(algorithmTypeBox.getSelectedIndex());
			verificationOptions.setExclusionStateLimit(PreferencesDialog.getInt("State limit", exclusionStateLimit.getText(), 10));
			verificationOptions.setReachabilityStateLimit(PreferencesDialog.getInt("State limit", reachabilityStateLimit.getText(), 10));
			verificationOptions.setOneEventAtATime(oneEventAtATimeBox.isSelected());
			verificationOptions.setSkipUncontrollabilityCheck(skipUncontrollabilityBox.isSelected());
			dialog.setVisible(false);
			dialog.dispose();
		}
		else if (source == cancelButton)
		{
			verificationOptions.setDialogOK(false); // Already done...
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
}
