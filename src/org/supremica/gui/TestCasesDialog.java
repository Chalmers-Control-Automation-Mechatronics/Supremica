
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
import org.supremica.testcases.StickPickingGame;
import org.supremica.testcases.AllocationBatch;

// should perform integer validation - see Horstmann
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
		super(new GridLayout(2, 1, 10, 10));

		JPanel cont = new JPanel();

		cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
		cont.add(req);
		cont.add(acc);
		cont.add(rel);

		JPanel num_users = new JPanel();

		num_users.add(new JLabel("Number of users: "));
		num_users.add(int_num = new IntegerField("3", 6));
		add(BorderLayout.NORTH, cont);
		add(BorderLayout.SOUTH, num_users);
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
		super(new GridLayout(2, 1, 10, 10));

		JPanel cont = new JPanel();

		cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
		cont.add(l_take);
		cont.add(r_take);
		cont.add(l_put);
		cont.add(r_put);

		JPanel num_users = new JPanel();

		num_users.add(new JLabel("Number of philosophers and forks: "));
		num_users.add(int_num);
		add(cont, BorderLayout.NORTH);
		add(num_users, BorderLayout.SOUTH);
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
		
		add(BorderLayout.NORTH, rows);
		add(BorderLayout.SOUTH, cols);
	}

	public Automata doIt()
		throws Exception
	{
		BricksGame bg = new BricksGame(num_rows.get(), num_cols.get());

		return bg.getAutomata();
	}
}

class StickGamePanel
	extends JPanel
	implements TestCase
{
	IntegerField num_players = new IntegerField("2", 6);
	IntegerField num_sticks = new IntegerField("5", 6);
	
	StickGamePanel()
	{
		JPanel players = new JPanel();
		players.add(new JLabel("Number of players: "));
		players.add(num_players);
		
		JPanel sticks = new JPanel();
		sticks.add(new JLabel("Number of sticks: "));
		sticks.add(num_sticks);
		
		add(players, BorderLayout.NORTH);
		add(sticks, BorderLayout.SOUTH);
		
	}
	public Automata doIt()
		throws Exception
	{
		// System.err.println("SticksGamePanel::doIt()");
		StickPickingGame spg = new StickPickingGame(num_players.get(), num_sticks.get());
		return spg.getAutomata();
	}
}

// ++ ARASH
class AllocationBatchPanel
	extends JPanel
	implements TestCase, ActionListener
{
	JTextField filename;
	JButton browse;

	AllocationBatchPanel()
	{
		super(new BorderLayout(10, 10));

		JPanel pCenter = new JPanel(new FlowLayout(FlowLayout.LEFT));

		pCenter.add(new JLabel("batch file:  "));
		pCenter.add(filename = new JTextField(20));
		pCenter.add(browse = new JButton("..."));
		browse.addActionListener(this);
		add(pCenter, BorderLayout.CENTER);
		add(new JLabel("Experimental serialized allocation batch"), BorderLayout.NORTH);
	}

	public Automata doIt()
		throws Exception
	{
		String file = filename.getText();

		if (file.length() > 0)
		{
			AllocationBatch ab = new AllocationBatch(file);

			return ab.getAutomata();
		}    // else...

		throw new Exception("you must choose a filename");
	}

	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();

		if (src == browse)
		{
			JFileChooser chooser = new JFileChooser();

			chooser.setDialogTitle("Please choose a batch file");

			int returnVal = chooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				filename.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}
}

// -- ARASH
class ExampleTab
	extends JTabbedPane
{
	ExampleTab()
	{
		addTab("Users", null, new UsersPanel(), "Mutual exclusion users");
		addTab("Philos", null, new PhilosPanel(), "Dininig Philosophers");
		addTab("Bricks", null, new BricksPanel(), "n-by-m bricks game");
		addTab("Game", null, new StickGamePanel(), "Stick picking game");
		addTab("Allocation Batch", null, new AllocationBatchPanel(), "Serialized Allocation Batch");
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
	System.err.println("Done it!");
		dispose();
	}

	Automata getAutomata()
	{
		return automata;
	}

	TestCasesDialog(JFrame frame)
	{
		super(frame, "Example Generator", true);    // modal dialog with frame as parent

		Container pane = getContentPane();

		pane.setLayout(new BorderLayout(10, 10));

		// Utility.setupFrame(this, 400, 200);
		// Dimension size = new Dimension(400, 200);
		// Point point = Utility.getPosForCenter(size);
		// setSize(size);
		// setLocation(point);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton tmp;

		buttons.add(tmp = new DoitButton());
		buttons.add(new CancelButton());
		buttons.add(new HelpButton());
		pane.add(extab, BorderLayout.CENTER);
		pane.add(buttons, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(tmp);    // :)
		pack();

		Point point = Utility.getPosForCenter(getSize());

		setLocation(point);
	}
}
