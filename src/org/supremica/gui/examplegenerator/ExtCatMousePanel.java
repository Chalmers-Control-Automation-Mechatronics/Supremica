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

		JPanel stepsL = new JPanel();
		stepsL.add(new JLabel(
				"stepL (increasement of number levels for each instance): "),
				BorderLayout.SOUTH);
		stepsL.add(int_step_levels, BorderLayout.SOUTH);
		int_step_levels.setEnabled(false);

		all_cases.addActionListener(this);

		JPanel NK = new JPanel();
		NK.add(new JLabel("N: "), BorderLayout.SOUTH);
		NK.add(int_N, BorderLayout.SOUTH);
		NK.add(new JLabel("K: "), BorderLayout.SOUTH);
		NK.add(int_K, BorderLayout.SOUTH);
		int_N.setEnabled(false);
		int_K.setEnabled(false);

		zigzagButton.setSelected(false);
		verticalButton.setSelected(true);

		traversing_algorithms = new JPanel();

		Border border = BorderFactory
				.createTitledBorder("Traversing order of the test cases for the N*K plane");
		traversing_algorithms.setBorder(border);
		ButtonGroup group = new ButtonGroup();
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

	public void actionPerformed(ActionEvent e) {
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
	public void synthesizeSupervisor(IDE ide) throws Exception {
		int number_of_cats = int_num.get();
		int number_of_levels = int_num_levels.get();
		int number_of_instances = int_numberOfInstances.get();

		TextArea result = new TextArea("");

		String result_text = "n \t k \t t \t m \t s \t d \n";

		SynthesizerOptions synthesizerOptions = new SynthesizerOptions();

		if (chooseSynthesisAlgorithmManually.isSelected()) {
			// Manually select the synthesis algorithm
			SynthesizerDialog synthesizerDialog = new SynthesizerDialog(ide
					.getFrame(), 2 * number_of_cats + 5 * number_of_levels,
					synthesizerOptions);
			synthesizerDialog.show();
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
			} catch (IOException ex) {
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
			ExtCatMouse ecm = new ExtCatMouse(number_of_cats, number_of_levels);
			// INSTANCE GENERATION

			if (chooseSynthesisAlgorithmManually.isSelected()) {
				AutomataSynthesisWorker asw = new AutomataSynthesisWorker(null,
						ecm.getAutomata(), synthesizerOptions);
				asw.join();

				result_text += " " + asw.getTimeSeconds() + "\t";
				result_text += " \t ";

				Automaton supervisor = asw.getSupervisor();
				result_text += " " + supervisor.nbrOfStates();
			} else {
				AutomataSynthesizer synthesizer = new AutomataSynthesizer(ecm
						.getAutomata(), SynchronizationOptions
						.getDefaultSynthesisOptions(), synthesizerOptions);

				if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.BDD) {
					synthesizer.execute();

					BigDecimal time = synthesizer.getTimeSeconds();
					result_text += " " + time + "\t";
					result_text += " \t";

					long nbrOfStates = synthesizer.getNbrOfStatesBDD();
					result_text += " " + nbrOfStates + "\t";

					long nbrOfNodes = synthesizer.getNbrOfNodesBDD();
					result_text += " " + nbrOfNodes;
				} else {
					Automaton supervisor = synthesizer.execute()
							.getFirstAutomaton();

					BigDecimal time = synthesizer.getTimeSeconds();
					result_text += " " + time + "\t";
					result_text += " \t";

					int nbrOfStates = supervisor.getStateSet().size();
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

	public Project generateAutomata() throws Exception {
		ExtCatMouse cm = new ExtCatMouse(int_num.get(), int_num_levels.get());
		return cm.getProject();
	}
}