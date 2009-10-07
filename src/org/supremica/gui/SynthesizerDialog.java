
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

abstract class SynthesizerPanel
    extends JPanel
{
    public abstract void update(SynthesizerOptions s);
    
    public abstract void regain(SynthesizerOptions s);
}

class SynthesizerDialogStandardPanel
    extends SynthesizerPanel
    implements ActionListener
{
    private static final long serialVersionUID = 1L;
    private SynthesisSelector typeSelector;
    private AlgorithmSelector algorithmSelector;
    private JCheckBox purgeBox;
    private JCheckBox removeUnecessarySupBox;
    private NonblockNote nbNote;
    
    static class AlgorithmSelector
        extends JComboBox
    {
        private static final long serialVersionUID = 1L;
        
        private AlgorithmSelector()
        {
            super();
        }
        
        private AlgorithmSelector(Object[] array)
        {
            super(array);
        }
        
        private AlgorithmSelector(SynthesisAlgorithm algo)
        {
            addItem(algo);
        }
        
        public SynthesisAlgorithm getAlgorithm()
        {
            return (SynthesisAlgorithm) getSelectedItem();
        }
        
        public void setAlgorithm(SynthesisAlgorithm algo)
        {
            setSelectedItem(algo);
        }
        
        public static AlgorithmSelector create(int num)
        {
            if (num == 1)
            {
                AlgorithmSelector selector = new AlgorithmSelector();
                for (SynthesisAlgorithm algo: SynthesisAlgorithm.values())
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
                AlgorithmSelector selector = new AlgorithmSelector();
                for (SynthesisAlgorithm algo: SynthesisAlgorithm.values())
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
            super(SynthesisType.values());
        }
        
        public SynthesisType getType()
        {
            return (SynthesisType) getSelectedItem();
        }
        
        public void setType(SynthesisType type)
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
    
    public SynthesizerDialogStandardPanel(int num)
    {
        algorithmSelector = AlgorithmSelector.create(num);
        algorithmSelector.addActionListener(this);
        
        typeSelector = SynthesisSelector.create();
        typeSelector.addActionListener(this);
        
        purgeBox = new JCheckBox("Purge result");
        purgeBox.setToolTipText("Remove all forbidden states");
        
        removeUnecessarySupBox = new JCheckBox("Remove unnecessary supervisors");
        removeUnecessarySupBox.setToolTipText("Remove supervisors that don't affect the controllability");
        
        nbNote = new NonblockNote();
        
        if (num == 1)
        {
            removeUnecessarySupBox.setEnabled(false);
            nbNote.setVisible(false);
        }
        
        // Create layout!
        Box mainBox = Box.createVerticalBox();
        
        JPanel panel = new JPanel();
        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Property:"));
        box.add(typeSelector);
        panel.add(box);
        mainBox.add(panel);
        
        panel = new JPanel();
        box = Box.createHorizontalBox();
        box.add(new JLabel("Algorithm: "));
        box.add(algorithmSelector);
        panel.add(box);
        mainBox.add(panel);
        
        panel = new JPanel();
        box = Box.createHorizontalBox();
        box.add(purgeBox);
        box.add(removeUnecessarySupBox);
        panel.add(box);
        mainBox.add(panel);
        
        panel = new JPanel();
        panel.add(nbNote);
        mainBox.add(panel);
        
        // Add components
        this.add(mainBox);
        
        updatePanel();
    }
    
    public void update(SynthesizerOptions synthesizerOptions)
    {
        typeSelector.setType(synthesizerOptions.getSynthesisType());
        algorithmSelector.setAlgorithm(synthesizerOptions.getSynthesisAlgorithm());
        purgeBox.setSelected(synthesizerOptions.doPurge());
        removeUnecessarySupBox.setSelected(synthesizerOptions.getRemoveUnecessarySupervisors());
    }
    
    public void updatePanel()
    {
        // Which algorithms should be enabled?
        // Remember current selection
        SynthesisAlgorithm selected = algorithmSelector.getAlgorithm();
        // Clear, then add the ones that are implemented
        algorithmSelector.removeAllItems();
        // Which type of verification?
        if (typeSelector.getType() == SynthesisType.CONTROLLABLE)
        {
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
            algorithmSelector.addItem(SynthesisAlgorithm.MODULAR);
            algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
            algorithmSelector.addItem(SynthesisAlgorithm.BDD);
        }
        else if (typeSelector.getType() == SynthesisType.NONBLOCKING)
        {
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD); 
            algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
            algorithmSelector.addItem(SynthesisAlgorithm.BDD);
        }
        else if (typeSelector.getType() == SynthesisType.NONBLOCKINGCONTROLLABLE)
        {
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
            algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
            algorithmSelector.addItem(SynthesisAlgorithm.BDD);
        }
        else if (typeSelector.getType() == SynthesisType.NONBLOCKINGCONTROLLABLEOBSERVABLE)
        {
            algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
        }
        // Default selection
        algorithmSelector.setSelectedIndex(0);
        // Reselect previously selected item if possible
        algorithmSelector.setAlgorithm(selected);        
    }
    
    public void regain(SynthesizerOptions synthesizerOptions)
    {
        synthesizerOptions.setSynthesisType(typeSelector.getType());
        synthesizerOptions.setSynthesisAlgorithm(algorithmSelector.getAlgorithm());
        synthesizerOptions.setPurge(purgeBox.isSelected());
        synthesizerOptions.setRemoveUnecessarySupervisors(removeUnecessarySupBox.isSelected());
    }
    
    public void actionPerformed(ActionEvent e)
    {
        //X stands for "Should be setEnabled but setEnabled does not work as it should(?)."
        
        // Default
        purgeBox.setVisible(true); //X
        removeUnecessarySupBox.setVisible(true); //X
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
        updatePanel();
    }
}

class SynthesizerDialogAdvancedPanel
    extends SynthesizerPanel
    implements ActionListener
{
    private static final long serialVersionUID = 1L;
    private JCheckBox reduceSupervisorsBox;
    private JCheckBox oneEventAtATimeBox;
    private JCheckBox maximallyPermissiveBox;
    private JCheckBox maximallyPermissiveIncrementalBox;
    private JCheckBox maximallyPermissiveOnePlantAtATimeBox;
    private JTextArea note;
    
    public SynthesizerDialogAdvancedPanel()
    {
        Box advancedBox = Box.createVerticalBox();
        
        oneEventAtATimeBox = new JCheckBox("One event at a time (experimental)");
        oneEventAtATimeBox.setToolTipText("Synthesize with respect to one event at a time");
        
        maximallyPermissiveBox = new JCheckBox("Maximally permissive result");
        maximallyPermissiveBox.setToolTipText("Guarantee maximally permissive result");
        maximallyPermissiveBox.addActionListener(this);
        
        maximallyPermissiveIncrementalBox = new JCheckBox("Incremental algorithm");
        maximallyPermissiveIncrementalBox.setToolTipText("Use incremental algorithm for maximally permissive synthesis");
        maximallyPermissiveIncrementalBox.addActionListener(this);
        
        maximallyPermissiveOnePlantAtATimeBox = new JCheckBox("One plant at a time (experimental)");
        maximallyPermissiveOnePlantAtATimeBox.setToolTipText("Increment by one plant at a time");
        
        reduceSupervisorsBox = new JCheckBox("Reduce supervisors (experimental)");
        reduceSupervisorsBox.setToolTipText("Remove redundant states and events from " +
            "synthesized supervisors");
        reduceSupervisorsBox.addActionListener(this);
        
        advancedBox.add(oneEventAtATimeBox);
        advancedBox.add(maximallyPermissiveBox);
        advancedBox.add(maximallyPermissiveIncrementalBox);
        advancedBox.add(maximallyPermissiveOnePlantAtATimeBox);
        advancedBox.add(reduceSupervisorsBox);
        
        note = new JTextArea("Note:\n" + "'Purge result' must be selected for supervisor\n" +
            "reduction to work.\n");
        
        note.setBackground(new Color(0, 0, 0, 0));
        note.setVisible(false);
        this.add(advancedBox, BorderLayout.CENTER);
        this.add(note, BorderLayout.SOUTH);
    }
    
    public void update(SynthesizerOptions synthesizerOptions)
    {
        reduceSupervisorsBox.setSelected(synthesizerOptions.getReduceSupervisors());
        maximallyPermissiveBox.setSelected(synthesizerOptions.getMaximallyPermissive());
        maximallyPermissiveIncrementalBox.setSelected(synthesizerOptions.getMaximallyPermissiveIncremental());
    }
    
    private void updatePanel()
    {        
        if (!maximallyPermissiveBox.isSelected())
            maximallyPermissiveIncrementalBox.setSelected(false);
        if (!maximallyPermissiveIncrementalBox.isSelected())
            maximallyPermissiveOnePlantAtATimeBox.setSelected(false);

        maximallyPermissiveIncrementalBox.setEnabled(maximallyPermissiveBox.isSelected());
        maximallyPermissiveOnePlantAtATimeBox.setEnabled(maximallyPermissiveIncrementalBox.isSelected() &&
        maximallyPermissiveIncrementalBox.isEnabled());
    }
    
    public void regain(SynthesizerOptions options)
    {
        options.setReduceSupervisors(reduceSupervisorsBox.isSelected());
        options.setMaximallyPermissive(maximallyPermissiveBox.isSelected());
        options.setMaximallyPermissiveIncremental(maximallyPermissiveIncrementalBox.isSelected());
        options.addOnePlantAtATime = maximallyPermissiveOnePlantAtATimeBox.isSelected();
        options.oneEventAtATime = oneEventAtATimeBox.isSelected();
    }
    
    public void actionPerformed(ActionEvent e)
    {
        // Incremental box enabled?
        maximallyPermissiveIncrementalBox.setEnabled(maximallyPermissiveBox.isSelected());
        
        // Display note?
        note.setVisible(reduceSupervisorsBox.isSelected());
        
        updatePanel();
    }
}

class SynthesizerDialogGuardPanel
    extends SynthesizerPanel
{
	private static final long serialVersionUID = 1L;
    private JRadioButton fromAllowedStatesButton;
    private JRadioButton fromForbiddenStatesButton;
    private JRadioButton optimalButton;
    private JComboBox eventList;
//	private JTextField eventField;

    public SynthesizerDialogGuardPanel(Vector events)
    {
		Box standardBox = Box.createVerticalBox();

		fromAllowedStatesButton = new JRadioButton("From allowed states");
		fromAllowedStatesButton.setToolTipText("Generate the guard from the Allowed state set");

        fromForbiddenStatesButton = new JRadioButton("From forbidden states");
		fromForbiddenStatesButton.setToolTipText("Generate the guard from the Forbidden state set");

        optimalButton = new JRadioButton("Optimal solution");
		optimalButton.setToolTipText("Generate the guard from the state set that yields the best result");

		JLabel event = new JLabel("Events");
//		eventField = new JTextField(15);
//		eventField.setToolTipText("The name of the desired event");

        ButtonGroup group = new ButtonGroup();
        group.add(fromAllowedStatesButton);
        group.add(fromForbiddenStatesButton);
        group.add(optimalButton);

        JPanel expressionTypePanel = new JPanel();
        expressionTypePanel.add(fromAllowedStatesButton);
        expressionTypePanel.add(fromForbiddenStatesButton);
        expressionTypePanel.add(optimalButton);

        eventList = new JComboBox(events);

        standardBox.add(expressionTypePanel);
		standardBox.add(event);
//        standardBox.add(eventField);
        standardBox.add(eventList);

		this.add(standardBox);
    }

	public void update(SynthesizerOptions synthesizerOptions)
	{
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

//        eventField.setText(guardOptions.getEvent());
	}

	public void regain(SynthesizerOptions synthesizerOptions)
	{
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
}

public class SynthesizerDialog
    implements ActionListener
{
    private JButton okButton;
    private JButton cancelButton;
    private SynthesizerOptions synthesizerOptions;
    SynthesizerDialogStandardPanel standardPanel;
    SynthesizerDialogAdvancedPanel advancedPanel;
    SynthesizerDialogGuardPanel guardPanel;

    private JDialog dialog;
    private Frame parentFrame;
    
    /**
     * Creates modal dialog box for input of synthesizer options.
     */
    public SynthesizerDialog(Frame parentFrame, int numSelected, SynthesizerOptions synthesizerOptions)
    {
        dialog = new JDialog(parentFrame, true);    // modal
        this.parentFrame = parentFrame;
        this.synthesizerOptions = synthesizerOptions;
        
        dialog.setTitle("Synthesizer options");
        dialog.setSize(new Dimension(400, 300));
        
        Container contentPane = dialog.getContentPane();
        
        standardPanel = new SynthesizerDialogStandardPanel(numSelected);
        advancedPanel = new SynthesizerDialogAdvancedPanel();
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");
        tabbedPane.addTab("Advanced options", null, advancedPanel, "Advanced options");
//        tabbedPane.addTab("Guard options", null, guardPanel, "Guard options");
        
        // buttonPanel
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

    public SynthesizerDialog(Frame parentFrame, int numSelected, SynthesizerOptions synthesizerOptions, Vector controllableEvents)
    {
        this(parentFrame, numSelected, synthesizerOptions);
        guardPanel = new SynthesizerDialogGuardPanel(controllableEvents);

    }

    
    /**
     * Updates the information in the dialog from what is recorded in SynthesizerOptions.
     * @see SynthesizerOptions
     */
    public void update()
    {
        standardPanel.update(synthesizerOptions);
        advancedPanel.update(synthesizerOptions);
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
            standardPanel.regain(synthesizerOptions);
            advancedPanel.regain(synthesizerOptions);
            
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
