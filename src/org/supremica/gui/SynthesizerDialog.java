
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

import java.awt.*;
import java.awt.event.*;
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
	private SynthesisSelector synthesisTypeBox;
	private AlgorithmSelector algorithmTypeBox;
	private JCheckBox purgeBox;
	private JCheckBox optimizeBox;
	private NonblockNote nbNote;

	static class AlgorithmSelector
		extends JComboBox
	{
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

				// return new AlgorithmSelector(SynthesisAlgorithm.Monolithic);
				return new AlgorithmSelector(SynthesisAlgorithm.toArray_oneAutomaton());
			}
			else
			{
				return new AlgorithmSelector(SynthesisAlgorithm.toArray());
			}
		}
	}

	static class SynthesisSelector
		extends JComboBox
	{
		private SynthesisSelector()
		{
			super(SynthesisType.toArray());
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
		private static final int transparent = 0;

		public NonblockNote()
		{
			super("Note:\n" + "Currently, the modular nonblocking algorithm\n" + "does not guarantee global nonblocking. The only\n" + "gurantee is that each supervisor is nonblockng\n" + "with respect to the plants that it controls");

			super.setBackground(new Color(0, 0, 0, transparent));
		}
	}

	public SynthesizerDialogStandardPanel(int num)
	{
		algorithmTypeBox = AlgorithmSelector.create(num);
		algorithmTypeBox.addActionListener(this);

		synthesisTypeBox = SynthesisSelector.create();
		synthesisTypeBox.addActionListener(this);

		purgeBox = new JCheckBox("Purge result");
		purgeBox.setToolTipText("Remove all forbidden states");

		optimizeBox = new JCheckBox("Optimize result");
		optimizeBox.setToolTipText("Remove supervisors that don't affect the controllability");

		nbNote = new NonblockNote();

		if (num == 1)
		{
			optimizeBox.setEnabled(false);
			nbNote.setVisible(false);
		}

		Box standardBox = Box.createVerticalBox();
		standardBox.add(synthesisTypeBox);
		standardBox.add(algorithmTypeBox);
		Box anotherBox = Box.createVerticalBox();
		anotherBox.add(purgeBox);
		anotherBox.add(optimizeBox);
		this.add(standardBox, BorderLayout.CENTER);
		this.add(anotherBox, BorderLayout.CENTER);
		this.add(nbNote, BorderLayout.SOUTH);
	}

	public void update(SynthesizerOptions synthesizerOptions)
	{
		synthesisTypeBox.setType(synthesizerOptions.getSynthesisType());
		algorithmTypeBox.setAlgorithm(synthesizerOptions.getSynthesisAlgorithm());
		purgeBox.setSelected(synthesizerOptions.doPurge());
		optimizeBox.setSelected(synthesizerOptions.getOptimize());
	}

	public void regain(SynthesizerOptions synthesizerOptions)
	{
		synthesizerOptions.setSynthesisType(synthesisTypeBox.getType());
		synthesizerOptions.setSynthesisAlgorithm(algorithmTypeBox.getAlgorithm());
		synthesizerOptions.setPurge(purgeBox.isSelected());
		synthesizerOptions.setOptimize(optimizeBox.isSelected());
	}

	public void actionPerformed(ActionEvent e)
	{
		if (algorithmTypeBox.getAlgorithm() == SynthesisAlgorithm.Monolithic)
		{
			optimizeBox.setEnabled(false);
		}

		if (algorithmTypeBox.getAlgorithm() == SynthesisAlgorithm.MonolithicSingleFixpoint)
		{
			optimizeBox.setEnabled(false);
		}

		if (algorithmTypeBox.getAlgorithm() == SynthesisAlgorithm.Modular)
		{
			optimizeBox.setEnabled(true);

			if (synthesisTypeBox.getType() == SynthesisType.Controllable)
			{
				nbNote.setVisible(false);
			}
			else    // some type of nb, show the sign
			{
				nbNote.setVisible(true);
			}
		}
		else
		{
			nbNote.setVisible(false);
		}
	}
}

class SynthesizerDialogAdvancedPanel
	extends SynthesizerPanel
	implements ActionListener
{
	private JCheckBox reduceSupervisorsBox;
	private JCheckBox maximallyPermissiveBox;
	private JCheckBox maximallyPermissiveIncrementalBox;
	private JTextArea note;

	public SynthesizerDialogAdvancedPanel()
	{
		Box advancedBox = Box.createVerticalBox();

		reduceSupervisorsBox = new JCheckBox("Reduce supervisors (experimental)");
		reduceSupervisorsBox.setToolTipText("Remove redundant states and events from synthesized supervisors");

		maximallyPermissiveBox = new JCheckBox("Maximally permissive result");
		maximallyPermissiveBox.setToolTipText("Guarantee maximally permissive result");

		maximallyPermissiveIncrementalBox = new JCheckBox("Incremental algorithm");
		maximallyPermissiveIncrementalBox.setToolTipText("Use incremental algorithm for maximally permissive synthesis");
		reduceSupervisorsBox.addActionListener(this);
		maximallyPermissiveIncrementalBox.addActionListener(this);
		maximallyPermissiveBox.addActionListener(this);
		advancedBox.add(reduceSupervisorsBox);
		advancedBox.add(maximallyPermissiveBox);
		advancedBox.add(maximallyPermissiveIncrementalBox);

		note = new JTextArea("Note:\n" + "'Purge result' must be selected for supervisor\n" + "reduction to work.\n");

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

	public void regain(SynthesizerOptions synthesizerOptions)
	{
		synthesizerOptions.setReduceSupervisors(reduceSupervisorsBox.isSelected());
		synthesizerOptions.setMaximallyPermissive(maximallyPermissiveBox.isSelected());
		synthesizerOptions.setMaximallyPermissiveIncremental(maximallyPermissiveIncrementalBox.isSelected());
	}

	public void actionPerformed(ActionEvent e)
	{

		// Incremental box enabled?
		maximallyPermissiveIncrementalBox.setEnabled(maximallyPermissiveBox.isSelected());

		// Display note?
		note.setVisible(reduceSupervisorsBox.isSelected());
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
	private JDialog dialog;
	private JFrame parentFrame;

	/**
	 * Creates modal dialog box for input of synthesizer options.
	 */
	public SynthesizerDialog(JFrame parentFrame, int numSelected, SynthesizerOptions synthesizerOptions)
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

	/**
	 * Updates the information in the dialog from what is recorded in SynthesizerOptions.
	 * @see SynchesizerOptions
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
		dialog.show();
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
		}
		else if (source == cancelButton)
		{
			synthesizerOptions.setDialogOK(false);    // Already done...
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
}
