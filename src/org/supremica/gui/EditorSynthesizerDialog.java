
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

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import org.supremica.automata.algorithms.*;

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
    private final Box guardBox;
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

        // Create layout!
        final Box mainBox = Box.createVerticalBox();

        final JPanel supervisorPanel = new JPanel();
	supervisorPanel.setBorder(BorderFactory.createTitledBorder("Supervisor"));
        supervisorPanel.setLayout(new GridLayout(3, 1));

        JPanel panel = new JPanel();
        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Property:"));
        box.add(typeSelector);
        panel.add(box);
        supervisorPanel.add(panel);
//        mainBox.add(panel);

        panel = new JPanel();
        box = Box.createHorizontalBox();
        box.add(new JLabel("Algorithm: "));
        //Until now it is only possible to perform the synthesis via BDDs
        algorithmSelector.setEnabled(true);
        box.add(algorithmSelector);
        panel.add(box);
        supervisorPanel.add(panel);
//        mainBox.add(panel);

        panel = new JPanel();
        box = Box.createHorizontalBox();
        reachableBox = new JCheckBox("Remove the unreachable states");
        reachableBox.setEnabled(true);
        reachableBox.setVisible(true);
        box.add(reachableBox);
        reachableBox.addActionListener(this);
        panel.add(box);
        supervisorPanel.add(panel);
//        mainBox.add(panel);

        mainBox.add(supervisorPanel);

        // Add components
        this.add(mainBox);

        //Guard options
        guardBox = Box.createVerticalBox();

        fromAllowedStatesButton = new JRadioButton("From allowed states");
        fromAllowedStatesButton.setToolTipText("Generate the guard from the states where the event is allowed to occur");

        fromForbiddenStatesButton = new JRadioButton("From forbidden states");
	fromForbiddenStatesButton.setToolTipText("Generate the guard from the states where the event is forbidden to occur");

        optimalButton = new JRadioButton("Adaptive solution");
        optimalButton.setToolTipText("Generate the guard from the state set that yields the best result");
        optimalButton.setSelected(true);

//		eventField = new JTextField(15);
//		eventField.setToolTipText("The name of the desired event");

        ButtonGroup group = new ButtonGroup();
        group.add(fromAllowedStatesButton);
        group.add(fromForbiddenStatesButton);
        group.add(optimalButton);

        complementHeuristicBox = new JCheckBox("Complement Heuristic");
        complementHeuristicBox.setToolTipText("Apply the 'Complement Heuristic'.");
        complementHeuristicBox.setEnabled(true);
        complementHeuristicBox.setSelected(true);

        independentHeuristicBox = new JCheckBox("Independent Heuristic");
        independentHeuristicBox.setToolTipText("Apply the 'Independent Heuristic'.");
        independentHeuristicBox.setEnabled(true);
        independentHeuristicBox.setSelected(true);

        final JPanel expressionTypePanel = new JPanel();
        expressionTypePanel.setBorder(BorderFactory.createTitledBorder("Guard computation"));
        expressionTypePanel.setLayout(new GridLayout(3, 1));
        panel = new JPanel();
        panel.add(optimalButton);
        panel.add(fromAllowedStatesButton);
        panel.add(fromForbiddenStatesButton);
        expressionTypePanel.add(panel);
        panel = new JPanel();
        panel.add(complementHeuristicBox);
        panel.add(independentHeuristicBox);
        expressionTypePanel.add(panel);

        eventList = new JComboBox<String>(events);

        expressionTypePanel.add(eventList);

        guardBox.add(expressionTypePanel);
        guardBox.setVisible(true);

        this.add(guardBox);

        //Presentation options
        genGuardComputeSupBox = Box.createVerticalBox();

        printGuardBox = new JCheckBox("Print the guards");
        printGuardBox.setToolTipText("Compute and print the guards.");

        printGuardBox.addActionListener(this);

        addGuardsBox = new JCheckBox("Add guards to the model");
        addGuardsBox.setToolTipText("Compute and add the guards to the model.");

        addGuardsBox.addActionListener(this);

        saveEventGuardInFileBox = new JCheckBox("Save the result in a file");
        saveEventGuardInFileBox.setToolTipText("Compute and write the event-guard pairs in a file.");

        saveEventGuardInFileBox.addActionListener(this);

        saveIDDInFileBox = new JCheckBox("Save the result as an IDD in a file");
        saveIDDInFileBox.setToolTipText("For each event an IDD (expression graph) is generated representing the guard. The file is saved in .ps format.");

        saveIDDInFileBox.addActionListener(this);

        final JPanel representationPanel = new JPanel();
        representationPanel.setBorder(BorderFactory.createTitledBorder("Guard representation"));
        representationPanel.setLayout(new GridLayout(4, 1));
        representationPanel.add(printGuardBox);
        representationPanel.add(addGuardsBox);
        representationPanel.add(saveEventGuardInFileBox);
        representationPanel.add(saveIDDInFileBox);

        genGuardComputeSupBox.add(representationPanel);

        this.add(genGuardComputeSupBox);

        final JPanel optimizationPanel = new JPanel(new GridLayout(5, 2));
        optimizationPanel.setBorder(BorderFactory.createTitledBorder("Optimization"));

        //Optimization options

        timeOptBox = new JCheckBox("Compute optimal time");
        timeOptBox.setToolTipText("Compute the supervisor that will restrict the plant to reach a marked state with the minimum time.");

        timeOptBox.addActionListener(this);

        optimizationPanel.add(timeOptBox);
        optimizationPanel.add(new JLabel());

        optimizationPanel.add(new JLabel("Enter the global time domain:"));
        globalClockDomainField = new JTextField("0");
        globalClockDomainField.setToolTipText("Guess a reasonably large amount of time to reach a marked state");
        globalClockDomainField.setEnabled(false);
        optimizationPanel.add(globalClockDomainField);

        final JLabel miniSupLabel = new JLabel("Optimize variable:");
        miniSupLabel.setToolTipText("Compute the supervisor that ensures the variable will have its optimal value among the reachable markes states.");
        optimizationPanel.add(miniSupLabel);
        optimizationPanel.add(new JLabel());

        minVarButton = new JRadioButton("Minimize");
        minVarButton.setSelected(true);
        maxVarButton = new JRadioButton("Maximize");

//		eventField = new JTextField(15);
//		eventField.setToolTipText("The name of the desired event");

        group = new ButtonGroup();
        group.add(minVarButton);
        group.add(maxVarButton);

        optimizationPanel.add(minVarButton);
        optimizationPanel.add(maxVarButton);

        variablesList = new JComboBox<String>(variables);
        optimizationPanel.add(variablesList);


//        optimizationSupBox.add(optimizationPanel);

        this.add(optimizationPanel);

        updatePanel();
    }

    public void update(final EditorSynthesizerOptions synthesizerOptions)
    {
        typeSelector.setType(synthesizerOptions.getSynthesisType());
        algorithmSelector.setAlgorithm(synthesizerOptions.getSynthesisAlgorithm());
        printGuardBox.setSelected(synthesizerOptions.getPrintGuard());
        reachableBox.setSelected(synthesizerOptions.getReachability());
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
        else if (typeSelector.getType() == SynthesisType.NONBLOCKINGCONTROLLABLE)
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
            //algorithmSelector.addItem(SynthesisAlgorithm.MINIMALITY_P);
        }


        // Default selection

        algorithmSelector.setSelectedIndex(0);
        // Reselect previously selected item if possible
        algorithmSelector.setAlgorithm(selected);
    }

    public void regain(final EditorSynthesizerOptions synthesizerOptions)
    {
        synthesizerOptions.setSynthesisType(typeSelector.getType());
        synthesizerOptions.setSynthesisAlgorithm(algorithmSelector.getAlgorithm());
        synthesizerOptions.setPrintGuard(printGuardBox.isSelected());
        synthesizerOptions.setAddGuards(addGuardsBox.isSelected());
        synthesizerOptions.setSaveInFile(saveEventGuardInFileBox.isSelected());
        synthesizerOptions.setSaveIDDInFile(saveIDDInFileBox.isSelected());
        synthesizerOptions.setReachability(reachableBox.isSelected());
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
    private final Frame parentFrame;

    /**
     * Creates modal dialog box for input of synthesizer and guard  options.
     */
    public EditorSynthesizerDialog(final Frame parentFrame, final int numSelected, final EditorSynthesizerOptions synthesizerOptions, final Vector<String> events, final Vector<String> variables)
    {
        dialog = new JDialog(parentFrame, true);    // modal
        this.parentFrame = parentFrame;
        this.synthesizerOptions = synthesizerOptions;
        synthesizerOptions.setReachability(true);

        dialog.setTitle("Synthesizer options");
        dialog.setSize(new Dimension(520, 500));

        final Container contentPane = dialog.getContentPane();

        standardPanel = new EditorSynthesizerDialogStandardPanel(numSelected, events, variables);

//        final JTabbedPane tabbedPane = new JTabbedPane();

//        tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");

        // buttonPanel
        final JPanel buttonPanel = new JPanel();

        okButton = addButton(buttonPanel, "OK");
        cancelButton = addButton(buttonPanel, "Cancel");

        contentPane.add("Center", standardPanel);
        contentPane.add("South", buttonPanel);
        Utility.setDefaultButton(dialog, okButton);

        // ** MF ** Fix to get the frigging thing centered
        final Dimension dim = dialog.getMinimumSize();

        dialog.setLocation(Utility.getPosForCenter(dim));
        dialog.setResizable(false);
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
                JOptionPane.showMessageDialog(parentFrame, "Invalid combination of type and algorithm", "Alert", JOptionPane.ERROR_MESSAGE);
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
