package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.PigeonHole;
import org.supremica.util.SupremicaException;

class PigeonHolePanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField int_pigeons = null;
	IntegerField int_holes = null;

	public PigeonHolePanel() {
		Box theBox = Box.createVerticalBox();

		add(theBox, BorderLayout.NORTH);

		JPanel labelPanel = new JPanel();

		labelPanel.add(new JLabel(
				"Ref: 'The Intractability of Resolution', Armin Haken."));

		JPanel panel = new JPanel(new GridLayout(2, 2));

		panel.add(new JLabel("Number of pigeons: "));
		panel.add(int_pigeons = new IntegerField("5", 3));
		panel.add(new JLabel("Number of holes: "));
		panel.add(int_holes = new IntegerField("6", 3));
		theBox.add(labelPanel);
		theBox.add(panel);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		int p = int_pigeons.get();
		int h = int_holes.get();

		if ((p < 1) || (h < 1)) {
			throw new SupremicaException("Weird configuration...");
		}

		PigeonHole ph = new PigeonHole(p, h);

		return ph.getProject();
	}
}