
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

import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.automata.algorithms.minimization.MinimizationStrategy;
import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.minimization.BisimulationEquivalenceMinimizer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.automata.*;
import org.supremica.log.*;

public class MinimizationDialog
	implements ActionListener
{
    private static Logger logger = LoggerFactory.createLogger(MinimizationDialog.class);

	private JButton okButton;
	private JButton cancelButton;
	private MinimizationOptions options;
	private MinimizationDialogStandardPanel standardPanel;
	private MinimizationDialogAdvancedPanel advancedPanel;
	private JDialog dialog;

	/**
	 * Creates modal dialog box for input of verification options.
	 */
	public MinimizationDialog(JFrame parentFrame, MinimizationOptions options, Automata automata)
	{
		dialog = new JDialog(parentFrame, true);    // modal
		this.options = options;

		dialog.setTitle("Minimization options");
		dialog.setSize(new Dimension(400, 300));

		// dialog.setResizable(false);
		Container contentPane = dialog.getContentPane();

		advancedPanel = new MinimizationDialogAdvancedPanel(automata);
		standardPanel = new MinimizationDialogStandardPanel();

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");
		if (automata.size() > 1)
		{
			tabbedPane.addTab("Advanced options", null, advancedPanel, "Advanced options");
			//tabbedPane.setSelectedComponent(advancedPanel);
		}

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
	 * Updates the information in the dialog from what is recorded in MinimizationOptions.
	 * @see MinimizationOptions
	 */
	public void update()
	{
		advancedPanel.update(options);
		standardPanel.update(options);
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
			standardPanel.regain(options);
			advancedPanel.regain(options);
			options.saveOptions();
			options.setDialogOK(true);
			dialog.setVisible(false);
			dialog.dispose();
		}
		else if (source == cancelButton)
		{
			// Cancel
			options.setDialogOK(false);    // Already done...
			dialog.setVisible(false);
			dialog.dispose();
		}
	}

	abstract class MinimizationPanel
		extends JPanel
	{
		public abstract void update(MinimizationOptions options);

		public abstract void regain(MinimizationOptions options);
	}

	class MinimizationDialogStandardPanel
		extends MinimizationPanel
		implements ActionListener
	{
		private static final long serialVersionUID = 1L;

		private JComboBox minimizationTypeBox;
		private JCheckBox alsoTransitions;
		private JCheckBox keepOriginal;
		private JCheckBox ignoreMarking;

		private JTextArea note;

		public MinimizationDialogStandardPanel()
		{
			minimizationTypeBox = new JComboBox(EquivalenceRelation.toArray());
			minimizationTypeBox.addActionListener(this);

			// Disable bisimulation equivalence if library is missing!
			if (!BisimulationEquivalenceMinimizer.libraryLoaded())
			{
				logger.warn("Library BisimulationEquivalence not in path, using less efficient algorithm.");
				minimizationTypeBox.removeItem(EquivalenceRelation.BisimulationEquivalence);
			}

			alsoTransitions = new JCheckBox("Also minimize number of transitions");
			alsoTransitions.setToolTipText("Make sure that the number of the transitions is the minimal number (with respect to observation equivalence))");
			keepOriginal = new JCheckBox("Keep original");
			keepOriginal.setToolTipText("If unchecked, the selected automata are thrown away after completed execution");
			ignoreMarking = new JCheckBox("Ignore marking of states");
			ignoreMarking.setToolTipText("If checked, the marking is ignored, i.e. a marked state is not considered to be different from a nonmarked state with respect to equivalence");

			note = new JTextArea("Note:\n" + "I have nothing to say.");
			note.setBackground(this.getBackground());

			Box standardBox = Box.createHorizontalBox();
			standardBox.add(new JLabel("         ")); // Ugly fix to get stuff centered
			standardBox.add(minimizationTypeBox);
			standardBox.add(new JLabel("         ")); // Ugly fix to get stuff centered
			Box anotherBox = Box.createVerticalBox();
			anotherBox.add(alsoTransitions);
			anotherBox.add(keepOriginal);
			anotherBox.add(ignoreMarking);

			// NEW TRY
			this.setLayout(new GridLayout(2, 1));

			JPanel choicePanel = new JPanel();
			choicePanel.setLayout(new FlowLayout());
			choicePanel.add(standardBox);
			choicePanel.add(anotherBox);
			this.add(choicePanel);

			JPanel notePanel = new JPanel();
			notePanel.setLayout(new FlowLayout());
			notePanel.add(note);
			note.setVisible(false);
			this.add(notePanel);
		}

		public void update(MinimizationOptions options)
		{
			minimizationTypeBox.setSelectedItem(options.getMinimizationType());
			alsoTransitions.setSelected(options.getAlsoTransitions());
			keepOriginal.setSelected(options.getKeepOriginal());
			ignoreMarking.setSelected(options.getIgnoreMarking());
		}

		public void regain(MinimizationOptions options)
		{
			options.setMinimizationType((EquivalenceRelation) minimizationTypeBox.getSelectedItem());
			options.setAlsoTransitions(alsoTransitions.isSelected());
			options.setKeepOriginal(keepOriginal.isSelected());
			options.setIgnoreMarking(ignoreMarking.isSelected());
		}

		public void actionPerformed(ActionEvent e)
		{
			// Should we display a note?
			if ((minimizationTypeBox.getSelectedItem()) == EquivalenceRelation.ObservationEquivalence)
			{
				note.setText("Note:\n" +
							 "This minimization algorithm is experimental! The\n" +
							 "result may not be minimal but should at least be\n" +
							 "observation equivalent to the input with the excep-\n" +
							 "tion of epsilon loops where the marking is ignored.");
				note.setVisible(true);
			}
			else if ((minimizationTypeBox.getSelectedItem()) == EquivalenceRelation.LanguageEquivalence)
			{
				note.setText("Note:\n" +
							 "Returns an automaton representing the same language\n" +
							 "using a minimal number of states and transitions.\n" +
							 "  If the automaton is nondeterministic, it is first\n" +
							 "made deterministic.");
				note.setVisible(true);
			}
			else if ((minimizationTypeBox.getSelectedItem()) == EquivalenceRelation.ConflictEquivalence)
			{
				note.setText("Note:\n" + "This minimization algorithm is experimental! The\n" +
							 "result may not be minimal but should at least be\n" +
							 "conflict equivalent to the input.");
				note.setVisible(true);
			}
			else
			{
				note.setVisible(false);
			}

			// Not else if!
			alsoTransitions.setEnabled(true);
			ignoreMarking.setEnabled(true);
			if ((minimizationTypeBox.getSelectedItem()) == EquivalenceRelation.LanguageEquivalence)
			{
				// This already implies that the number of transitions are minimized
				alsoTransitions.setSelected(true);
				alsoTransitions.setEnabled(false);
			}
			else if ((minimizationTypeBox.getSelectedItem()) == EquivalenceRelation.ConflictEquivalence)
			{
				// When considering conflicts, the marking is essential!
				ignoreMarking.setSelected(false);
				ignoreMarking.setEnabled(false);
			}
		}
	}

	class MinimizationDialogAdvancedPanel
		extends MinimizationPanel
		implements ActionListener
	{
		private static final long serialVersionUID = 1L;

		private JCheckBox compositionalMinimization;
		private JComboBox minimizationStrategy;
		private Alphabet unionAlphabet;
		private Alphabet targetAlphabet;
		private JList targetAlphabetSelector;

		//private JTextArea note;

		public MinimizationDialogAdvancedPanel(Automata automata)
		{
			unionAlphabet = automata.getUnionAlphabet();
			targetAlphabet = null;

			compositionalMinimization = new JCheckBox("Compositional minimization (keep selected events)");
			compositionalMinimization.setToolTipText("Minimizes selected automata compositionally, keeps the events that have been selected from the list below");
			compositionalMinimization.addActionListener(this);

			minimizationStrategy = new JComboBox(MinimizationStrategy.toArray());
			Box strategyBox = Box.createHorizontalBox();
			strategyBox.add(new JLabel("     ")); // Ugly fix to get stuff centered
			strategyBox.add(new JLabel("Strategy: "));
			strategyBox.add(minimizationStrategy);
			strategyBox.add(new JLabel("     ")); // Ugly fix to get stuff centered

			/*
			  targetAlphabetSelector = new List(7, true);
			  for (EventIterator evIt = unionAlphabet.iterator(); evIt.hasNext(); )
			  {
			  LabeledEvent event = evIt.nextEvent();

			  // Only non-epsilon events!
			  if (!event.isEpsilon())
			  {
			  targetAlphabetSelector.add(event.getLabel());
			  }
			  }
			*/

			DefaultListModel list = new DefaultListModel();
			for (EventIterator evIt = unionAlphabet.iterator(); evIt.hasNext(); )
			{
				LabeledEvent event = evIt.nextEvent();

				// Only non-epsilon events!
				if (!event.isEpsilon())
				{
					list.addElement(event.getLabel());
				}
			}
			targetAlphabetSelector = new JList(list);
			targetAlphabetSelector.setVisibleRowCount(8);
			targetAlphabetSelector.setPrototypeCellValue("AT LEAST THIS WIDE");
			JScrollPane alphaPane = new JScrollPane(targetAlphabetSelector);

			//note = new JTextArea("Note:\n" + "I have nothing to say.");
			//note.setBackground(this.getBackground());

			// Add components
			this.add(compositionalMinimization);
			this.add(strategyBox);
			this.add(alphaPane);

			/*
			// Add components
			this.setLayout(new BorderLayout());

			JPanel choicePanel = new JPanel();
			choicePanel.setLayout(new FlowLayout());
			//choicePanel.add(advancedBox);
			choicePanel.add(compositionalMinimization);
			choicePanel.add(minimizationStrategy);
			this.add("North", choicePanel);

			JPanel alphaPanel = new JPanel();
			alphaPanel.setLayout(new FlowLayout());
			//alphaPanel.add(targetAlphabetSelector);
			alphaPanel.add(alphaPane);
			targetAlphabetSelector.setEnabled(false);
			this.add("South", alphaPanel);
			*/
		}

		public void update(MinimizationOptions options)
		{
			// compositionalMinimization.setSelected(options.getCompositionalMinimization());
			minimizationStrategy.setSelectedItem(options.getMinimizationStrategy());
			updatePanel();
		}

		/**
		 * Options that are not related to the current MinimizationOptions.
		 */
		public void updatePanel()
		{
			targetAlphabetSelector.setEnabled(compositionalMinimization.isSelected());
			minimizationStrategy.setEnabled(compositionalMinimization.isSelected());
			this.repaint();
		}

		public void regain(MinimizationOptions options)
		{
			// Update targetAlphabet
			if (compositionalMinimization.isSelected())
			{
				targetAlphabet = new Alphabet();
				//int[] selected = targetAlphabetSelector.getSelectedIndexes();
				int[] selected = targetAlphabetSelector.getSelectedIndices();
				//String[] selected = (String[]) targetAlphabetSelector.getSelectedValues();
				//String[] selected = targetAlphabetSelector.getModel();
				ListModel list = targetAlphabetSelector.getModel();
				for (int i=0; i<selected.length; i++)
				{
					//targetAlphabet.addEvent(unionAlphabet.getEvent(targetAlphabetSelector.getItem(selected[i])));
					//System.err.println("Seleceted: " + (String) selected[i]);
					//targetAlphabet.addEvent(unionAlphabet.getEvent(selected[i]));
					String label = (String) list.getElementAt(selected[i]);
					targetAlphabet.addEvent(unionAlphabet.getEvent(label));
				}
			}
			else
			{
				targetAlphabet = null;
			}

			options.setCompositionalMinimization(compositionalMinimization.isSelected());
			options.setMinimizationStrategy((MinimizationStrategy) minimizationStrategy.getSelectedItem());
			options.setTargetAlphabet(targetAlphabet);
		}

		public void actionPerformed(ActionEvent e)
		{
			updatePanel();

			/*
			  if (compositionalMinimization.isSelected())
			  {
			  targetAlphabetSelector.setEnabled(true);
			  //targetAlphabetSelector.setVisible(true);
			  //note.setText("Note:\n" + "This minimization algorithm is experimental!");
			  //note.setVisible(true);
			  }
			  else
			  {
			  targetAlphabetSelector.setEnabled(false);
			  //targetAlphabetSelector.setVisible(false);
			  //note.setVisible(false);
			  }
			*/
		}
	}
}
