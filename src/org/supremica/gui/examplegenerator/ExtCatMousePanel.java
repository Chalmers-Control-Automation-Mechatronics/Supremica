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

package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.algorithms.AutomataSynthesizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.gui.AutomataSynthesisWorker;
import org.supremica.gui.SynthesizerDialog;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.testcases.ExtCatMouse;

class ExtCatMousePanel extends CatMousePanel implements TestCase,
		ActionListener {
	private static final long serialVersionUID = 1L;
	IntegerField int_num_levels = new IntegerField("1", 6);
	IntegerField int_step_levels = new IntegerField("0", 6);
	IntegerField int_N = new IntegerField("0", 6);
	IntegerField int_K = new IntegerField("0", 6);
	JCheckBox chooseSynthesisAlgorithmManually = new JCheckBox(
			"Choose synthesis algorithm manually (deafult is BDD)", false);
	JCheckBox all_cases = new JCheckBox(
			"Compute all cases for n in interval (1,N) and k in interval (1,K)",
			false);

	JPanel traversing_algorithms;
	JRadioButton zigzagButton = new JRadioButton("Zigzag traversing");
	JRadioButton verticalButton = new JRadioButton("Vertical traversing");
	private static Logger logger = LoggerFactory
			.createLogger(ExtCatMousePanel.class);

	public ExtCatMousePanel() {
		// super(new GridLayout(2, 1, 10, 10));
		super();
		num_users
				.add(new JLabel("     Number of levels: "), BorderLayout.NORTH);
		num_users.add(int_num_levels, BorderLayout.NORTH);

		final JPanel stepsL = new JPanel();
		stepsL.add(new JLabel(
				"stepL (increasement of number levels for each instance): "),
				BorderLayout.SOUTH);
		stepsL.add(int_step_levels, BorderLayout.SOUTH);
		int_step_levels.setEnabled(false);

		all_cases.addActionListener(this);

		final JPanel NK = new JPanel();
		NK.add(new JLabel("N: "), BorderLayout.SOUTH);
		NK.add(int_N, BorderLayout.SOUTH);
		NK.add(new JLabel("K: "), BorderLayout.SOUTH);
		NK.add(int_K, BorderLayout.SOUTH);
		int_N.setEnabled(false);
		int_K.setEnabled(false);

		zigzagButton.setSelected(false);
		verticalButton.setSelected(true);

		traversing_algorithms = new JPanel();

		final Border border = BorderFactory
				.createTitledBorder("Traversing order of the test cases for the N*K plane");
		traversing_algorithms.setBorder(border);
		final ButtonGroup group = new ButtonGroup();
		group.add(zigzagButton);
		group.add(verticalButton);

		traversing_algorithms.add(zigzagButton);
		traversing_algorithms.add(verticalButton);

		zigzagButton.setEnabled(false);
		verticalButton.setEnabled(false);

		theBox.add(stepsL);
		theBox.add(numberOfInstances);
		theBox.add(chooseSynthesisAlgorithmManually);
		theBox.add(all_cases);
		theBox.add(traversing_algorithms);
		theBox.add(NK);

	}

	@Override
  public void actionPerformed(final ActionEvent e) {
		if (multiple.isSelected()) {
			int_step_cats.setEnabled(true);
			int_step_levels.setEnabled(true);
			int_numberOfInstances.setEnabled(true);
		} else {
			int_step_cats.setEnabled(false);
			int_step_levels.setEnabled(false);
			int_numberOfInstances.setEnabled(false);
		}

		if (all_cases.isSelected()) {
			int_N.setEnabled(true);
			int_K.setEnabled(true);
			zigzagButton.setEnabled(true);
			verticalButton.setEnabled(true);
		} else {
			int_N.setEnabled(false);
			int_K.setEnabled(false);
			zigzagButton.setEnabled(false);
			verticalButton.setEnabled(false);
		}
	}

	// This function will be called when "Synthesize" button is pressed
	@Override
  public void synthesizeSupervisor(final IDE ide) throws Exception {
		int number_of_cats = int_num.get();
		int number_of_levels = int_num_levels.get();
		final int number_of_instances = int_numberOfInstances.get();

		final TextArea result = new TextArea("");

		String result_text = "n \t k \t t \t m \t s \t d \n";

		final SynthesizerOptions synthesizerOptions = new SynthesizerOptions();

		if (chooseSynthesisAlgorithmManually.isSelected()) {
			// Manually select the synthesis algorithm
			final SynthesizerDialog synthesizerDialog = new SynthesizerDialog(ide
					.getFrame(), 2 * number_of_cats + 5 * number_of_levels,
					synthesizerOptions);
			synthesizerDialog.setVisible(true);
		} else {
			// Default is BDD
			synthesizerOptions
					.setSynthesisType(SynthesisType.NONBLOCKINGCONTROLLABLE);
			synthesizerOptions.setSynthesisAlgorithm(SynthesisAlgorithm.BDD);
			synthesizerOptions.setPurge(true);
			synthesizerOptions.setMaximallyPermissive(true);
			synthesizerOptions.setMaximallyPermissiveIncremental(true);
		}

		int finalNbrOfInstances;
		Point[] p = null;

		if (all_cases.isSelected()) {
			// All instances for a N*K plane
			finalNbrOfInstances = int_N.get() * int_K.get();
			p = new Point[finalNbrOfInstances];
			if (zigzagButton.isSelected())
				util.zigzagTraversing(p, 0, int_num.get(),
						int_num_levels.get(), 0, 1, int_num.get(),
						int_num_levels.get(), int_N.get(), int_K.get(), true);
			// util.zigzagTraversing(p,0,int_num.get(),int_num_levels.get(),-1,1,1,1,int_N.get(),int_K.get(),true);

			if (verticalButton.isSelected())
				util.verticalTraversing(p, 0, int_num.get(), int_num_levels
						.get(), int_num.get(), int_num_levels.get(), int_N
						.get(), int_K.get());
		} else {
			finalNbrOfInstances = number_of_instances;
		}

		for (int i = 0; i < finalNbrOfInstances; i++) {
			result_text += "\n";
			BufferedWriter out_back = null;
			try {
				out_back = new BufferedWriter(new FileWriter(
						"Results/CM/results_catmouse" + i + ".txt"));
			} catch (final IOException ex) {
				logger.error(ex);
			}

			if (all_cases.isSelected()) {
				number_of_cats = p[i].y;
				number_of_levels = p[i].x;
			} else {
				number_of_cats = i * int_step_cats.get() + int_num.get();
				number_of_levels = i * int_step_levels.get()
						+ int_num_levels.get();
			}

			System.err.println("number of cats: " + number_of_cats);
			System.err.println("number of levels: " + number_of_levels);
			System.err.println("computing supervisor... ");

			result_text += "" + number_of_cats + "\t";

			result_text += " " + number_of_levels + "\t";

			// INSTANCE GENERATION
			final ExtCatMouse ecm = new ExtCatMouse(number_of_cats, number_of_levels);
			// INSTANCE GENERATION

			if (chooseSynthesisAlgorithmManually.isSelected()) {
				final AutomataSynthesisWorker asw = new AutomataSynthesisWorker(null,
						ecm.getAutomata(), synthesizerOptions);
				asw.join();

				result_text += " " + asw.getTimeSeconds() + "\t";
				result_text += " \t ";

				final Automaton supervisor = asw.getSupervisor();
				result_text += " " + supervisor.nbrOfStates();
			} else {
				final AutomataSynthesizer synthesizer = new AutomataSynthesizer(ecm
						.getAutomata(), SynchronizationOptions
						.getDefaultSynthesisOptions(), synthesizerOptions);

				if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.BDD) {
					synthesizer.execute();

					final BigDecimal time = synthesizer.getTimeSeconds();
					result_text += " " + time + "\t";
					result_text += " \t";

					final long nbrOfStates = synthesizer.getNbrOfStatesBDD();
					result_text += " " + nbrOfStates + "\t";

					final long nbrOfNodes = synthesizer.getNbrOfNodesBDD();
					result_text += " " + nbrOfNodes;
				} else {
					final Automaton supervisor = synthesizer.execute()
							.getFirstAutomaton();

					final BigDecimal time = synthesizer.getTimeSeconds();
					result_text += " " + time + "\t";
					result_text += " \t";

					final int nbrOfStates = supervisor.getStateSet().size();
					result_text += " " + nbrOfStates;
				}
			}

			System.err.println("Finished. ");
			System.err.println("--------------------------------");

			result_text += "\t";

			if (out_back != null) {
				util.writeToFile(out_back, result_text, true);
				out_back.close();
			}
		}

		result_text += "\n \n------------------------------------------------------------------ \n";
		result_text += "\n n: Number of levels \n k: Number of cats and mice \n t: Computation time in seconds \n m: The memory used in Mbytes \n s: Number of states for the supervisor \n d: Number of nodes for BDD";
		result.setText(result_text);
		result.setVisible(true);
	}

	@Override
  public Project generateAutomata() throws Exception {
		final ExtCatMouse cm = new ExtCatMouse(int_num.get(), int_num_levels.get());
		return cm.getProject();
	}
}
