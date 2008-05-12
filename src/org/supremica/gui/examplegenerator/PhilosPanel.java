package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.DiningPhilosophers;

class PhilosPanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField int_num = new IntegerField("5", 6);
	JCheckBox l_take = new JCheckBox("take left fork", true);
	JCheckBox r_take = new JCheckBox("take right fork", true);
	JCheckBox l_put = new JCheckBox("put left fork", true);
	JCheckBox r_put = new JCheckBox("put right fork", true);
	JCheckBox animation = new JCheckBox("Include animation (5 philos)", false);
	JCheckBox memory = new JCheckBox("Forks have memory", false);
	JCheckBox multiple = new JCheckBox("Multiple instances", false);
	Util util = new Util();

	public PhilosPanel() {
		// super(new GridLayout(2, 1, 10, 10));
		super();

		JPanel cont = new JPanel();
		// cont.setLayout(new BoxLayout());
		cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
		cont.add(l_take);
		cont.add(r_take);
		cont.add(l_put);
		cont.add(r_put);

		JPanel num_users = new JPanel();
		num_users.add(new JLabel("Number of philosophers and forks: "),
				BorderLayout.NORTH);
		num_users.add(int_num, BorderLayout.NORTH);

		JPanel animationPanel = new JPanel();
		animationPanel.add(animation);
		animationPanel.add(memory);
		animationPanel.add(multiple);

		Box theBox = Box.createVerticalBox();
		theBox.add(cont);
		theBox.add(num_users);
		theBox.add(animationPanel);
		add(theBox, BorderLayout.NORTH);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		DiningPhilosophers dp = new DiningPhilosophers(int_num.get(), l_take
				.isSelected(), r_take.isSelected(), l_put.isSelected(), r_put
				.isSelected(), animation.isSelected(), memory.isSelected());

		Iterator<LabeledEvent> uit;

		/*
		 * for(int i=0;i<dp.getProject().nbrOfAutomata();i++) {
		 * System.out.println("i: "+i); uit =
		 * dp.getProject().getAutomatonAt(i).getAlphabet().getUncontrollableAlphabet().iterator();
		 * while(uit.hasNext()) System.out.println(""+uit.next()); }
		 */

		return dp.getProject();
	}
}
