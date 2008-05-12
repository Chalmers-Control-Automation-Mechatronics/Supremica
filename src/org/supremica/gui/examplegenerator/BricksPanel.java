package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.BricksGame;

class BricksPanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField num_rows = new IntegerField("4", 6);
	IntegerField num_cols = new IntegerField("4", 6);

	BricksPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 2));

		add(panel, BorderLayout.WEST);
		panel.add(new JLabel("Number of rows: "));
		panel.add(num_rows);
		panel.add(new JLabel("Number of cols: "));
		panel.add(num_cols);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		BricksGame bg = new BricksGame(num_rows.get(), num_cols.get());

		return bg.getProject();
	}
}
