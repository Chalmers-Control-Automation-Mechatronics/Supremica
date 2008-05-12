package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.AllocationBatch;
import org.supremica.util.SupremicaException;

class AllocationBatchPanel extends JPanel implements TestCase, ActionListener {
	private static final long serialVersionUID = 1L;
	JTextField filename;
	JButton browse;

	AllocationBatchPanel() {
		super(new BorderLayout(10, 10));

		JPanel pCenter = new JPanel(new GridLayout(4, 2));

		add(pCenter, BorderLayout.WEST);
		pCenter.add(new JLabel("batch file:  "));
		pCenter.add(filename = new JTextField(20));
		pCenter.add(browse = new JButton("..."));
		browse.addActionListener(this);
		add(pCenter, BorderLayout.CENTER);
		add(new JLabel("Experimental serialized allocation batch"),
				BorderLayout.NORTH);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		String file = filename.getText();

		if (file.length() > 0) {
			AllocationBatch ab = new AllocationBatch(file);

			return ab.getProject();
		} // else...

		throw new SupremicaException("you must choose a filename");
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (src == browse) {
			JFileChooser chooser = new JFileChooser();

			chooser.setDialogTitle("Please choose a batch file");

			int returnVal = chooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				filename.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}
}