
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
package org.supremica.gui;

import org.supremica.automata.algorithms.VerificationOptions;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.VerificationAlgorithm;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.automata.algorithms.minimization.MinimizationStrategy;
import org.supremica.automata.algorithms.minimization.MinimizationHeuristic;
import org.supremica.automata.algorithms.EquivalenceRelation;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.Integer;

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
    public VerificationDialog(Frame parentFrame, VerificationOptions verificationOptions,
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
    
    private class VerificationDialogStandardPanel
        extends JPanel
        implements VerificationPanel, ActionListener
    {
        private static final long serialVersionUID = 1L;
        
        private JComboBox verificationTypeBox;
        private JComboBox algorithmSelector;
        private JCheckBox showTrace;
        private JTextArea note;
        
        //private JTextArea note;// = new JTextArea("Bananas...");
        //final String[] verificationData = { "Controllability",  // keep them in this order, for God's sake!
        //   "nonblocking",         // No! God has nothing to do with programming!!
        //   "Language inclusion"}; // Programming is fate-driven!
        
        public VerificationDialogStandardPanel()
        {
            verificationTypeBox = new JComboBox(VerificationType.values());
            verificationTypeBox.addActionListener(this);
            
            algorithmSelector = new JComboBox();
            algorithmSelector.addActionListener(this);
            
            showTrace = new JCheckBox("Show trace to bad states");
            showTrace.addActionListener(this);
            
            note = new JTextArea("Note:\n" + "Currently, modular nonblocking\n" + "verification is not supported.");
            note.setBackground(this.getBackground());
            
            Box mainBox = Box.createVerticalBox();
            
            JPanel panel = new JPanel();
            Box algoBox = Box.createVerticalBox();
            algoBox.add(verificationTypeBox);
            algoBox.add(algorithmSelector);
            panel.add(algoBox);
            mainBox.add(panel);
            
            panel = new JPanel();
            panel.add(showTrace);
            mainBox.add(panel);
            
            panel = new JPanel();
            panel.add(note);
            note.setVisible(false);
            mainBox.add(panel);
            
            this.add(mainBox);
        }
        
        public void update(VerificationOptions verificationOptions)
        {
            verificationTypeBox.setSelectedItem(verificationOptions.getVerificationType());
            algorithmSelector.setSelectedItem(verificationOptions.getAlgorithmType());
            showTrace.setSelected(verificationOptions.showBadTrace());
            updatePanel();
            updateNote();
        }
        
        /**
         * Changes the available options on the panel based on the current choice.
         */
        private void updatePanel()
        {
            // Some ugly stuff goes on here, the "advanced panel" is supposed to be the one
            // with index 1.
            int advancedTabIndex = 1;
            
            // Change the advanced panel
            if ((verificationTypeBox.getSelectedItem() == VerificationType.CONTROLLABILITY ||
                verificationTypeBox.getSelectedItem() == VerificationType.INVERSECONTROLLABILITY) &&
                (algorithmSelector.getSelectedItem() == VerificationAlgorithm.MODULAR))
            {
                // Show advanced controllability options!
                //tabbedPane.add("Advanced options", null, advancedPanelControllability, "Advanced options");
                //int index = tabbedPane.indexOfComponent(advancedPanelControllability);
                tabbedPane.setComponentAt(advancedTabIndex, advancedPanelControllability);
                tabbedPane.setEnabledAt(advancedTabIndex, true);
            }
            else if (algorithmSelector.getSelectedItem() == VerificationAlgorithm.COMPOSITIONAL)
            {
                // Show advanced nonblocking options!
                //tabbedPane.remove(advancedPanelControllability);
                //int index = tabbedPane.indexOfComponent(advancedPanelControllability);
                tabbedPane.setComponentAt(advancedTabIndex, advancedPanelNonblocking);
                tabbedPane.setEnabledAt(advancedTabIndex, true);
            }
            else
            {
                // Hide advanced panel
                tabbedPane.setEnabledAt(advancedTabIndex, false);
            }
            
            // Which algorithms should be enabled?
            // Remember current selection
            VerificationAlgorithm selected = (VerificationAlgorithm) algorithmSelector.getSelectedItem();
            // Clear, then add the ones that are implemented
            algorithmSelector.removeAllItems();
            // Which type of verification?
            if (verificationTypeBox.getSelectedItem() == VerificationType.CONTROLLABILITY ||
                verificationTypeBox.getSelectedItem() == VerificationType.INVERSECONTROLLABILITY ||
                verificationTypeBox.getSelectedItem() == VerificationType.LANGUAGEINCLUSION)
            {
                algorithmSelector.addItem(VerificationAlgorithm.MONOLITHIC);
                algorithmSelector.addItem(VerificationAlgorithm.MODULAR);
                algorithmSelector.addItem(VerificationAlgorithm.COMPOSITIONAL);
                algorithmSelector.addItem(VerificationAlgorithm.BDD);
            }
            /*
            else if (verificationTypeBox.getSelectedItem() == VerificationType.MUTUALLYNONBLOCKING)
            {
                // Only modular algo implemented
                algorithmSelector.addItem(VerificationAlgorithm.MODULAR);
            }
             */
            else if (verificationTypeBox.getSelectedItem() == VerificationType.NONBLOCKING)
            {
                algorithmSelector.addItem(VerificationAlgorithm.MONOLITHIC);
                //algorithmSelector.addItem(VerificationAlgorithm.MODULAR);
                algorithmSelector.addItem(VerificationAlgorithm.COMPOSITIONAL);
                algorithmSelector.addItem(VerificationAlgorithm.BDD);
            }
            else if (verificationTypeBox.getSelectedItem() == VerificationType.CONTROLLABILITYNONBLOCKING)
            {
                algorithmSelector.addItem(VerificationAlgorithm.COMPOSITIONAL);
            }
            // Default selection
            algorithmSelector.setSelectedIndex(0);
            // Reselect previously selected item if possible
            algorithmSelector.setSelectedItem(selected);
            
            // Show trace?
            if (verificationTypeBox.getSelectedItem() == VerificationType.NONBLOCKING &&
                algorithmSelector.getSelectedItem() == VerificationAlgorithm.MONOLITHIC ||
                (verificationTypeBox.getSelectedItem() == VerificationType.CONTROLLABILITY ||
                verificationTypeBox.getSelectedItem() == VerificationType.INVERSECONTROLLABILITY ||
                verificationTypeBox.getSelectedItem() == VerificationType.LANGUAGEINCLUSION) &&
                algorithmSelector.getSelectedItem() == VerificationAlgorithm.MODULAR)
            {
                showTrace.setEnabled(true);
            }
            else
            {
                //showTrace.setSelected(false);
                showTrace.setEnabled(false);
            }
        }
        
        /**
         * Changes the displayed note depending on the current choice.
         */
        private void updateNote()
        {
            // Change the note
            if (verificationTypeBox.getSelectedItem() == VerificationType.NONBLOCKING &&
                algorithmSelector.getSelectedItem() == VerificationAlgorithm.MODULAR)
            {
                note.setText("Note:\n" + "This algorithm uses incremental\n" +
                    "composition and minimization with\n" +
                    "respect to conflict equivalence.");
                note.setVisible(true);
            }
            /*
            else if (verificationTypeBox.getSelectedItem() == VerificationType.MUTUALLYNONBLOCKING)
            {
                note.setText("Note:\n" + "Mutual nonblocking is inherently modular\n" +
                    "and hence there is no monolithic algoritm.");
                note.setVisible(true);
            }
             */
            else if (verificationTypeBox.getSelectedItem() == VerificationType.CONTROLLABILITYNONBLOCKING)
            {
                note.setText("Note:\n" + "Verifies both controllability and nonblocking in\n" +
                    "one run. Currently, Supremica will not distinguish\n" +
                    "controllability problems from blocking problems");
                note.setVisible(true);
            }
            else if (verificationTypeBox.getSelectedItem() == VerificationType.LANGUAGEINCLUSION)
            {
                note.setText("Note:\n" + "This verifies whether the language of the unselected\n" +
                    "automata is included in the inverse projection of\n" +
                    "the language of the selected automata.\n" +
                    "  The alphabet of the unselected automata must\n" +
                    "include the alphabet of the selected automata.");
                note.setVisible(true);
            }
            else if (verificationTypeBox.getSelectedItem() == VerificationType.INVERSECONTROLLABILITY)
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
            verificationOptions.setShowBadTrace(showTrace.isSelected());
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
        JComboBox minimizationHeuristic;
        JCheckBox ruleSC;
        JCheckBox ruleOSI;
        JCheckBox ruleAE;
        JCheckBox ruleOSO;
        
        public VerificationDialogAdvancedPanelModularNonblocking()
        {
            minimizationStrategy = new JComboBox(MinimizationStrategy.values());
            minimizationHeuristic = new JComboBox(MinimizationHeuristic.values());
            ruleSC = new JCheckBox("Rule SC");
            ruleOSI = new JCheckBox("Rule OSI");
            ruleAE = new JCheckBox("Rule AE");
            ruleOSO = new JCheckBox("Rule OSO");
            
            // Create layout!
            Box mainBox = Box.createVerticalBox();
            
            JPanel panel = new JPanel();
            Box strategyBox = Box.createHorizontalBox();
            strategyBox.add(new JLabel("Minimization strategy: "));
            strategyBox.add(minimizationStrategy);
            panel.add(strategyBox);
            mainBox.add(panel);
            
            panel = new JPanel();
            Box heuristicBox = Box.createHorizontalBox();
            heuristicBox.add(new JLabel("Minimization heuristic: "));
            heuristicBox.add(minimizationHeuristic);
            panel.add(heuristicBox);
            mainBox.add(panel);
            
            panel = new JPanel();
            panel.add(new JLabel("Rules: "));
            panel.add(ruleSC);
            panel.add(ruleOSI);
            panel.add(ruleAE);
            panel.add(ruleOSO);
            mainBox.add(panel);
            
            // Add components
            this.add(mainBox);
        }
        
        public void update(MinimizationOptions options)
        {
            minimizationStrategy.setSelectedItem(options.getMinimizationStrategy());
            minimizationHeuristic.setSelectedItem(options.getMinimizationHeuristic());
            ruleSC.setSelected(options.getUseRuleSC());
            ruleOSI.setSelected(options.getUseRuleOSI());
            ruleAE.setSelected(options.getUseRuleAE());
            ruleOSO.setSelected(options.getUseRuleOSO());
        }
        
        public void regain(MinimizationOptions options)
        {
            options.setMinimizationType(EquivalenceRelation.CONFLICTEQUIVALENCE);
            
            options.setMinimizationStrategy((MinimizationStrategy) minimizationStrategy.getSelectedItem());
            options.setMinimizationHeuristic((MinimizationHeuristic) minimizationHeuristic.getSelectedItem());
            options.setUseRuleSC(ruleSC.isSelected());
            options.setUseRuleOSI(ruleOSI.isSelected());
            options.setUseRuleAE(ruleAE.isSelected());
            options.setUseRuleOSO(ruleOSO.isSelected());
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
            //verificationOptions.setExclusionStateLimit(PreferencesDialog.getInt("Exclusion state limit", exclusionStateLimit.getText(), 10));
            verificationOptions.setExclusionStateLimit(Integer.parseInt(exclusionStateLimit.getText())); // Should have min and max values?
            //verificationOptions.setReachabilityStateLimit(PreferencesDialog.getInt("Reachability state limit", reachabilityStateLimit.getText(), 10));
            verificationOptions.setReachabilityStateLimit(Integer.parseInt(reachabilityStateLimit.getText())); // Should have min and max values?
            verificationOptions.setOneEventAtATime(oneEventAtATimeBox.isSelected());
            verificationOptions.setSkipUncontrollabilityCheck(skipUncontrollabilityBox.isSelected());
            //verificationOptions.setNbrOfAttempts(PreferencesDialog.getInt("Nbr of attempts limit", nbrOfAttempts.getText(), 1));
            verificationOptions.setNbrOfAttempts(Integer.parseInt(nbrOfAttempts.getText()));
        }
    }
}
