//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;

abstract class EditorSynthesizerPanel
    extends JPanel
{
    public abstract void update(EditorSynthesizerOptions s);

    public abstract void regain(EditorSynthesizerOptions s);

    private static final long serialVersionUID = 1L;
}

class EditorSynthesizerDialogStandardPanel
    extends EditorSynthesizerPanel
    implements ActionListener
{
    private static final long serialVersionUID = 1L;
    private final SynthesisSelector typeSelector;
    private final AlgorithmSelector algorithmSelector;
    private final JCheckBox reachableBox;
    private final JCheckBox peakBDDBox;
    private final JPanel guardBox;
    private final Box genGuardComputeSupBox;

    private final JCheckBox printGuardBox;
    private final JCheckBox addGuardsBox;
    private final JCheckBox saveEventGuardInFileBox;
    private final JCheckBox saveIDDInFileBox;

    private final JCheckBox timeOptBox;

    private final JCheckBox complementHeuristicBox;
    private final JCheckBox independentHeuristicBox;

    private final JTextField globalClockDomainField;

    private final JRadioButton fromAllowedStatesButton;
    private final JRadioButton fromForbiddenStatesButton;

    private final JRadioButton minVarButton;
    private final JRadioButton maxVarButton;

    private final JRadioButton optimalButton;
    private final JComboBox<String> eventList;
    private final JComboBox<String> variablesList;


    static class AlgorithmSelector
        extends JComboBox<SynthesisAlgorithm>
    {
        private static final long serialVersionUID = 1L;

        private AlgorithmSelector()
        {
            super();
        }

        private AlgorithmSelector(final SynthesisAlgorithm[] array)
        {
            super(array);
        }

        private AlgorithmSelector(final SynthesisAlgorithm algo)
        {
            addItem(algo);
        }

        public SynthesisAlgorithm getAlgorithm()
        {
            return (SynthesisAlgorithm) getSelectedItem();
        }

        public void setAlgorithm(final SynthesisAlgorithm algo)
        {
            setSelectedItem(algo);
        }

        public static AlgorithmSelector create(final int num)
        {
            if (num == 1)
            {
                final AlgorithmSelector selector = new AlgorithmSelector();
                for (final SynthesisAlgorithm algo: SynthesisAlgorithm.values())
                {
                    if (!algo.prefersModular())
                    {
                        selector.addItem(algo);
                    }
                }
                return selector;
            }
            else
            {
                final AlgorithmSelector selector = new AlgorithmSelector();
                for (final SynthesisAlgorithm algo: SynthesisAlgorithm.values())
                {
                    selector.addItem(algo);
                }
                return selector;
            }
        }
    }

    static class SynthesisSelector
        extends JComboBox<SynthesisType>
    {
        private static final long serialVersionUID = 1L;

        private SynthesisSelector()
        {
            super(SynthesisType.editorValues());
        }

        public SynthesisType getType()
        {
            return (SynthesisType) getSelectedItem();
        }

        public void setType(final SynthesisType type)
        {
            setSelectedItem(type);
        }

        public static SynthesisSelector create()
        {
            return new SynthesisSelector();
        }
    }


    public EditorSynthesizerDialogStandardPanel(final int num, final Vector<String> events, final Vector<String> variables)
    {

        algorithmSelector = AlgorithmSelector.create(num);
        algorithmSelector.addActionListener(this);

        typeSelector = SynthesisSelector.create();
        typeSelector.addActionListener(this);

        // Supervisor options
        final Box mainBox = Box.createVerticalBox();
        final JPanel supervisorPanel = new JPanel();
        supervisorPanel.setBorder(BorderFactory.createTitledBorder("Supervisor"));
        supervisorPanel.setLayout(new GridLayout(4, 1));

        JPanel panel = new JPanel();
        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Property:"));
        box.add(typeSelector);
        panel.add(box);
        supervisorPanel.add(panel);

        panel = new JPanel();
        box = Box.createHorizontalBox();
        box.add(new JLabel("Algorithm: "));
        //Until now it is only possible to perform the synthesis via BDDs
        algorithmSelector.setEnabled(true);
        box.add(algorithmSelector);
        panel.add(box);
        supervisorPanel.add(panel);

        panel = new JPanel();
        box = Box.createHorizontalBox();
        reachableBox = new JCheckBox("Remove the unreachable states");
        reachableBox.setEnabled(true);
        reachableBox.setVisible(true);
        box.add(reachableBox);
        reachableBox.addActionListener(this);
        panel.add(box);
        supervisorPanel.add(panel);

        panel = new JPanel();
        box = Box.createHorizontalBox();
        peakBDDBox = new JCheckBox("Compute peak BDD nodes");
        peakBDDBox.setEnabled(true);
        peakBDDBox.setVisible(true);
        box.add(peakBDDBox);
        peakBDDBox.addActionListener(this);
        panel.add(box);
        supervisorPanel.add(panel);

        mainBox.add(supervisorPanel);

        // Guard computation options
        GridBagLayout layout = new GridBagLayout();
        guardBox = new JPanel(layout);
        guardBox.setBorder(BorderFactory.createTitledBorder("Guard computation"));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        optimalButton = new JRadioButton("Adaptive solution");
        optimalButton.setToolTipText("Generate the guard from the state set that yields the best result");
        optimalButton.setSelected(true);
        fromAllowedStatesButton = new JRadioButton("From allowed states");
        fromAllowedStatesButton.setToolTipText("Generate the guard from the states where the event is allowed to occur");
        fromForbiddenStatesButton = new JRadioButton("From forbidden states");
        fromForbiddenStatesButton.setToolTipText("Generate the guard from the states where the event is forbidden to occur");
        ButtonGroup group = new ButtonGroup();
        group.add(optimalButton);
        group.add(fromAllowedStatesButton);
        group.add(fromForbiddenStatesButton);

        complementHeuristicBox = new JCheckBox("Complement Heuristic");
        complementHeuristicBox.setToolTipText("Apply the 'Complement Heuristic'.");
        complementHeuristicBox.setEnabled(true);
        complementHeuristicBox.setSelected(true);
        independentHeuristicBox = new JCheckBox("Independent Heuristic");
        independentHeuristicBox.setToolTipText("Apply the 'Independent Heuristic'.");
        independentHeuristicBox.setEnabled(true);
        independentHeuristicBox.setSelected(true);

        guardBox.add(optimalButton, constraints);
        guardBox.add(complementHeuristicBox, constraints);
        constraints.gridy++;
        guardBox.add(fromAllowedStatesButton, constraints);
        guardBox.add(independentHeuristicBox, constraints);
        constraints.gridy++;
        guardBox.add(fromForbiddenStatesButton, constraints);
        constraints.gridy++;

        eventList = new JComboBox<String>(events);
        constraints.gridwidth = 2;
        guardBox.add(eventList, constraints);
        constraints.gridwidth = 1;

        // Guard Representation options
        genGuardComputeSupBox = Box.createVerticalBox();

        printGuardBox = new JCheckBox("Print the computed guards");
        printGuardBox.setToolTipText("Compute and print the guards.");

        printGuardBox.addActionListener(this);

        addGuardsBox = new JCheckBox("Add guards to the model");
        addGuardsBox.setToolTipText("Add computed guards to the model; variables of automata are created and updates are added.");

        addGuardsBox.addActionListener(this);

        saveEventGuardInFileBox = new JCheckBox("Save the result in a file");
        saveEventGuardInFileBox.setToolTipText("Compute and write the event-guard pairs in a file.");

        saveEventGuardInFileBox.addActionListener(this);

        saveIDDInFileBox = new JCheckBox("Save the result as an IDD in a file");
        saveIDDInFileBox.setToolTipText("For each event an IDD (expression graph) is generated representing the guard. The file is saved in .ps format.");

        saveIDDInFileBox.addActionListener(this);

        final JPanel representationPanel = new JPanel();
        representationPanel.setBorder(BorderFactory.createTitledBorder("Guard representation"));
        representationPanel.setLayout(new GridLayout(5, 1));
        representationPanel.add(printGuardBox);
        representationPanel.add(addGuardsBox);
        representationPanel.add(saveEventGuardInFileBox);
        representationPanel.add(saveIDDInFileBox);

        genGuardComputeSupBox.add(representationPanel);

        // Optimisation options
        layout = new GridBagLayout();
        final JPanel optimizationPanel = new JPanel(layout);
        constraints.gridy = 0;
        optimizationPanel.setBorder(BorderFactory.createTitledBorder("Optimization"));

        timeOptBox = new JCheckBox("Compute optimal time");
        timeOptBox.setToolTipText("Compute the supervisor that will restrict the plant to reach a marked state with the minimum time.");
        timeOptBox.addActionListener(this);
        constraints.gridwidth = 2;
        optimizationPanel.add(timeOptBox, constraints);

        constraints.gridwidth = 1;
        constraints.gridy++;
        optimizationPanel.add(new JLabel("Global time domain:"), constraints);
        globalClockDomainField = new JTextField("0");
        globalClockDomainField.setToolTipText("Guess a reasonably large amount of time to reach a marked state");
        globalClockDomainField.setColumns(10);
        globalClockDomainField.setEnabled(false);
        optimizationPanel.add(globalClockDomainField, constraints);

        constraints.gridy++;
        final JLabel miniSupLabel = new JLabel("Optimize variable:");
        miniSupLabel.setToolTipText("Compute the supervisor that ensures the variable will have its optimal value among the reachable markes states.");
        optimizationPanel.add(miniSupLabel, constraints);
        variablesList = new JComboBox<String>(variables);
        optimizationPanel.add(variablesList, constraints);

        constraints.gridy++;
        minVarButton = new JRadioButton("Minimize");
        minVarButton.setSelected(true);
        maxVarButton = new JRadioButton("Maximize");
        group = new ButtonGroup();
        group.add(minVarButton);
        group.add(maxVarButton);
        optimizationPanel.add(minVarButton, constraints);
        optimizationPanel.add(maxVarButton, constraints);

        // Create layout!
        layout = new GridBagLayout();
        setLayout(layout);
        constraints.gridy = 0;
        add(mainBox, constraints);
        add(guardBox, constraints);
        constraints.gridy++;
        add(genGuardComputeSupBox, constraints);
        add(optimizationPanel, constraints);

        updatePanel();
    }

    @Override
    public void update(final EditorSynthesizerOptions synthesizerOptions)
    {
        typeSelector.setType(synthesizerOptions.getSynthesisType());
        algorithmSelector.setAlgorithm(synthesizerOptions.getSynthesisAlgorithm());
        printGuardBox.setSelected(synthesizerOptions.getPrintGuard());
        reachableBox.setSelected(synthesizerOptions.getReachability());
        peakBDDBox.setSelected(synthesizerOptions.getPeakBDD());
        globalClockDomainField.setEnabled(timeOptBox.isSelected());

/*
        boolean selected = saveIDDInFileBox.isSelected() || saveEventGuardInFileBox.isSelected() ||
                                addGuardsBox.isSelected() || printGuardBox.isSelected();
        optimalButton.setEnabled(selected);
        fromAllowedStatesButton.setEnabled(selected);
        fromForbiddenStatesButton.setEnabled(selected);
        eventList.setEnabled(selected);
        complementHeuristicBox.setEnabled(selected);
        independentHeuristicBox.setEnabled(selected);
*/
    }

    public void updatePanel()
    {
        globalClockDomainField.setEnabled(timeOptBox.isSelected());
        //At present, it is only possible to perform the synthesis via BDDs
        final SynthesisAlgorithm selected = algorithmSelector.getAlgorithm();
        // Clear, then add the ones that are implemented
        algorithmSelector.removeAllItems();
        if (typeSelector.getType() == SynthesisType.CONTROLLABLE)
        {
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD);
            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD);
//            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD_Automaton);
//            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD_Variable);
/*            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
            algorithmSelector.addItem(SynthesisAlgorithm.MODULAR);
            algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
            algorithmSelector.addItem(SynthesisAlgorithm.BDD);
*/
        }
        else if (typeSelector.getType() == SynthesisType.NONBLOCKING)
        {
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD);
            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD);
//            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD_Automaton);
//            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD_Variable);

/*
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD);
            algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
            algorithmSelector.addItem(SynthesisAlgorithm.BDD);
*/
        }
        else if (typeSelector.getType() == SynthesisType.NONBLOCKING_CONTROLLABLE)
        {
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD);
            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD);
//            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD_Automaton);
//            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD_Variable);
/*
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
            algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
            algorithmSelector.addItem(SynthesisAlgorithm.BDD);
*/
        }
        else if (typeSelector.getType() == SynthesisType.UNSAFETY)
        {
            algorithmSelector.addItem(SynthesisAlgorithm.MINIMALITY_C);
            algorithmSelector.addItem(SynthesisAlgorithm.MINIMALITY_M);
            algorithmSelector.addItem(SynthesisAlgorithm.MINIMALITY_P);
        }


        // Default selection

        algorithmSelector.setSelectedIndex(0);
        // Reselect previously selected item if possible
        algorithmSelector.setAlgorithm(selected);
    }

    @Override
    public void regain(final EditorSynthesizerOptions synthesizerOptions)
    {
        synthesizerOptions.setSynthesisType(typeSelector.getType());
        synthesizerOptions.setSynthesisAlgorithm(algorithmSelector.getAlgorithm());
        synthesizerOptions.setPrintGuard(printGuardBox.isSelected());
        synthesizerOptions.setAddGuards(addGuardsBox.isSelected());
        synthesizerOptions.setSaveInFile(saveEventGuardInFileBox.isSelected());
        synthesizerOptions.setSaveIDDInFile(saveIDDInFileBox.isSelected());
        synthesizerOptions.setReachability(reachableBox.isSelected());
        synthesizerOptions.setPeakBDD(peakBDDBox.isSelected());
        synthesizerOptions.setCompHeuristic(complementHeuristicBox.isSelected());
        synthesizerOptions.setIndpHeuristic(independentHeuristicBox.isSelected());
        synthesizerOptions.setOptimization(timeOptBox.isSelected());
        synthesizerOptions.setGlobalClockDomain(Long.parseLong(globalClockDomainField.getText()));

        if(fromForbiddenStatesButton.isSelected())
        {
            synthesizerOptions.setExpressionType(0);
        }
        if(fromAllowedStatesButton.isSelected())
        {
            synthesizerOptions.setExpressionType(1);
        }
        if(optimalButton.isSelected())
        {
            synthesizerOptions.setExpressionType(2);
        }

        if(eventList.getSelectedIndex() == 0)
            synthesizerOptions.setEvent("");
        else
            synthesizerOptions.setEvent((String)eventList.getSelectedItem());

        if(variablesList.getSelectedIndex() == 0)
            synthesizerOptions.setOptVariable("");
        else
            synthesizerOptions.setOptVariable((String)variablesList.getSelectedItem());

        if(minVarButton.isSelected())
            synthesizerOptions.setTypeOfVarOpt(true);

        if(maxVarButton.isSelected())
            synthesizerOptions.setTypeOfVarOpt(false);

    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        // Default

        if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MONOLITHIC)
        {
           ;// Will be completed later
        }
        else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.COMPOSITIONAL)
        {
            ;// Will be completed later
        }
        else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MODULAR)
        {
            ;// Will be completed later
        }

/*
        boolean selected = saveIDDInFileBox.isSelected() || saveEventGuardInFileBox.isSelected() ||
                                addGuardsBox.isSelected() || printGuardBox.isSelected();
        optimalButton.setEnabled(selected);
        fromAllowedStatesButton.setEnabled(selected);
        fromForbiddenStatesButton.setEnabled(selected);
        eventList.setEnabled(selected);
        complementHeuristicBox.setEnabled(selected);
        independentHeuristicBox.setEnabled(selected);
*/
        updatePanel();
    }
}


public class EditorSynthesizerDialog
    implements ActionListener
{
    private final JButton okButton;
    private final JButton cancelButton;
    private final EditorSynthesizerOptions synthesizerOptions;
    EditorSynthesizerDialogStandardPanel standardPanel;

    private final JDialog dialog;

    /**
     * Creates modal dialog box for input of synthesizer and guard  options.
     */
    public EditorSynthesizerDialog(final Frame parentFrame, final int numSelected, final EditorSynthesizerOptions synthesizerOptions, final Vector<String> events, final Vector<String> variables)
    {
        this.synthesizerOptions = synthesizerOptions;
        synthesizerOptions.setReachability(true);
        dialog = new JDialog(parentFrame, true);    // modal
        dialog.setTitle("Synthesizer options");
        standardPanel =
          new EditorSynthesizerDialogStandardPanel(numSelected, events, variables);
        final JPanel buttonPanel = new JPanel();
        okButton = addButton(buttonPanel, "OK");
        cancelButton = addButton(buttonPanel, "Cancel");
        dialog.add(standardPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        Utility.setDefaultButton(dialog, okButton);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        final Dimension size = dialog.getSize();
        dialog.setMinimumSize(size);
        update();
    }


    /**
     * Updates the information in the dialog from what is recorded in EditorSynthesizerOptions.
     * @see EditorSynthesizerOptions
     */
    public void update()
    {
        standardPanel.update(synthesizerOptions);
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

    @Override
    public void actionPerformed(final ActionEvent event)
    {
        final Object source = event.getSource();

        if (source == okButton)
        {
            standardPanel.regain(synthesizerOptions);

            if (synthesizerOptions.isValid())
            {
                synthesizerOptions.saveOptions();
                synthesizerOptions.setDialogOK(true);

                dialog.setVisible(false);
                dialog.dispose();
            }
            else
            {
                final Container parent = dialog.getParent();
                JOptionPane.showMessageDialog(parent, "Invalid combination of type and algorithm", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            //////////////

            //////////////
        }
        else if (source == cancelButton)
        {
            synthesizerOptions.setDialogOK(false);
            dialog.setVisible(false);
            dialog.dispose();
        }
    }
}
