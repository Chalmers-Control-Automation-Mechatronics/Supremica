package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.TransferLine;
import org.supremica.util.SupremicaException;

class TransferLinePanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField int_cap1 = null;
	IntegerField int_cap2 = null;
	IntegerField int_cells = null;

	// <<<<<<< TestCasesDialog.java
	// IntegerField int_caps = null; // Gromyko, Pistore, Traverno allow
	// arbitrary size of all resources, inc machines
	//
	// =======
	//
	// >>>>>>> 1.47

	public TransferLinePanel() {
		JPanel panel = new JPanel(new GridLayout(4, 2));

		add(panel, BorderLayout.CENTER);
		panel.add(new JLabel("Ref: 'Notes on Control of Discrete",
				SwingConstants.RIGHT));
		panel.add(new JLabel("-Event Systems', W.M. Wonham",
				SwingConstants.LEFT));
		panel.add(new JLabel("Number of cells: "));
		panel.add(int_cells = new IntegerField("3", 5));
		panel.add(new JLabel("Buffer 1 capacity: "));
		panel.add(int_cap1 = new IntegerField("3", 5));
		panel.add(new JLabel("Buffer 2 capacity: "));
		panel.add(int_cap2 = new IntegerField("1", 5));
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		int cap1 = int_cap1.get();
		int cap2 = int_cap2.get();

		if ((cap1 < 1) || (cap2 < 1)) {
			throw new SupremicaException("Buffer capacity must be at least 1");
		}

		TransferLine tl = new TransferLine(int_cells.get(), cap1, cap2, false);

		return tl.getProject();
	}
}
