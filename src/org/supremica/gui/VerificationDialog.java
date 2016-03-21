//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import org.supremica.properties.Config;
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
    private final JButton okButton;
    private final JButton cancelButton;
    private final VerificationOptions verificationOptions;
    private final VerificationDialogStandardPanel standardPanel;
    private final VerificationDialogAdvancedPanelControllability advancedPanelControllability;
    private final VerificationDialogAdvancedPanelModularNonblocking advancedPanelNonblocking;
    private final MinimizationOptions minimizationOptions;
    private final JDialog dialog;

    private final JTabbedPane tabbedPane;

    /**
     * Creates modal dialog box for input of options for verification.
     */
    public VerificationDialog(final Frame parentFrame, final VerificationOptions verificationOptions,
        final MinimizationOptions minimizationOptions)
    {
        dialog = new JDialog(parentFrame, true);    // modal
        this.verificationOptions = verificationOptions;
        this.minimizationOptions = minimizationOptions;

        dialog.setTitle("Verification options");
        dialog.setSize(new Dimension(400, 300));

        // dialog.setResizable(false);
        final Container contentPane = dialog.getContentPane();

        standardPanel = new VerificationDialogStandardPanel();
        advancedPanelControllability = new VerificationDialogAdvancedPanelControllability();
        advancedPanelNonblocking = new VerificationDialogAdvancedPanelModularNonblocking();

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");
        tabbedPane.addTab("Advanced options", null, advancedPanelControllability, "Advanced options");

        // buttonPanel;
        final JPanel buttonPanel = new JPanel();

        okButton = addButton(buttonPanel, "OK");
        cancelButton = addButton(buttonPanel, "Cancel");

        contentPane.add("Center", tabbedPane);
        contentPane.add("South", buttonPanel);
        Utility.setDefaultButton(dialog, okButton);

        // ** MF ** Fix to get the frigging thing centered
        final Dimension dim = dialog.getMinimumSize();

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

    private JButton addButton(final Container container, final String name)
    {
        final JButton button = new JButton(name);

        button.addActionListener(this);
        container.add(button);

        return button;
    }

    public void show()
    {
        dialog.setVisible(true);
    }

    public void actionPerformed(final ActionEvent event)
    {
        final Object source = event.getSource();

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

        private final JComboBox<VerificationType> verificationTypeBox;
        private final JComboBox<VerificationAlgorithm> algorithmSelector;
        private final JCheckBox showTrace;
        private final JTextArea note;

        //private JTextArea note;// = new JTextArea("Bananas...");
        //final String[] verificationData = { "Controllability",  // keep them in this order, for God's sake!
        //   "nonblocking",         // No! God has nothing to do with programming!!
        //   "Language inclusion"}; // Programming is fate-driven!

        public VerificationDialogStandardPanel()
        {
            verificationTypeBox =
              new JComboBox<VerificationType>(VerificationType.enabledValues());
            verificationTypeBox.addActionListener(this);

            algorithmSelector = new JComboBox<VerificationAlgorithm>();
            algorithmSelector.addActionListener(this);

            showTrace = new JCheckBox("Show trace to bad states");
            showTrace.addActionListener(this);

            note = new JTextArea("Note:\n" + "Currently, modular nonblocking\n" + "verification is not supported.");
            note.setBackground(this.getBackground());

            final Box mainBox = Box.createVerticalBox();

            JPanel panel = new JPanel();
            final Box algoBox = Box.createVerticalBox();
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

        public void update(final VerificationOptions verificationOptions)
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
            final int advancedTabIndex = 1;

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
            final VerificationAlgorithm selected = (VerificationAlgorithm) algorithmSelector.getSelectedItem();
            // Clear, then add the ones that are implemented
            algorithmSelector.removeAllItems();
            // Which type of verification?
            final VerificationType vtype =
              (VerificationType) verificationTypeBox.getSelectedItem();
            switch (vtype) {
            case CONTROLLABILITY:
            case INVERSECONTROLLABILITY:
            case LANGUAGEINCLUSION:
              algorithmSelector.addItem(VerificationAlgorithm.MONOLITHIC);
              algorithmSelector.addItem(VerificationAlgorithm.MODULAR);
              algorithmSelector.addItem(VerificationAlgorithm.COMPOSITIONAL);
              algorithmSelector.addItem(VerificationAlgorithm.COMBINED);
              algorithmSelector.addItem(VerificationAlgorithm.BDD);
              if (Config.INCLUDE_EXPERIMENTAL_ALGORITHMS.isTrue() &&
                  vtype == VerificationType.CONTROLLABILITY) {
                algorithmSelector.addItem(VerificationAlgorithm.SAT);
              }
              break;
            case NONBLOCKING:
              algorithmSelector.addItem(VerificationAlgorithm.MONOLITHIC);
              algorithmSelector.addItem(VerificationAlgorithm.MONOLITHICBDD);
              algorithmSelector.addItem(VerificationAlgorithm.COMPOSITIONAL);
              algorithmSelector.addItem(VerificationAlgorithm.COMBINED);
              algorithmSelector.addItem(VerificationAlgorithm.BDD);
              break;
            case CONTROLLABILITYNONBLOCKING:
              algorithmSelector.addItem(VerificationAlgorithm.COMPOSITIONAL);
              break;
            case OP:
              algorithmSelector.addItem(VerificationAlgorithm.MONOLITHIC);
              break;
            case DIAGNOSABILITY:
                algorithmSelector.addItem(VerificationAlgorithm.BBSD);
                break;
            default:
              throw new IllegalStateException("Unknown verification type: " +
                                              vtype + "!");
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
          final VerificationType vtype =
            (VerificationType) verificationTypeBox.getSelectedItem();
          switch (vtype) {
          case NONBLOCKING:
            if (algorithmSelector.getSelectedItem() ==
                VerificationAlgorithm.MODULAR) {
              note.setText("Note:\n" + "This algorithm uses incremental\n" +
                           "composition and minimization with\n" +
                           "respect to conflict equivalence.");
              note.setVisible(true);
            } else {
              note.setVisible(false);
            }
            break;
          case CONTROLLABILITYNONBLOCKING:
            note.setText("Note:\n" +
                         "Verifies both controllability and nonblocking in\n" +
                         "one run. Currently, Supremica will not distinguish\n" +
                         "controllability problems from blocking problems.");
            note.setVisible(true);
            break;
          case LANGUAGEINCLUSION:
            note.setText("Note:\n" +
                         "This verifies whether the language of the unselected\n" +
                         "automata is included in the inverse projection of\n" +
                         "the language of the selected automata.\n" +
                         "  The alphabet of the unselected automata must\n" +
                         "include the alphabet of the selected automata.");
            note.setVisible(true);
            break;
          case INVERSECONTROLLABILITY:
            note.setText("Note:\n" +
                         "This verifies whether the controllable events in the\n" +
                         "supervisor candidate are always accepted by\n" +
                         "the plant. That is, the supervisor is considered\n" +
                         "to be a controller as in the input/output approach\n" +
                         "to control of DES presented by Balemi.");
            note.setVisible(true);
            break;
          case OP:
            note.setText("Note:\n" +
                         "This verifies whether the natural projection\n" +
                         "hiding all tau events in the model satisfies the\n" +
                         "observer property, using the OP-Verifier algorithm\n" +
                         "by P. Pena et.al. (2009).");
            note.setVisible(true);
            break;
          case DIAGNOSABILITY:
              if (algorithmSelector.getSelectedItem() ==
                      VerificationAlgorithm.BBSD) {
                  note.setText("Note:\n" +
                          "This algorithm uses BBSD to verify\n" +
                          "if selected automata are diagnosable.\n");
                  note.setVisible(true);
              } else {
                  note.setVisible(false);
              }
              break;
          default:
            note.setVisible(false);
            break;
          }
        }

        public void regain(final VerificationOptions verificationOptions)
        {
            verificationOptions.setVerificationType((VerificationType) verificationTypeBox.getSelectedItem());
            verificationOptions.setAlgorithmType((VerificationAlgorithm) algorithmSelector.getSelectedItem());
            verificationOptions.setShowBadTrace(showTrace.isSelected());
        }

        public void actionPerformed(final ActionEvent e)
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

        JComboBox<Object> minimizationStrategy;
        JComboBox<Object> minimizationHeuristic;
        JCheckBox ruleSC;
        JCheckBox ruleOSI;
        JCheckBox ruleAE;
        JCheckBox ruleOSO;

        public VerificationDialogAdvancedPanelModularNonblocking()
        {
            minimizationStrategy = new JComboBox<Object>(MinimizationStrategy.values());
            minimizationHeuristic = new JComboBox<Object>(MinimizationHeuristic.values());
            ruleSC = new JCheckBox("Rule SC");
            ruleOSI = new JCheckBox("Rule OSI");
            ruleAE = new JCheckBox("Rule AE");
            ruleOSO = new JCheckBox("Rule OSO");

            // Create layout!
            final Box mainBox = Box.createVerticalBox();

            JPanel panel = new JPanel();
            final Box strategyBox = Box.createHorizontalBox();
            strategyBox.add(new JLabel("Minimization strategy: "));
            strategyBox.add(minimizationStrategy);
            panel.add(strategyBox);
            mainBox.add(panel);

            panel = new JPanel();
            final Box heuristicBox = Box.createHorizontalBox();
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

        public void update(final MinimizationOptions options)
        {
            minimizationStrategy.setSelectedItem(options.getMinimizationStrategy());
            minimizationHeuristic.setSelectedItem(options.getMinimizationHeuristic());
            ruleSC.setSelected(options.getUseRuleSC());
            ruleOSI.setSelected(options.getUseRuleOSI());
            ruleAE.setSelected(options.getUseRuleAE());
            ruleOSO.setSelected(options.getUseRuleOSO());
        }

        public void regain(final MinimizationOptions options)
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

        private final JTextField exclusionStateLimit;
        private final JTextField reachabilityStateLimit;
        private final JCheckBox oneEventAtATimeBox;
        private final JCheckBox skipUncontrollabilityBox;
        private final JTextField nbrOfAttempts;

        public VerificationDialogAdvancedPanelControllability()
        {
            final Box advancedBox = Box.createVerticalBox();
            final JLabel exclusionStateLimitText = new JLabel("Initial state limit for state exclusion");

            exclusionStateLimit = new JTextField();

            final JLabel reachabilityStateLimitText = new JLabel("Initial state limit for reachability verification");

            reachabilityStateLimit = new JTextField();
            oneEventAtATimeBox = new JCheckBox("Verify one uncontrollable event at a time");
            skipUncontrollabilityBox = new JCheckBox("Skip uncontrollability check");

            final JLabel nbrOfAttemptsText = new JLabel("Number of verification attempts");

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

        public void update(final VerificationOptions verificationOptions)
        {
            exclusionStateLimit.setText(Integer.toString(verificationOptions.getExclusionStateLimit()));
            reachabilityStateLimit.setText(Integer.toString(verificationOptions.getReachabilityStateLimit()));
            oneEventAtATimeBox.setSelected(verificationOptions.getOneEventAtATime());
            skipUncontrollabilityBox.setSelected(verificationOptions.getSkipUncontrollabilityCheck());
            nbrOfAttempts.setText(Integer.toString(verificationOptions.getNbrOfAttempts()));
        }

        public void regain(final VerificationOptions verificationOptions)
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
