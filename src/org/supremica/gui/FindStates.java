
// ** MF ************** FindStates.java *******************//
// Implements the FindStates dialog with regexps
package org.supremica.gui;

import java.util.HashSet;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.supremica.log.*;
import org.apache.oro.text.regex.*;
import org.supremica.automata.algorithms.*;
import org.supremica.util.*;
import org.supremica.automata.Automata;

// ----------------------------------------------------------------------------------
// compiler type should be adjustable, but as for now, we only support a single type
class CompilerFactory
{
	public static PatternCompiler getCompiler()
	{
		return new Perl5Compiler();
	}
}

// The table model manages the input of the regexp patterns
// It takes a compiler to be able to verify the correctness
// of the patterns on-line
class FindStatesTableModel
	extends AbstractTableModel
{
	private static Logger logger = LoggerFactory.createLogger(FindStatesTableModel.class);
	private Pattern[] patterns = null;
	private PatternCompiler comp = null;
	private String[] columnNames = { "Automaton", "Type", "Regular Expression" };
	private Object[][] cells = null;
	private final static int AUTOMATON_COL = 0;
	private final static int TYPE_COL = AUTOMATON_COL + 1;
	private final static int REGEXP_COL = TYPE_COL + 1;

	public FindStatesTableModel(Automata a, PatternCompiler c)
	{
		final int size = a.size();

		cells = new Object[size][REGEXP_COL + 1];

		for (int it = 0; it < size; ++it)
		{
			cells[it][AUTOMATON_COL] = a.getAutomatonAt(it).getName();
			cells[it][TYPE_COL] = a.getAutomatonAt(it).getType().toString();
			cells[it][REGEXP_COL] = ".*";
		}

		comp = c;
		patterns = new Pattern[size];

		try
		{

			// I know compile _cannot_ throw here, but Java requires me to catch this exception
			Pattern any_string = comp.compile(".*");

			for (int i = 0; i < patterns.length; ++i)
			{
				patterns[i] = any_string;
			}
		}
		catch (MalformedPatternException excp)
		{
			System.err.println("This should never happend");
		}
	}

	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	public Class getColumnClass(int col)
	{
		return cells[0][col].getClass();
	}

	public int getColumnCount()
	{
		return columnNames.length;
	}

	public int getRowCount()
	{
		return cells.length;
	}

	public Object getValueAt(int row, int col)
	{
		return cells[row][col];
	}

	public void setValueAt(Object obj, int row, int col)
	{
		cells[row][col] = obj;

		if (isRegexpColumn(col))
		{
			try
			{
				patterns[row] = comp.compile((String) obj);
			}
			catch (MalformedPatternException excp)
			{
				logger.debug("FindStatesTable::Incorrect pattern \"" + (String) obj + "\"");
				JOptionPane.showMessageDialog(null, "Incorrect pattern: " + (String) obj, "Incorrect pattern", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public boolean isCellEditable(int row, int col)
	{
		return col == REGEXP_COL;

		// the one and only editable column
	}

	public boolean isRegexpColumn(int col)
	{
		return col == REGEXP_COL;
	}

	public Pattern[] getRegexpPatterns()
	{
		return patterns;
	}
}

// -----------------------------------
class FindStatesTable
	extends JTable
{

	// local utility functions
	private TableSorter getTableSorterModel()
	{
		return (TableSorter) getModel();
	}

	private FindStatesTableModel getStatesTableModel()
	{
		return (FindStatesTableModel) getTableSorterModel().getModel();
	}

	private void doRepaint()
	{
		repaint();
	}

	private FindStatesTable getThisTable()
	{
		return this;
	}

	// ** Inner class, needs access to the model
	class RegexpPopupMenu
		extends JPopupMenu
	{
		int row;
		int col;

		public RegexpPopupMenu(int r, int c)
		{
			super("RegexpPopup");

			row = r;
			col = c;

			JMenuItem edit_item = add("Edit");

			edit_item.addActionListener(new ActionListener()
			{

				// anonymous class
				public void actionPerformed(ActionEvent e)
				{
					String str = (String) getModel().getValueAt(row, col);
					RegexpDialog regexp_dialog = new RegexpDialog(null, str);

					if (regexp_dialog.isOk())
					{
						getModel().setValueAt(regexp_dialog.getText(), row, col);
					}

					doRepaint();

					// for resolving ambiguity
				}
			});
		}
	}

	// Wrap the FindStatesTableModel inside a sort filter
	private static TableSorter makeTableModel(Automata a)
	{
		TableSorter sorter = new TableSorter();

		sorter.setModel(new FindStatesTableModel(a, CompilerFactory.getCompiler()));

		return sorter;
	}

	public FindStatesTable(Automata a)
	{

		// ** If TableSorter had a proper constructor we could have done
		// ** super(new TableSorter(new FindStatesTableModel(a)));
		super(makeTableModel(a));

		getTableSorterModel().addMouseListenerToHeaderInTable(this);

		// Note! This code is duplicated (almost) from Supremica.java
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{

				// This is needed for the Linux platform
				// where isPopupTrigger is true only on mousePressed.
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e)
			{

				// This is for triggering the popup on Windows platforms
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e)
			{
				int col = columnAtPoint(new Point(e.getX(), e.getY()));

				if (e.isPopupTrigger())
				{
					int row = rowAtPoint(new Point(e.getX(), e.getY()));

					if (row < 0)
					{
						return;
					}

					if (!isRowSelected(row))
					{
						clearSelection();
						setRowSelectionInterval(row, row);
					}

					//
					if (getStatesTableModel().isRegexpColumn(col))
					{
						RegexpPopupMenu regexp_popup = new RegexpPopupMenu(row, col);

						regexp_popup.show(e.getComponent(), e.getX(), e.getY());
					}
					else
					{

						// in table but not in the regexp column - show main menu
						// Supremica.menuHandler.getDisabledPopupMenu(getThisTable()).show(e.getComponent(), e.getX(), e.getY());
						// getMainPopupMenu().show(getThisTable().getSelectedRowCount(), e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		});
	}

	public Pattern[] getRegexpPatterns()
	{
		return getStatesTableModel().getRegexpPatterns();
	}
}


// ------------------------------------------

/*
 * abstract class FindStatesPanel
 *       extends JPanel
 * {
 *       public String title;
 *       public String tip;
 *
 *       FindStatesPanel(String title, String tip)
 *       {
 *               this.title = title;
 *               this.tip = tip;
 *       }
 *
 *       public abstract Matcher getMatcher();
 * }
 */
interface FindStatesTab
{
	public String getTitle();

	public String getTip();

	public Matcher getMatcher();
}


class FreeFormPanel
	extends JPanel    /* FindStatesPanel */
	implements FindStatesTab
{
	private String title = "Free Form";
	private String tip = "Search with a free form regexp";
	private JTextField reg_exp;
	private JTextField sep_str;
	private boolean ok = false;

	private void setOk()
	{
		ok = true;
	}

	private void doRepaint()
	{
		repaint();
	}

	private void replaceSelection(String s)
	{
		reg_exp.replaceSelection(s);
	}

	class RegexpMenuItem
		extends JMenuItem
		implements ActionListener
	{
		String pattern;

		public RegexpMenuItem(String s, String p)
		{
			super(s + " - " + p);

			pattern = p;

			addActionListener(this);
		}

		public void actionPerformed(ActionEvent event)
		{
			replaceSelection(pattern);
			doRepaint();
		}
	}

	class RegexpMenuBar
		extends JMenuBar
	{
		public RegexpMenuBar()
		{
			JMenu menu = new JMenu("Expressions");

			menu.add(new RegexpMenuItem("any string", ".*"));
			menu.add(new RegexpMenuItem("any uppercase", "[A-Z]"));
			menu.add(new RegexpMenuItem("any lowercase", "[a-z]"));
			menu.add(new RegexpMenuItem("any alphabetic", "[a-zA-Z]"));
			menu.add(new RegexpMenuItem("any digit", "[0-9]"));
			this.add(menu);

			JMenu help = new JMenu("Help");

			help.add(new JMenuItem("Help Topics"));
			help.add(new JSeparator());
			help.add(new JMenuItem("About..."));
			this.add(help);
		}
	}

	FreeFormPanel()
	{
		setLayout(new BorderLayout());
		add(new RegexpMenuBar(), BorderLayout.NORTH);

		JPanel p1 = new JPanel();

		p1.add(new JLabel("Regexp:"));
		p1.add(reg_exp = new JTextField(".*", 30));
		p1.add(new JLabel("State Separator: "));
		p1.add(sep_str = new JTextField(".", 20));
		add("Center", p1);

		/*
		 *               JPanel p2 = new JPanel();
		 *               p2.add(new JLabel("State Separator: "));
		 *               p2.add(sep_str = new JTextField(".", 20));
		 *               add("South", p2);
		 */
	}

	public String getTitle()
	{
		return title;
	}

	public String getTip()
	{
		return tip;
	}

	public Matcher getMatcher()
	{
		try
		{
			Pattern pattern = CompilerFactory.getCompiler().compile(reg_exp.getText());

			return new FreeformMatcher(new Perl5Matcher(), pattern, sep_str.getText());
		}
		catch (MalformedPatternException excp)
		{

			// debug("FindStatesTable::Incorrect pattern \"" + reg_exp.getText() +"\"");
			JOptionPane.showMessageDialog(null, "Incorrect pattern: " + reg_exp.getText(), "Incorrect pattern", JOptionPane.ERROR_MESSAGE);

			return null;
		}
	}

	public void setVisible(boolean aFlag)
	{
		super.setVisible(aFlag);

		if (aFlag)
		{
			reg_exp.requestFocus();
			reg_exp.selectAll();
		}
	}
}

class FixedFormPanel
	extends WhiteScrollPane
	implements FindStatesTab
{
	private final String title = "Fixed Form";
	private final String tip = "Search with state specific content";
	FindStatesTable table = null;

	FixedFormPanel(FindStatesTable t)
	{

		// this.table = table;
		// JScrollPane scroll_pane = new WhitePane(table);
		// scroll_pane.setPreferredSize(new Dimension(500, 300)); // does not help - still no scrolling
		// add(scroll_pane, "Center");
		super(t);

		this.table = t;
	}

	public String getTitle()
	{
		return title;
	}

	public String getTip()
	{
		return tip;
	}

	public Matcher getMatcher()
	{
		return new FixedformMatcher(new Perl5Matcher(), table.getRegexpPatterns());
	}
}

// -----------------------------------------
class FindStatesFrame
	extends JFrame
{
	private Gui gui;
	private FindStatesTable table = null;
	private Automata automata = null;
	private JTabbedPane tabbedPane = null;
	private JButton find_button = null;
	private CancelButton quit_button = null;
	private static Logger logger = LoggerFactory.createLogger(FindStatesFrame.class);

	private static void debug(String s)
	{
		logger.debug(s);
	}

	private Automata getAutomata()
	{
		return automata;
	}

	private Pattern[] getRegexpPatterns()
	{
		return table.getRegexpPatterns();
	}

	private JButton setDefaultButton(JButton b)
	{
		getRootPane().setDefaultButton(b);

		return b;
	}

	private void doRepaint()
	{
		repaint();
	}

	private FindStatesTab getSelectedComponent()
	{
		return (FindStatesTab) tabbedPane.getSelectedComponent();
	}

	private void showCompositeStates(SearchStates ss)
	{
		PresentStates present_states = new PresentStates(ss, getAutomata());

		present_states.execute();
	}

	class FindButton
		extends JButton
	{
		public FindButton()
		{
			super("Find");

			setToolTipText("Go ahead and find");
			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					action(e);
				}
			});
		}

		void action(ActionEvent e)
		{
			goAhead();
		}
	}

	class CancelButton
		extends JButton
	{
		public CancelButton()
		{
			super("Cancel");

			setToolTipText("Enough of finding states");
			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					action(e);
				}
			});
		}

		void action(ActionEvent e)
		{

			// debug("CancelButton disposing");
			dispose();
		}
	}

	private void goAhead()
	{

		// synchronize the automata but don't build the new automaton (throw away the edges)
		// just save the states (efficiently) and mark those that match as matching
		// ** TODO ** Disable the Find button, enable the Break button, start a new thread for searching
		// ** TODO ** If the Cancel or Break buttons are pressed, break the search thread
		try
		{

			// Pattern[] patterns = getRegexpPatterns();
			// ss.search(new Perl5Matcher(), patterns);
			Matcher matcher = ((FindStatesTab) getSelectedComponent()).getMatcher();

			if (matcher != null)
			{
				SearchStates ss = new SearchStates(getAutomata(), matcher);

				// /setCursor(WAIT_CURSOR);
				// find_button.setEnabled(false);
				ExecutionDialog exedlg = new ExecutionDialog(this, "Finding States...", ss);

				ss.setExecutionDialog(exedlg);
				ss.run();    // Start the synchronization thread
				// ss.join(); // at the moment, simply wait for ss to finish
				// find_button.setEnabled(true);
				// /setCursor(DEFAULT_CURSOR);
				showCompositeStates(ss);
			}

			// else do nothing
		}
		catch (Exception excp)
		{

			// Let it silently die, how the f*** do get these excp specs to work?
			debug("FindButton - " + excp);
			excp.printStackTrace();
		}
	}

	public FindStatesFrame(Gui gui)
	{

		// super(400, 300); // for CenteredFrame inheritance
		Utility.setupFrame(this, 500, 300);
		setTitle("Find States");

		this.gui = gui;
		this.automata = gui.getSelectedAutomata();
		this.table = new FindStatesTable(automata);

		FixedFormPanel fixedformPanel = new FixedFormPanel(table);
		FreeFormPanel freeformPanel = new FreeFormPanel();

		tabbedPane = new JTabbedPane();

		tabbedPane.addTab(fixedformPanel.getTitle(), null, fixedformPanel, fixedformPanel.getTip());
		tabbedPane.addTab(freeformPanel.getTitle(), null, freeformPanel, freeformPanel.getTip());

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(find_button = setDefaultButton(new FindButton()));
		buttonPanel.add(quit_button = new CancelButton());

		Container contentPane = getContentPane();

		contentPane.add(tabbedPane, "Center");
		contentPane.add(buttonPanel, "South");
	}
}

public class FindStates
{
	private JFrame frame = null;

	public FindStates(Gui gui)
	{
		frame = new FindStatesFrame(gui);
	}

	public void execute()
	{
		frame.show();
	}
}
