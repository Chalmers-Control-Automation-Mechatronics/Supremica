
/********************** TestCasesDialog.java ************************/
package org.supremica.gui;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.supremica.automata.algorithms.AutomatonToDsx;
import org.supremica.automata.algorithms.AutomataToXml;
import org.supremica.automata.Automata;
import org.supremica.testcases.Users;
import org.supremica.testcases.BricksGame;
import org.supremica.testcases.DiningPhilosophers;

// performs integer validation - see Horstmann
class IntegerField
	extends JTextField
{
	public IntegerField(String init, int cols)
	{
		super(init, cols);
	}

	int get()
	{
		return Integer.parseInt(getText());
	}
}


interface TestCase
{
	public Automata doIt()
		throws Exception;
}


class UsersPanel
	extends JPanel
	implements TestCase
{
	IntegerField int_num = null;
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

		num_users.add(new JLabel("Number of users: "));
		num_users.add(int_num = new IntegerField("3", 6));
		add("North", cont);
		add("South", num_users);
	}

	public Automata doIt()
		throws Exception
	{
		Users users = new Users(int_num.get(), req.isSelected(), acc.isSelected(), rel.isSelected());

		return users.getAutomata();
	}
}

class PhilosPanel
	extends JPanel
	implements TestCase
{
	IntegerField int_num = new IntegerField("3", 6);
	JCheckBox l_take = new JCheckBox("take left fork", true);
	JCheckBox r_take = new JCheckBox("take right fork", true);
	JCheckBox l_put = new JCheckBox("put left fork", true);
	JCheckBox r_put = new JCheckBox("put right fork", true);

	public PhilosPanel()
	{
		JPanel cont = new JPanel();

		cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
		cont.add(l_take);
		cont.add(r_take);
		cont.add(l_put);
		cont.add(r_put);

		JPanel num_users = new JPanel();

		num_users.add(new JLabel("Number of philosophers and forks: "));
		num_users.add(int_num);
		add("North", cont);
		add("South", num_users);
	}

	public Automata doIt()
		throws Exception
	{
		DiningPhilosophers dp = new DiningPhilosophers(int_num.get(), l_take.isSelected(), r_take.isSelected(), l_put.isSelected(), r_put.isSelected());

		return dp.getAutomata();
	}
}

class BricksPanel
	extends JPanel
	implements TestCase
{
	IntegerField num_rows = new IntegerField("4", 6);
	IntegerField num_cols = new IntegerField("4", 6);

	BricksPanel()
	{
		JPanel rows = new JPanel();

		rows.add(new JLabel("Number of rows: "));
		rows.add(num_rows);

		JPanel cols = new JPanel();

		cols.add(new JLabel("Number of cols: "));
		cols.add(num_cols);
		add("North", rows);
		add("South", cols);
	}

	public Automata doIt()
		throws Exception
	{
		BricksGame bg = new BricksGame(num_rows.get(), num_cols.get());

		return bg.getAutomata();
	}
}

class ExampleTab
	extends JTabbedPane
{
	ExampleTab()
	{
		addTab("Users", null, new UsersPanel(), "Mutual exclusion users");
		addTab("Philos", null, new PhilosPanel(), "Dininig Philosophers");
		addTab("Bricks", null, new BricksPanel(), "n-by-m bricks game");
	}
}

public class TestCasesDialog
	extends JDialog
{
	private ExampleTab extab = new ExampleTab();
	private Automata automata = null;

	class DoitButton
		extends JButton
	{
		DoitButton()
		{
			super("Do it");

			setToolTipText("Go ahead and do it");
			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)

				// throws Exception // cannot do this - what the f**k!
				{
					try
					{
						doit();
					}
					catch (Exception excp)
					{

						// what are we supposed to do?
					}
				}
			});
		}
	}

	class CancelButton
		extends JButton
	{
		CancelButton()
		{
			super("Cancel");

			setToolTipText("Enough of this");
			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					action();
				}
			});
		}

		void action()
		{
			dispose();
		}
	}

	class HelpButton
		extends JButton
	{
		HelpButton()
		{
			super("Help");

			setToolTipText("Want some help?");
			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					action();
				}
			});
		}

		void action() {}
	}

	void doit()
		throws Exception
	{
		Component comp = extab.getSelectedComponent();

		// We know that this is actually also a TestCase (right?)
		TestCase tc = (TestCase) comp;

		automata = tc.doIt();    // Should return a Project (named)

		dispose();
	}

	Automata getAutomata()
	{
		return automata;
	}

	TestCasesDialog(JFrame frame)
	{
		super(frame, "Example Generator", true);    // modal dialog with frame as parent

		// Utility.setupFrame(this, 400, 200);
		Dimension size = new Dimension(400, 200);
		Point point = Utility.getPosForCenter(size);

		setSize(size);
		setLocation(point);

		JPanel buttons = new JPanel();

		buttons.add(new CancelButton());
		buttons.add(new DoitButton());
		buttons.add(new HelpButton());
		getContentPane().add(extab);
		getContentPane().add("South", buttons);
	}
}
