package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.SanchezTestCase;

class SanchezPanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField int_blocks = null;
	JComboBox choice = null;
	static final String[] choice_items = { "#1: Async prod", "#2: Synch prod",
			"#3: SupC" };

	public SanchezPanel() {
		JPanel panel = new JPanel(new GridLayout(3, 2));

		add(panel, BorderLayout.NORTH);
		panel.add(new JLabel("Ref: 'A Comparision of Synthesis",
				SwingConstants.RIGHT));
		panel.add(new JLabel(" Tools For...', A. Sanchez et. al.",
				SwingConstants.LEFT));
		panel.add(new JLabel("Number of blocks: "));
		panel.add(int_blocks = new IntegerField("5", 3));
		panel.add(new JLabel("Benchmark: "));
		panel.add(choice = new JComboBox(choice_items));
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		int p = int_blocks.get();
		int type = choice.getSelectedIndex();
		SanchezTestCase stc = new SanchezTestCase(p, type);

		return stc.getProject();
	}
}