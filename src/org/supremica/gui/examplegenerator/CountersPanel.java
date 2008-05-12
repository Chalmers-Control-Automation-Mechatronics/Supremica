package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.Counters;

class CountersPanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField int_num = null;
	IntegerField int_size = null;

	public CountersPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 2));

		add(panel, BorderLayout.CENTER);
		panel.add(new JLabel("Number of counters: "));
		panel.add(int_num = new IntegerField("3", 6));
		panel.add(new JLabel("Counter states: "));
		panel.add(int_size = new IntegerField("8", 6));
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		Counters counters = new Counters(int_num.get(), int_size.get());

		return counters.getProject();
	}
}