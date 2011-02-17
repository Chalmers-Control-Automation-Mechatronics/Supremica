package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import javax.swing.Box;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.Users;

class UsersPanel extends JPanel implements TestCase 
{
	private static final long serialVersionUID = 1L;
	IntegerField int_num = null;
	IntegerField int_rsc = null;
	JCheckBox req = new JCheckBox("request (a)");
	JCheckBox acc = new JCheckBox("access  (b)", true);
	JCheckBox rel = new JCheckBox("release (c)");

	public UsersPanel()
        {
		JPanel cont = new JPanel();

		cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
		cont.add(req);
		cont.add(acc);
		cont.add(rel);

		JPanel num_users = new JPanel();
		num_users.add(new JLabel("Number of resources: "));
		num_users.add(int_rsc = new IntegerField("1", 6));
		num_users.add(new JLabel("Number of users: "));
		num_users.add(int_num = new IntegerField("3", 6));

                Box theBox = Box.createVerticalBox();
		theBox.add(cont);
		theBox.add(num_users);
		add(theBox, BorderLayout.NORTH);

	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		Users users = new Users(int_num.get(), int_rsc.get(), req.isSelected(),
				acc.isSelected(), rel.isSelected());

		return users.getProject();
	}
}