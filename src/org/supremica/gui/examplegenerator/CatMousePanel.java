package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.testcases.CatMouse;

class CatMousePanel extends JPanel implements TestCase, ActionListener {
	private static final long serialVersionUID = 1L;
	IntegerField int_num = new IntegerField("1", 6);
	IntegerField int_step_cats = new IntegerField("0", 6);
	IntegerField int_numberOfInstances = new IntegerField("1", 6);
	JCheckBox multiple = new JCheckBox("Multiple instances", false);
	JPanel num_users;
	JPanel steps;
	Box theBox;
	JPanel numberOfInstances;
	Util util = new Util();
	private static Logger logger = LoggerFactory
			.createLogger(CatMousePanel.class);

	public CatMousePanel() {
		// super(new GridLayout(2, 1, 10, 10));
		super();

		num_users = new JPanel();
		num_users.add(new JLabel("Number of cats (or mice): "),
				BorderLayout.NORTH);
		num_users.add(int_num, BorderLayout.NORTH);

		JPanel multiplePanel = new JPanel();
		multiplePanel.add(multiple);

		multiple.addActionListener(this);

		steps = new JPanel();
		steps
				.add(
						new JLabel(
								"step (increasement of number of cats (or mice) for each instance): "),
						BorderLayout.NORTH);
		steps.add(int_step_cats, BorderLayout.SOUTH);
		int_step_cats.setEnabled(false);

		numberOfInstances = new JPanel();
		numberOfInstances.add(new JLabel("Number of instances: "),
				BorderLayout.NORTH);
		numberOfInstances.add(int_numberOfInstances, BorderLayout.SOUTH);
		int_numberOfInstances.setEnabled(false);

		theBox = Box.createVerticalBox();
		theBox.add(num_users);
		theBox.add(multiplePanel);
		theBox.add(steps);
		theBox.add(numberOfInstances);
		add(theBox, BorderLayout.NORTH);
	}

	public void synthesizeSupervisor(IDE ide) throws Exception {
		// TODO: implement this one
		logger.warn("Not implemented");
	}

	public void actionPerformed(ActionEvent e) {
		if (multiple.isSelected()) {
			int_step_cats.setEnabled(true);
			int_numberOfInstances.setEnabled(true);
		} else {
			int_step_cats.setEnabled(false);
			int_numberOfInstances.setEnabled(false);
		}
	}

	public Project generateAutomata() throws Exception {
		CatMouse cm = new CatMouse(int_num.get());

		return cm.getProject();
	}
}
