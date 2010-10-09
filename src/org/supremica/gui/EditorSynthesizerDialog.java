
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
    private final JCheckBox purgeBox;
    private final JCheckBox removeUnecessarySupBox;
    private final JCheckBox reachableBox;
    private final Box guardBox;
    private final Box genGuardComputeSupBox;
    private final JRadioButton generateGuardButton;
    private final JRadioButton computeSupervisorButton;


    private final NonblockNote nbNote;

    private final JRadioButton fromAllowedStatesButton;
    private final JRadioButton fromForbiddenStatesButton;
    private final JRadioButton optimalButton;
    private final JComboBox eventList;

    static class AlgorithmSelector
        extends JComboBox
    {
        private static final long serialVersionUID = 1L;

        private AlgorithmSelector()
        {
            super();
        }

        private AlgorithmSelector(final Object[] array)
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
        extends JComboBox
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

    class NonblockNote
        extends JTextArea
    {
        private static final long serialVersionUID = 1L;
        private static final int transparent = 0;

        public NonblockNote()
        {
            super("Note:\n" + "Modular nonblocking synthesis results in a\n" +
                "compact representation of the monolithic\n" +
                "supervisor that Supremica can not currently\n" +
                "make use of.");

            super.setBackground(new Color(0, 0, 0, transparent));
        }
    }

    public EditorSynthesizerDialogStandardPanel(final int num, final Vector<String> events)
    {
        algorithmSelector = AlgorithmSelector.create(num);
        algorithmSelector.addActionListener(this);

        typeSelector = SynthesisSelector.create();
        typeSelector.addActionListener(this);

        purgeBox = new JCheckBox("Purge result");
        purgeBox.setToolTipText("Remove all forbidden states");

        removeUnecessarySupBox = new JCheckBox("Remove unnecessary supervisors");
        removeUnecessarySupBox.setToolTipText("Remove supervisors that don't affect the controllability");

        reachableBox = new JCheckBox("Remove the unreachable states");
        reachableBox.setEnabled(true);
        reachableBox.setVisible(true);

        nbNote = new NonblockNote();

        if (num == 1)
        {
            removeUnecessarySupBox.setEnabled(false);
            nbNote.setVisible(false);
        }

        // Create layout!
        final Box mainBox = Box.createVerticalBox();

        JPanel panel = new JPanel();
        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Property:"));
        box.add(typeSelector);
        panel.add(box);
        mainBox.add(panel);

        panel = new JPanel();
        box = Box.createHorizontalBox();
        box.add(new JLabel("Algorithm: "));
        //Until now it is only possible to perform the synthesis via BDDs
        algorithmSelector.setEnabled(true);
        box.add(algorithmSelector);
        panel.add(box);
        mainBox.add(panel);

        panel = new JPanel();
        box = Box.createHorizontalBox();
        box.add(purgeBox);
        box.add(removeUnecessarySupBox);
        box.add(reachableBox);
        reachableBox.addActionListener(this);
        panel.add(box);
        mainBox.add(panel);

        panel = new JPanel();
        panel.add(nbNote);
        mainBox.add(panel);

        // Add components
        this.add(mainBox);

        genGuardComputeSupBox = Box.createVerticalBox();

        generateGuardButton = new JRadioButton("Compute supervisor and guards");
        generateGuardButton.setToolTipText("Compute the supervisor and add the generated guards to the model.");

        generateGuardButton.addActionListener(this);

		computeSupervisorButton = new JRadioButton("Compute supervisor");
		computeSupervisorButton.setToolTipText("Compute the supervisor without generating guards.");

        computeSupervisorButton.addActionListener(this);
        final ButtonGroup grupp = new ButtonGroup();
        grupp.add(generateGuardButton);
        grupp.add(computeSupervisorButton);

        final JPanel genComPanel = new JPanel();
        genComPanel.add(computeSupervisorButton);
        genComPanel.add(generateGuardButton);

        genGuardComputeSupBox.add(genComPanel);

        this.add(genGuardComputeSupBox);

        //Guard options
        guardBox = Box.createVerticalBox();

        fromAllowedStatesButton = new JRadioButton("From allowed states");
        fromAllowedStatesButton.setToolTipText("Generate the guard from the states where the event is allowed to occur");

        fromForbiddenStatesButton = new JRadioButton("From forbidden states");
		fromForbiddenStatesButton.setToolTipText("Generate the guard from the states where the event is forbidden to occur");

        optimalButton = new JRadioButton("Adaptive solution");
        optimalButton.setToolTipText("Generate the guard from the state set that yields the best result");

        final JLabel event = new JLabel("Events");
//		eventField = new JTextField(15);
//		eventField.setToolTipText("The name of the desired event");

        final ButtonGroup group = new ButtonGroup();
        group.add(fromAllowedStatesButton);
        group.add(fromForbiddenStatesButton);
        group.add(optimalButton);

        final JPanel expressionTypePanel = new JPanel();
        expressionTypePanel.add(optimalButton);
        expressionTypePanel.add(fromAllowedStatesButton);
        expressionTypePanel.add(fromForbiddenStatesButton);

        eventList = new JComboBox(events);

        guardBox.add(expressionTypePanel);
		guardBox.add(event);
//        standardBox.add(eventField);
        guardBox.add(eventList);

        guardBox.setVisible(true);

        this.add(guardBox);

        updatePanel();
    }

    public void update(final EditorSynthesizerOptions synthesizerOptions)
    {
        typeSelector.setType(synthesizerOptions.getSynthesisType());
        algorithmSelector.setAlgorithm(synthesizerOptions.getSynthesisAlgorithm());
        purgeBox.setSelected(synthesizerOptions.doPurge());
        removeUnecessarySupBox.setSelected(synthesizerOptions.getRemoveUnecessarySupervisors());
        generateGuardButton.setSelected(synthesizerOptions.getGenerateGuard());
        reachableBox.setSelected(synthesizerOptions.getReachability());
        guardBox.setVisible(synthesizerOptions.getGenerateGuard());

        if(synthesizerOptions.getExpressionType() == 0)
        {
            fromAllowedStatesButton.setSelected(false);
            fromForbiddenStatesButton.setSelected(true);
            optimalButton.setSelected(false);
        }
        else if(synthesizerOptions.getExpressionType() == 1)
        {
            fromAllowedStatesButton.setSelected(true);
            fromForbiddenStatesButton.setSelected(false);
            optimalButton.setSelected(false);
        }
        else if(synthesizerOptions.getExpressionType() == 2)
        {
            fromAllowedStatesButton.setSelected(false);
            fromForbiddenStatesButton.setSelected(false);
            optimalButton.setSelected(true);
        }
    }

    public void updatePanel()
    {
        //At present, it is only possible to perform the synthesis via BDDs
        final SynthesisAlgorithm selected = algorithmSelector.getAlgorithm();
        // Clear, then add the ones that are implemented
        algorithmSelector.removeAllItems();
        if (typeSelector.getType() == SynthesisType.CONTROLLABLE)
        {
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD);
            algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD);
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
/*
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
            algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
            algorithmSelector.addItem(SynthesisAlgorithm.BDD);
*/
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
        synthesizerOptions.setPurge(purgeBox.isSelected());
        synthesizerOptions.setRemoveUnecessarySupervisors(removeUnecessarySupBox.isSelected());
        synthesizerOptions.setGenerateGuard(generateGuardButton.isSelected());
        synthesizerOptions.setReachability(reachableBox.isSelected());

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
    }

    public void actionPerformed(final ActionEvent e)
    {
        //X stands for "Should be setEnabled but setEnabled does not work as it should(?)."

        // Default
        purgeBox.setVisible(false); //X
        removeUnecessarySupBox.setVisible(false); //X
        guardBox.setVisible(true);
        nbNote.setVisible(false);

        if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MONOLITHIC)
        {
            removeUnecessarySupBox.setVisible(false); //X
        }
        else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.COMPOSITIONAL)
        {
            removeUnecessarySupBox.setVisible(false); //X
            purgeBox.setVisible(false); //X
        }
        else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MODULAR)
        {
            if ((typeSelector.getType() == SynthesisType.NONBLOCKING) ||
                (typeSelector.getType() == SynthesisType.NONBLOCKINGCONTROLLABLE))
            {
                purgeBox.setVisible(false); //X
                removeUnecessarySupBox.setVisible(false); //X
                nbNote.setVisible(true);
            }
        }

        guardBox.setVisible(generateGuardButton.isSelected());

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
    public EditorSynthesizerDialog(final Frame parentFrame, final int numSelected, final EditorSynthesizerOptions synthesizerOptions, final Vector<String> events)
    {
        dialog = new JDialog(parentFrame, true);    // modal
        this.parentFrame = parentFrame;
        this.synthesizerOptions = synthesizerOptions;
        synthesizerOptions.setGenerateGuard(true);
        synthesizerOptions.setReachability(true);

        dialog.setTitle("Synthesizer options");
        dialog.setSize(new Dimension(400, 330));

        final Container contentPane = dialog.getContentPane();

        standardPanel = new EditorSynthesizerDialogStandardPanel(numSelected, events);

        final JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");
//        tabbedPane.addTab("Guard options", null, guardPanel, "Guard options");

        // buttonPanel
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
            synthesizerOptions.setDialogOK(false);    // Already done...
            dialog.setVisible(false);
            dialog.dispose();
        }
    }
}
