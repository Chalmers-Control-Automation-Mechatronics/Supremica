package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.StickPickingGame;

class StickGamePanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField num_players = new IntegerField("2", 6);
	IntegerField num_sticks = new IntegerField("7", 6);

	StickGamePanel() {
		JPanel panel = new JPanel(new GridLayout(2, 2));

		add(panel, BorderLayout.WEST);
		panel.add(new JLabel("Number of players: "));
		panel.add(num_players);
		panel.add(new JLabel("Number of sticks: "));
		panel.add(num_sticks);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {

		// System.err.println("SticksGamePanel::doIt()");
		StickPickingGame spg = new StickPickingGame(num_players.get(),
				num_sticks.get());

		return spg.getProject();
	}
}
