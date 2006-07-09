
/********************** TestCasesDialog.java ************************/
package org.supremica.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.supremica.automata.Project;
import org.supremica.log.*;
import org.supremica.testcases.Users;
import org.supremica.testcases.BricksGame;
import org.supremica.testcases.DiningPhilosophers;
import org.supremica.testcases.StickPickingGame;
import org.supremica.testcases.AllocationBatch;
import org.supremica.testcases.Counters;
import org.supremica.testcases.RandomAutomata;
import org.supremica.testcases.TransferLine;
import org.supremica.testcases.PigeonHole;
import org.supremica.testcases.SanchezTestCase;
import org.supremica.testcases.RoundRobin;
import org.supremica.testcases.Arbiter;
import org.supremica.testcases.warehouse.Warehouse;
import org.supremica.testcases.warehouse.SelectEventsWindow;
import org.supremica.util.SupremicaException;

// should perform integer validation - see Horstmann
class IntegerField
    extends JTextField
{
    private static final long serialVersionUID = 1L;

    public IntegerField(String init, int cols)
    {
		super(init, cols);
    }

    int get()
    {
		return Integer.parseInt(getText());
    }
}

class DoubleField
    extends JTextField
{
    private static final long serialVersionUID = 1L;

    public DoubleField(String init, int cols)
    {
		super(init, cols);
    }

    double get()
    {
		return Double.parseDouble(getText());
    }
}

interface TestCase
{
    Project doIt()
		throws Exception;
}

class UsersPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = null;
    IntegerField int_rsc = null;
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

		num_users.add(new JLabel("Number of resources: "));
		num_users.add(int_rsc = new IntegerField("1", 6));
		num_users.add(new JLabel("Number of users: "));
		num_users.add(int_num = new IntegerField("3", 6));
		add(BorderLayout.NORTH, cont);
		add(BorderLayout.SOUTH, num_users);
    }

    public Project doIt()
		throws Exception
    {
		Users users = new Users(int_num.get(), int_rsc.get(), req.isSelected(), acc.isSelected(), rel.isSelected());

		return users.getProject();
    }
}

class PhilosPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = new IntegerField("5", 6);
    JCheckBox l_take = new JCheckBox("take left fork", true);
    JCheckBox r_take = new JCheckBox("take right fork", true);
    JCheckBox l_put = new JCheckBox("put left fork", false);
    JCheckBox r_put = new JCheckBox("put right fork", false);
    JCheckBox animation = new JCheckBox("Include animation (5 philos)", false);
    JCheckBox memory = new JCheckBox("Forks have memory", false);

    public PhilosPanel()
    {
		// super(new GridLayout(2, 1, 10, 10));
		super();

		JPanel cont = new JPanel();
		//cont.setLayout(new BoxLayout());
		cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
		cont.add(l_take);
		cont.add(r_take);
		cont.add(l_put);
		cont.add(r_put);

		JPanel num_users = new JPanel();
		num_users.add(new JLabel("Number of philosophers and forks: "), BorderLayout.NORTH);
		num_users.add(int_num, BorderLayout.NORTH);

		JPanel animationPanel = new JPanel();
		animationPanel.add(animation);
		animationPanel.add(memory);

		Box theBox = Box.createVerticalBox();
		theBox.add(cont);
		theBox.add(num_users);
		theBox.add(animationPanel);
		add(theBox, BorderLayout.NORTH);
    }

    public Project doIt()
		throws Exception
    {
		DiningPhilosophers dp = new DiningPhilosophers(int_num.get(), l_take.isSelected(), r_take.isSelected(), l_put.isSelected(), r_put.isSelected(), animation.isSelected(), memory.isSelected());

		return dp.getProject();
    }
}

class BricksPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField num_rows = new IntegerField("4", 6);
    IntegerField num_cols = new IntegerField("4", 6);

    BricksPanel()
    {
		JPanel panel = new JPanel(new GridLayout(2, 2));

		add(panel, BorderLayout.WEST);
		panel.add(new JLabel("Number of rows: "));
		panel.add(num_rows);
		panel.add(new JLabel("Number of cols: "));
		panel.add(num_cols);
    }

    public Project doIt()
		throws Exception
    {
		BricksGame bg = new BricksGame(num_rows.get(), num_cols.get());

		return bg.getProject();
    }
}

class WarehousePanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    Warehouse warehouse = new Warehouse();
    IntegerField nbr_events_k = new IntegerField("3", 6);
    IntegerField nbr_events_m = new IntegerField("1", 6);
    SelectEventsWindow selectOperatorEventsWindow = null;
    SelectEventsWindow selectUnobservableEventsWindow = null;

    WarehousePanel()
    {
		JPanel panel = new JPanel(new GridLayout(3, 2));

		add(panel, BorderLayout.WEST);
		panel.add(new JLabel("Number of operator events (k): "));
		panel.add(nbr_events_k);
		panel.add(new JLabel("Number of supervisor events (m): "));
		panel.add(nbr_events_m);

		JButton selectOperatorEventsButton = new JButton("Select operator events");

		selectOperatorEventsButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (selectOperatorEventsWindow == null)
					{
						selectOperatorEventsWindow = new SelectEventsWindow(warehouse.getTruckAlphabet(), "Select operator events", "Select operator events", true);
					}

					selectOperatorEventsWindow.actionPerformed(e);

					// ActionMan.fileOpen(ActionMan.getGui());
				}
			});
		panel.add(selectOperatorEventsButton);

		//JButton selectControlEventsButton = new JButton("Select control events");
		//panel.add(selectControlEventsButton);
		JButton selectUnobservableEventsButton = new JButton("Select unobservable events");

		selectUnobservableEventsButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (selectUnobservableEventsWindow == null)
					{
						selectUnobservableEventsWindow = new SelectEventsWindow(warehouse.getTruckAlphabet(), "Select unobservable events", "Select unobservable events", false);
					}

					selectUnobservableEventsWindow.actionPerformed(e);

					// ActionMan.fileOpen(ActionMan.getGui());
				}
			});
		panel.add(selectUnobservableEventsButton);
    }

    public Project doIt()
		throws Exception
    {
		warehouse.setK(nbr_events_k.get());
		warehouse.setM(nbr_events_m.get());

		//System.err.println("Warehouse doIt");
		return warehouse.getProject();
    }
}

class StickGamePanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField num_players = new IntegerField("2", 6);
    IntegerField num_sticks = new IntegerField("7", 6);

    StickGamePanel()
    {
		JPanel panel = new JPanel(new GridLayout(2, 2));

		add(panel, BorderLayout.WEST);
		panel.add(new JLabel("Number of players: "));
		panel.add(num_players);
		panel.add(new JLabel("Number of sticks: "));
		panel.add(num_sticks);
    }

    public Project doIt()
		throws Exception
    {

		// System.err.println("SticksGamePanel::doIt()");
		StickPickingGame spg = new StickPickingGame(num_players.get(), num_sticks.get());

		return spg.getProject();
    }
}

class AllocationBatchPanel
    extends JPanel
    implements TestCase, ActionListener
{
    private static final long serialVersionUID = 1L;
    JTextField filename;
    JButton browse;

    AllocationBatchPanel()
    {
		super(new BorderLayout(10, 10));

		JPanel pCenter = new JPanel(new GridLayout(4, 2));

		add(pCenter, BorderLayout.WEST);
		pCenter.add(new JLabel("batch file:  "));
		pCenter.add(filename = new JTextField(20));
		pCenter.add(browse = new JButton("..."));
		browse.addActionListener(this);
		add(pCenter, BorderLayout.CENTER);
		add(new JLabel("Experimental serialized allocation batch"), BorderLayout.NORTH);
    }

    public Project doIt()
		throws Exception
    {
		String file = filename.getText();

		if (file.length() > 0)
	    {
			AllocationBatch ab = new AllocationBatch(file);

			return ab.getProject();
	    }    // else...

		throw new SupremicaException("you must choose a filename");
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

class CountersPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = null;
    IntegerField int_size = null;

    public CountersPanel()
    {
		JPanel panel = new JPanel(new GridLayout(2, 2));

		add(panel, BorderLayout.CENTER);
		panel.add(new JLabel("Number of counters: "));
		panel.add(int_num = new IntegerField("3", 6));
		panel.add(new JLabel("Counter states: "));
		panel.add(int_size = new IntegerField("8", 6));
    }

    public Project doIt()
		throws Exception
    {
		Counters counters = new Counters(int_num.get(), int_size.get());

		return counters.getProject();
    }
}

class RandomPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = null;
    IntegerField int_size = null;
    IntegerField int_events = null;
    DoubleField dbl_dens = null;

    public RandomPanel()
    {
		JPanel panel = new JPanel(new GridLayout(4, 2));

		add(panel, BorderLayout.WEST);
		panel.add(new JLabel("Number of automata: "));
		panel.add(int_num = new IntegerField("3", 6));
		panel.add(new JLabel("Number of states: "));
		panel.add(int_size = new IntegerField("8", 6));
		panel.add(new JLabel("Number of events: "));
		panel.add(int_events = new IntegerField("8", 3));
		panel.add(new JLabel("Deterministic transition-density: "));
		panel.add(dbl_dens = new DoubleField("0.3", 6));
    }

    public Project doIt()
		throws Exception
    {
		RandomAutomata ra = new RandomAutomata(int_num.get(), int_size.get(), int_events.get(), dbl_dens.get());

		return ra.getProject();
    }
}

class TransferLinePanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_cap1 = null;
    IntegerField int_cap2 = null;
    IntegerField int_cells = null;

    public TransferLinePanel()
    {
		JPanel panel = new JPanel(new GridLayout(4, 2));

		add(panel, BorderLayout.CENTER);
		panel.add(new JLabel("Ref: 'Notes on Control of Discrete", SwingConstants.RIGHT));
		panel.add(new JLabel("-Event Systems', W.M. Wonham", SwingConstants.LEFT));
		panel.add(new JLabel("Number of cells: "));
		panel.add(int_cells = new IntegerField("3", 5));
		panel.add(new JLabel("Buffer 1 capacity: "));
		panel.add(int_cap1 = new IntegerField("3", 5));
		panel.add(new JLabel("Buffer 2 capacity: "));
		panel.add(int_cap2 = new IntegerField("1", 5));
    }

    public Project doIt()
		throws Exception
    {
		int cap1 = int_cap1.get();
		int cap2 = int_cap2.get();

		if ((cap1 < 1) || (cap2 < 1))
	    {
			throw new SupremicaException("Buffer capacity must be at least 1");
	    }

		TransferLine tl = new TransferLine(int_cells.get(), cap1, cap2, false);

		return tl.getProject();
    }
}

class PigeonHolePanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_pigeons = null;
    IntegerField int_holes = null;

    public PigeonHolePanel()
    {
		Box theBox = Box.createVerticalBox();

		add(theBox, BorderLayout.NORTH);

		JPanel labelPanel = new JPanel();

		labelPanel.add(new JLabel("Ref: 'The Intractability of Resolution', Armin Haken."));

		JPanel panel = new JPanel(new GridLayout(2, 2));

		panel.add(new JLabel("Number of pigeons: "));
		panel.add(int_pigeons = new IntegerField("5", 3));
		panel.add(new JLabel("Number of holes: "));
		panel.add(int_holes = new IntegerField("6", 3));
		theBox.add(labelPanel);
		theBox.add(panel);
    }

    public Project doIt()
		throws Exception
    {
		int p = int_pigeons.get();
		int h = int_holes.get();

		if ((p < 1) || (h < 1))
	    {
			throw new SupremicaException("Weird configuration...");
	    }

		PigeonHole ph = new PigeonHole(p, h);

		return ph.getProject();
    }
}

class SanchezPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_blocks = null;
    JComboBox choice = null;
    static final String[] choice_items = { "#1: Async prod", "#2: Synch prod",
										   "#3: SupC" };

    public SanchezPanel()
    {
		JPanel panel = new JPanel(new GridLayout(3, 2));

		add(panel, BorderLayout.NORTH);
		panel.add(new JLabel("Ref: 'A Comparision of Synthesis", SwingConstants.RIGHT));
		panel.add(new JLabel(" Tools For...', A. Sanchez et. al.", SwingConstants.LEFT));
		panel.add(new JLabel("Number of blocks: "));
		panel.add(int_blocks = new IntegerField("5", 3));
		panel.add(new JLabel("Benchmark: "));
		panel.add(choice = new JComboBox(choice_items));
    }

    public Project doIt()
		throws Exception
    {
		int p = int_blocks.get();
		int type = choice.getSelectedIndex();
		SanchezTestCase stc = new SanchezTestCase(p, type);

		return stc.getProject();
    }
}

class RoundRobinPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField num_proc = new IntegerField("4", 2);

    public RoundRobinPanel()
    {
		Box theBox = Box.createVerticalBox();

		add(theBox, BorderLayout.NORTH);

		JPanel labelPanel = new JPanel();

		labelPanel.add(new JLabel("Ref: 'Compositional Minimization of " + "Finite State Systems', S. Graf et. al."));

		JPanel panel = new JPanel(new GridLayout(1, 2));

		panel.add(new JLabel("Number of processes: "));
		panel.add(num_proc);
		theBox.add(labelPanel);
		theBox.add(panel);
    }

    public Project doIt()
		throws Exception
    {
		RoundRobin rr = new RoundRobin(num_proc.get());

		return rr.getProject();
    }
}

class ArbiterPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField num_users = new IntegerField("4", 2);
    JCheckBox synchronize = new JCheckBox("Synchronize arbiter cells (yields an appealing structure)", true);

    public ArbiterPanel()
    {
		Box theBox = Box.createVerticalBox();
		add(theBox, BorderLayout.NORTH);

		JPanel labelPanel = new JPanel();
		labelPanel.add(new JLabel("Ref: 'Compositional Model Checking', E.M. Clarke et. al."));

		JPanel panel = new JPanel(new GridLayout(1, 2));
		panel.add(new JLabel("Number of users: "));
		panel.add(num_users);

		JPanel synchronizePanel = new JPanel();
		synchronizePanel.add(synchronize, BorderLayout.NORTH);
		theBox.add(labelPanel);
		theBox.add(panel);
		theBox.add(synchronizePanel);
    }

    public Project doIt()
		throws Exception
    {
		// At least two users!!
		if (num_users.get() < 2)
	    {
			throw new SupremicaException("The arbiter tree must have at least two users.");
	    }

		//Arbiter arb = new Arbiter(users, synchronize.isSelected());
		Arbiter arb = new Arbiter(num_users.get(), synchronize.isSelected());

		return arb.getProject();
    }
}

class ExampleTab
    extends JTabbedPane
{
    private static final long serialVersionUID = 1L;

    ExampleTab()
    {
		addTab("Users", null, new UsersPanel(), "Mutual exclusion users");
		addTab("Philos", null, new PhilosPanel(), "Dininig philosophers");
		addTab("Bricks", null, new BricksPanel(), "n-by-m bricks game");
		addTab("Sticks game", null, new StickGamePanel(), "Stick picking game");
		addTab("Transfer line", null, new TransferLinePanel(), "Transfer line");
		addTab("Counters", null, new CountersPanel(), "Independent Counters");
		addTab("Random automata", null, new RandomPanel(), "Random automata");
		addTab("Pigeon-Hole", null, new PigeonHolePanel(), "Pigeon-Hole");
		addTab("Sanchez-BM", null, new SanchezPanel(), "Sanchez-BM");
		addTab("Warehouse", null, new WarehousePanel(), "Warehouse");
		addTab("Round robin", null, new RoundRobinPanel(), "Round robin access");
		addTab("Arbiter", null, new ArbiterPanel(), "Arbiter tree");

		//addTab("Allocation Batch", null, new AllocationBatchPanel(), "Serialized Allocation Batch");
    }
}

public class TestCasesDialog
    extends JDialog
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.createLogger(TestCasesDialog.class);
    private ExampleTab extab = new ExampleTab();
    private Project project = null;
    private Gui gui;

    class DoitButton
		extends JButton
    {
		private static final long serialVersionUID = 1L;

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
						catch (Exception ex)
						{
							logger.error("Exception while constructing test case: " + ex);
							logger.debug(ex.getStackTrace());

							// what are we supposed to do?
						}
					}
				});
		}
    }

    class CancelButton
		extends JButton
    {
		private static final long serialVersionUID = 1L;

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
		private static final long serialVersionUID = 1L;

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

		setVisible(false);

		project = tc.doIt();    // Should return a Project (named)

		gui.addProject(project);
		dispose();
    }

    /*
      Project getProject()
      {
      return project;
      }
    */
    TestCasesDialog(Frame frame, Gui gui)
    {
		super(frame, "Example Generator", false);    // modal dialog with frame as parent

		this.gui = gui;

		Container pane = getContentPane();

		pane.setLayout(new BorderLayout(10, 10));

		// Utility.setupFrame(this, 400, 200);
		// Dimension size = new Dimension(400, 200);
		// Point point = Utility.getPosForCenter(size);
		// setSize(size);
		// setLocation(point);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
