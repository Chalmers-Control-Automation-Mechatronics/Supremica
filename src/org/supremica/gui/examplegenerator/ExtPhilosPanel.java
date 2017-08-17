//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import org.supremica.testcases.ExtDiningPhilosophers;

class ExtPhilosPanel extends JPanel implements TestCase, ActionListener {
	private static final long serialVersionUID = 1L;
	IntegerField int_num = new IntegerField("5", 6);
	IntegerField int_interm_num = new IntegerField("2", 6);
	IntegerField int_step_phils = new IntegerField("0", 6);
	IntegerField int_step_intermStates = new IntegerField("0", 6);
	IntegerField int_numberOfInstances = new IntegerField("1", 6);
	JCheckBox i_l_take = new JCheckBox(
			"The uncontrollable events are 'philosopher i takes the left fork' for i even");
	JCheckBox l_take = new JCheckBox("take left fork", true);
	JCheckBox r_take = new JCheckBox("take right fork", true);
	JCheckBox l_put = new JCheckBox("put left fork", true);
	JCheckBox r_put = new JCheckBox("put right fork", true);
	JCheckBox animation = new JCheckBox("Include animation (5 philos)", false);
	JCheckBox memory = new JCheckBox("Forks have memory", false);
	JCheckBox multiple = new JCheckBox("Multiple instances", false);
	JCheckBox synth_algorithm = new JCheckBox(
			"Choose synthesis algorithm manually (deafult is BDD)", false);

	IntegerField int_N = new IntegerField("0", 6);
	IntegerField int_K = new IntegerField("0", 6);
	JCheckBox all_cases = new JCheckBox(
			"Compute all cases for n in interval (5,N) and k in interval (3,K)",
			false);

	JPanel traversing_algorithms;
	JRadioButton zigzagButton = new JRadioButton("Zigzag traversing");
	JRadioButton verticalButton = new JRadioButton("Vertical traversing");
	private static Logger logger = LoggerFactory
			.createLogger(ExtPhilosPanel.class);

	Util util = new Util();

	public ExtPhilosPanel() {
		// super(new GridLayout(2, 1, 10, 10));
		super();

		final JPanel cont = new JPanel();
		cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
		cont.setLayout(new GridLayout(2, 1));
		final JPanel ext_control = new JPanel();
		ext_control.add(i_l_take, BorderLayout.NORTH);

		i_l_take.setSelected(true);
		i_l_take.setEnabled(true);
		l_take.setEnabled(false);
		r_take.setEnabled(false);
		l_put.setEnabled(false);
		r_put.setEnabled(false);

		final JPanel normal_control = new JPanel();
		normal_control.add(l_take, BorderLayout.SOUTH);
		normal_control.add(r_take, BorderLayout.SOUTH);
		normal_control.add(l_put, BorderLayout.SOUTH);
		normal_control.add(r_put, BorderLayout.SOUTH);
		cont.add(ext_control);
		cont.add(normal_control);

		final JPanel num_users = new JPanel();
		num_users.add(new JLabel("Number of philosophers and forks: "),
				BorderLayout.NORTH);
		num_users.add(int_num, BorderLayout.NORTH);

		final JPanel num_intermStates = new JPanel();
		num_intermStates.add(new JLabel("Number of intermediate states: "),
				BorderLayout.NORTH);
		num_intermStates.add(int_interm_num, BorderLayout.NORTH);

		final JPanel animationPanel = new JPanel();
		animationPanel.add(animation);
		animationPanel.add(memory);
		animationPanel.add(multiple);
		animationPanel.add(synth_algorithm);

		multiple.addActionListener(this);
		i_l_take.addActionListener(this);

		final JPanel numberOfInstances = new JPanel();
		numberOfInstances.add(new JLabel("Number of instances: "),
				BorderLayout.NORTH);
		numberOfInstances.add(int_numberOfInstances, BorderLayout.SOUTH);
		int_numberOfInstances.setEnabled(false);

		final JPanel step_phils = new JPanel();
		step_phils
				.add(
						new JLabel(
								"step_P (increasement of number of phils for each instance): "),
						BorderLayout.NORTH);
		step_phils.add(int_step_phils, BorderLayout.SOUTH);
		int_step_phils.setEnabled(false);

		final JPanel step_intermStates = new JPanel();
		step_intermStates
				.add(
						new JLabel(
								"step_S (increasement of number of intermediate states for each instance): "),
						BorderLayout.NORTH);
		step_intermStates.add(int_step_intermStates, BorderLayout.SOUTH);
		int_step_intermStates.setEnabled(false);

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

		final Box theBox = Box.createVerticalBox();
		theBox.add(cont);
		theBox.add(num_users);
		theBox.add(num_intermStates);
		theBox.add(animationPanel);
		theBox.add(step_phils);
		theBox.add(step_intermStates);
		theBox.add(numberOfInstances);
		theBox.add(all_cases);
		theBox.add(NK);
		theBox.add(traversing_algorithms);
		add(theBox, BorderLayout.NORTH);
	}

	@Override
  public void actionPerformed(final ActionEvent e) {
		if (multiple.isSelected()) {
			int_step_phils.setEnabled(true);
			int_step_intermStates.setEnabled(true);
			int_numberOfInstances.setEnabled(true);
		} else {
			int_step_phils.setEnabled(false);
			int_step_intermStates.setEnabled(false);
			int_numberOfInstances.setEnabled(false);
		}

		if (i_l_take.isSelected()) {
			l_take.setEnabled(false);
			r_take.setEnabled(false);
			l_put.setEnabled(false);
			r_put.setEnabled(false);
		} else {
			l_take.setEnabled(true);
			r_take.setEnabled(true);
			l_put.setEnabled(true);
			r_put.setEnabled(true);
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

	@Override
  public Project generateAutomata() throws Exception {
		final ExtDiningPhilosophers dp = new ExtDiningPhilosophers(i_l_take
				.isSelected(), int_num.get(), int_interm_num.get(), l_take
				.isSelected(), r_take.isSelected(), l_put.isSelected(), r_put
				.isSelected(), animation.isSelected(), memory.isSelected());

		return dp.getProject();
	}

	@Override
  public void synthesizeSupervisor(final IDE ide) throws Exception {
		int number_of_phils = int_num.get();
		int number_of_interm_states = int_interm_num.get();
		final int number_of_instances = int_numberOfInstances.get();

		final TextArea result = new TextArea("");

		String result_text = "n \t k \t t \t m \t s \n \n";

		BufferedWriter[] out_back;

		final SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
		if (synth_algorithm.isSelected()) {
			final SynthesizerDialog synthesizerDialog = new SynthesizerDialog(ide
					.getFrame(), 2 * number_of_phils, synthesizerOptions);
			synthesizerDialog.setVisible(true);
		} else {
			synthesizerOptions
					.setSynthesisType(SynthesisType.NONBLOCKING_CONTROLLABLE);
			synthesizerOptions.setSynthesisAlgorithm(SynthesisAlgorithm.MONOLITHIC_WATERS);
			synthesizerOptions.setPurge(true);
			synthesizerOptions.setMaximallyPermissive(true);
			synthesizerOptions.setMaximallyPermissiveIncremental(true);
		}

		AutomataSynthesizer synthesizer;

		ExtDiningPhilosophers dp;
		AutomataSynthesisWorker asw;

		int finalNbrOfInstances;
		Point[] p = null;
		if (all_cases.isSelected()) {
			finalNbrOfInstances = (int_N.get() - int_num.get() + 1)
					* (int_K.get() - int_interm_num.get() + 1);
			p = new Point[finalNbrOfInstances];
			if (zigzagButton.isSelected())
				util.zigzagTraversing(p, 0, int_num.get(),
						int_interm_num.get(), 0, 1, int_num.get(),
						int_interm_num.get(), int_N.get(), int_K.get(), true);

			if (verticalButton.isSelected())
				util.verticalTraversing(p, 0, int_num.get(), int_interm_num
						.get(), int_num.get(), int_interm_num.get(), int_N
						.get(), int_K.get());
		} else
			finalNbrOfInstances = number_of_instances;

		out_back = new BufferedWriter[finalNbrOfInstances];

		for (int i = 0; i < finalNbrOfInstances; i++) {
			result_text += "\n";

			try {
				out_back[i] = new BufferedWriter(new FileWriter(
						"Results/DP/results_diningPhil" + i + ".txt"));
			} catch (final IOException e) {
				logger.error(e);
			}

			if (all_cases.isSelected()) {
				number_of_phils = p[i].x;
				number_of_interm_states = p[i].y;
			} else {
				number_of_phils = i * int_step_phils.get() + int_num.get();
				number_of_interm_states = i * int_step_intermStates.get()
						+ int_interm_num.get();
			}

			System.err.println("number of philosophers: " + number_of_phils);
			System.err.println("number of intermediate states: "
					+ number_of_interm_states);
			System.err.println("computing supervisor... ");

			result_text += "" + number_of_phils + "\t";

			result_text += " " + number_of_interm_states + "\t";

			dp = new ExtDiningPhilosophers(i_l_take.isSelected(),
					number_of_phils, number_of_interm_states, l_take
							.isSelected(), r_take.isSelected(), l_put
							.isSelected(), r_put.isSelected(), animation
							.isSelected(), memory.isSelected());

			if (synth_algorithm.isSelected()) {
				asw = new AutomataSynthesisWorker(null, dp.getAutomata(),
						synthesizerOptions);
				asw.join();

				result_text += " " + asw.getTimeSeconds() + "\t";
				result_text += " \t ";

				final Automaton supervisor = asw.getSupervisor();
				result_text += " " + supervisor.nbrOfStates();
			} else {
			  synthesizer =
			    new AutomataSynthesizer(dp.getAutomata(),
			                            SynchronizationOptions.getDefaultSynthesisOptions(),
			                            synthesizerOptions);
			  final Automaton supervisor =
			    synthesizer.execute().getFirstAutomaton();
			  final BigDecimal time = synthesizer.getTimeSeconds();
			  result_text += " " + time + "\t";
			  result_text += " \t";
			  final int nbrOfStates = supervisor.getStateSet().size();
			  result_text += " " + nbrOfStates;
			}

			System.err.println("Finished. ");
			System.err.println("--------------------------------");

			result_text += "\t";

			if (out_back[i] != null) {
				util.writeToFile(out_back[i], result_text, true);
				out_back[i].close();
			}

			System.gc();
		}
		result_text += "\n \n------------------------------------------------------------------ \n";
		result_text += "\n n: Number of philosophers \n k: Number of intermediate states for each philosopher \n t: Computation time in seconds \n m: The memory used in Mbytes \n s: Number of states for the supervisor \n d: Number of nodes for BDD";
		result.setText(result_text);
		result.setVisible(true);
	}
}
