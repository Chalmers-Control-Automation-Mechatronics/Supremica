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


abstract class SynthesizerPanel extends JPanel
{
	public abstract void update(SynthesizerOptions s);
	public abstract void regain(SynthesizerOptions s);
}

class SD_StandardPanel extends SynthesizerPanel
{
	private JComboBox synthesisTypeBox;
	private JComboBox algorithmTypeBox;
	private JCheckBox purgeBox;
	private JCheckBox optimizeBox;

	public SD_StandardPanel()
	{
		Box standardBox = Box.createVerticalBox();

		synthesisTypeBox = new JComboBox(SynthesisType.toArray());

		algorithmTypeBox = new JComboBox(SynthesisAlgorithm.toArray());
		purgeBox = new JCheckBox("Purge result");
		optimizeBox = new JCheckBox("Optimize result");

		standardBox.add(synthesisTypeBox);
		standardBox.add(algorithmTypeBox);
		standardBox.add(purgeBox);
		standardBox.add(optimizeBox);

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
		synthesizerOptions.setSynthesisType((SynthesisType)synthesisTypeBox.getSelectedItem());
		synthesizerOptions.setSynthesisAlgorithm((SynthesisAlgorithm)algorithmTypeBox.getSelectedItem());
		synthesizerOptions.setPurge(purgeBox.isSelected());
		synthesizerOptions.setOptimize(optimizeBox.isSelected());
	}
}
class SD_AdvancedPanel extends SynthesizerPanel
{
	private JCheckBox maximallyPermissiveBox;

	public SD_AdvancedPanel()
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

	SD_StandardPanel standardPanel;

	// private JCheckBox maximallyPermissiveBox;
	SD_AdvancedPanel advancedPanel;

	private JDialog dialog;

	private JFrame parentFrame;

	/**
	 * Creates modal dialog box for input of synthesizer options.
	 */
	public SynthesizerDialog(JFrame parentFrame, SynthesizerOptions synthesizerOptions)
	{
		dialog = new JDialog(parentFrame, true); //modal
		this.parentFrame = parentFrame;
		this.synthesizerOptions = synthesizerOptions;
		dialog.setTitle("Synthesizer options");
		dialog.setSize(new Dimension(400, 300));

		Container contentPane = dialog.getContentPane();

		standardPanel = new SD_StandardPanel();
		advancedPanel = new SD_AdvancedPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Standard options", null,
                          standardPanel,
                          "Standard options");
        tabbedPane.addTab("Advanced options", null,
                          advancedPanel,
                          "Advanced options");

		/** standardPanel
		Box standardBox = Box.createVerticalBox();
		// JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		// JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String[] synthesisData = {"controllable", "non-blocking", "both"};
		synthesisTypeBox = new JComboBox(synthesisData);
		String[] algorithmData = {"modular", "monolithic", "IDD"};
		algorithmTypeBox = new JComboBox(algorithmData);
		purgeBox = new JCheckBox("Purge result");
		optimizeBox = new JCheckBox("Optimize result");
		// leftPanel.add(synthesisTypeBox);
		// leftPanel.add(algorithmTypeBox);
		// rightPanel.add(purgeBox);
		// rightPanel.add(optimizeBox);
		// standardPanel.setLayout(new GridLayout(1,2));
		// standardPanel.add("Center", leftPanel);
		// standardPanel.add("Center", rightPanel);

		standardBox.add(synthesisTypeBox);
		standardBox.add(algorithmTypeBox);
		standardBox.add(purgeBox);
		standardBox.add(optimizeBox);

		standardPanel.add(standardBox); **/

		/** advancedPanel
		Box advancedBox = Box.createVerticalBox();
		maximallyPermissiveBox = new JCheckBox("Maximally permissive result");

		advancedBox.add(maximallyPermissiveBox);

		advancedPanel.add(advancedBox); **/

		// buttonPanel
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
			synthesizerOptions.setDialogOK(false); // Already done...
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
}
