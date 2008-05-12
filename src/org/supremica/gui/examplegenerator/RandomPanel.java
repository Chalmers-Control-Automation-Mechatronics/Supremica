package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.RandomAutomata;

class RandomPanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField int_num = null;
	IntegerField int_size = null;
	IntegerField int_events = null;
	DoubleField dbl_dens = null;

	public RandomPanel() {
		JPanel panel = new JPanel(new GridLayout(4, 2));

		add(panel, BorderLayout.WEST);
		panel.add(new JLabel("Number of automata: "));
		panel.add(int_num = new IntegerField("3", 6));
		panel.add(new JLabel("Number of states: "));
		panel.add(int_size = new IntegerField("5", 6));
		panel.add(new JLabel("Number of events: "));
		panel.add(int_events = new IntegerField("3", 3));
		panel.add(new JLabel("Deterministic transition-density: "));
		panel.add(dbl_dens = new DoubleField("0.75", 6));
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		RandomAutomata ra = new RandomAutomata(int_num.get(), int_size.get(),
				int_events.get(), dbl_dens.get());

		return ra.getProject();
	}
}