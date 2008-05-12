package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.RoundRobin;

class RoundRobinPanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField num_proc = new IntegerField("4", 2);

	public RoundRobinPanel() {
		Box theBox = Box.createVerticalBox();

		add(theBox, BorderLayout.NORTH);

		JPanel labelPanel = new JPanel();

		labelPanel.add(new JLabel("Ref: 'Compositional Minimization of "
				+ "Finite State Systems', S. Graf et. al."));

		JPanel panel = new JPanel(new GridLayout(1, 2));

		panel.add(new JLabel("Number of processes: "));
		panel.add(num_proc);
		theBox.add(labelPanel);
		theBox.add(panel);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		RoundRobin rr = new RoundRobin(num_proc.get());

		return rr.getProject();
	}
}