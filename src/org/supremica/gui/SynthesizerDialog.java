
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
	extends SynthesizerPanel implements ActionListener
{
	private JComboBox synthesisTypeBox;
	private JComboBox algorithmTypeBox;
	private JCheckBox purgeBox;
	private JCheckBox optimizeBox;
	private NonblockNote nbNote;
	
	class NonblockNote
		extends JPanel
	{
		boolean enabled = false;
		
		public void enable()
		{
			if(!enabled)
			{
				super.setLayout(new GridLayout(5,1));
				super.add(new JLabel("Note:"));
				super.add(new JLabel("Currently, the modular nonblocking algorithm"));
				super.add(new JLabel("does not gurantee global nonblocking. The only"));
				super.add(new JLabel("gurantee is that each supervisor is nonblockng"));
				super.add(new JLabel("with respect to the plants that it controls"));
				super.revalidate();
				enabled = true;
			}
		}
		public void disable()
		{
			if(enabled)
			{
				super.removeAll();
				super.revalidate();
				enabled = false;
			}
		}
	}

	public SynthesizerDialogStandardPanel(int num)
	{
		Box standardBox = Box.createVerticalBox();
		
		if(num > 1)
		{
			algorithmTypeBox = new JComboBox(SynthesisAlgorithm.toArray());
		}
		else
		{
			algorithmTypeBox = new JComboBox();
			algorithmTypeBox.addItem(SynthesisAlgorithm.Monolithic);
		}
		algorithmTypeBox.addActionListener(this);
		
		synthesisTypeBox = new JComboBox(SynthesisType.toArray());
		synthesisTypeBox.addActionListener(this);
		
		purgeBox = new JCheckBox("Purge result");
		purgeBox.setToolTipText("Remove all forbidden states");
		
		optimizeBox = new JCheckBox("Optimize result");
		optimizeBox.setToolTipText("Remove supervisors that don't affect the controllability");
		
		nbNote = new NonblockNote();
		
		if(num == 1)
		{		
			optimizeBox.setEnabled(false);
			nbNote.disable();
		}
		
		standardBox.add(synthesisTypeBox);
		standardBox.add(algorithmTypeBox);
		standardBox.add(purgeBox);
		standardBox.add(optimizeBox);
		standardBox.add(nbNote);
		this.add(standardBox);
	}

	public void update(SynthesizerOptions synthesizerOptions)
	{
		synthesisTypeBox.setSelectedItem(synthesizerOptions.getSynthesisType());
		algorithmTypeBox.setSelectedItem(synthesizerOptions.getSynthesisAlgorithm());
		purgeBox.setSelected(synthesizerOptions.doPurge());
		optimizeBox.setSelected(synthesizerOptions.getOptimize());
	}

	public void regain(SynthesizerOptions synthesizerOptions)
	{
		synthesizerOptions.setSynthesisType((SynthesisType) synthesisTypeBox.getSelectedItem());
		synthesizerOptions.setSynthesisAlgorithm((SynthesisAlgorithm) algorithmTypeBox.getSelectedItem());
		synthesizerOptions.setPurge(purgeBox.isSelected());
		synthesizerOptions.setOptimize(optimizeBox.isSelected());
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if((SynthesisAlgorithm) algorithmTypeBox.getSelectedItem() == SynthesisAlgorithm.Monolithic)
		{
			optimizeBox.setEnabled(false);
			nbNote.disable();
		}
		else // modular
		{
			optimizeBox.setEnabled(true);
			if((SynthesisType) synthesisTypeBox.getSelectedItem() == SynthesisType.Controllable)
			{
				nbNote.disable();
			}
			else // some type of nb, show the sign
			{
				nbNote.enable();
			}
		}
	}
}

class SynthesizerDialogAdvancedPanel
	extends SynthesizerPanel
{
	private JCheckBox maximallyPermissiveBox;

	public SynthesizerDialogAdvancedPanel()
	{
		Box advancedBox = Box.createVerticalBox();

		maximallyPermissiveBox = new JCheckBox("Maximally permissive result");

		advancedBox.add(maximallyPermissiveBox);
		this.add(advancedBox);
	}

	public void update(SynthesizerOptions synthesizerOptions)
	{
		maximallyPermissiveBox.setSelected(synthesizerOptions.getMaximallyPermissive());
	}

	public void regain(SynthesizerOptions synthesizerOptions)
	{
		synthesizerOptions.setMaximallyPermissive(maximallyPermissiveBox.isSelected());
	}
}

public class SynthesizerDialog
	implements ActionListener
{
	private JButton okButton;
	private JButton cancelButton;
	private SynthesizerOptions synthesizerOptions;
	SynthesizerDialogStandardPanel standardPanel;

	// private JCheckBox maximallyPermissiveBox;
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
			synthesizerOptions.setDialogOK(true);
			standardPanel.regain(synthesizerOptions);
			advancedPanel.regain(synthesizerOptions);

			if (synthesizerOptions.isValid())
			{
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
