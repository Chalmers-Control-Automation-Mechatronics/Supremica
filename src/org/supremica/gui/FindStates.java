
// ** MF ************** FindStates.java *******************//
// Implements the FindStates dialog with regexps
package org.supremica.gui;

import java.util.HashSet;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.apache.log4j.*;
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
				LogDisplay.createCategory(FindStatesTableModel.class.getName()).debug("FindStatesTable::Incorrect pattern \"" + (String) obj + "\"");
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
abstract class FindStatesPanel
	extends JPanel
{
	public String title;
	public String tip;

	FindStatesPanel(String title, String tip)
	{
		this.title = title;
		this.tip = tip;
	}

	public abstract Matcher getMatcher();
}

class FreeFormPanel
	extends FindStatesPanel
{
	private JTextField reg_exp;
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
		super("Free Form", "Search with a free form regexp");

		setLayout(new BorderLayout());
		add(new RegexpMenuBar(), BorderLayout.NORTH);

		JPanel p1 = new JPanel();

		p1.add(new JLabel("Regexp:"));
		p1.add(reg_exp = new JTextField(new String(), 30));
		add("Center", p1);
	}

	public Matcher getMatcher()
	{
		while (true)
		{
			try
			{
				Pattern pattern = CompilerFactory.getCompiler().compile(reg_exp.getText());

				return new FreeformMatcher(new Perl5Matcher(), pattern);
			}
			catch (MalformedPatternException excp)
			{

				// debug("FindStatesTable::Incorrect pattern \"" + reg_exp.getText() +"\"");
				JOptionPane.showMessageDialog(null, "Incorrect pattern: " + reg_exp.getText(), "Incorrect pattern", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}

class FixedFormPanel
	extends FindStatesPanel
{
	FindStatesTable table = null;

	FixedFormPanel(FindStatesTable table)
	{
		super("Fixed Form", "Search with state specific content");

		this.table = table;

		add(new WhitePane(table), "Center");
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

	/*
	 *  CenteredFrame
	 */
	private FindStatesTable table = null;
	private Automata automata = null;

	// private FreeFormPanel freeformPanel = null;
	// private FixedFormPanel fixedformPanel = null;
	private JTabbedPane tabbedPane = null;
	private static Category thisCategory = LogDisplay.createCategory(FindStatesFrame.class.getName());

	private static void debug(String s)
	{
		thisCategory.debug(s);
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

	private FindStatesPanel getSelectedComponent()
	{
		return (FindStatesPanel) tabbedPane.getSelectedComponent();
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

			// synchronize the automata but don't build the new automaton (throw away the edges)
			// just save the states (efficiently) and mark those that match as matching
			try
			{
				SearchStates ss = new SearchStates(getAutomata());

				// Pattern[] patterns = getRegexpPatterns();
				// ss.search(new Perl5Matcher(), patterns);
				ss.search(((FindStatesPanel) getSelectedComponent()).getMatcher());
				showCompositeStates(ss);
			}
			catch (Exception excp)
			{

				// Let it silently die, how the f*** do get these excp specs to work?
				debug("FindButton - " + excp);
				excp.printStackTrace();
			}
		}
	}

	class QuitButton
		extends JButton
	{
		public QuitButton()
		{
			super("Close");

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

			// debug("QuitButton disposing");
			dispose();
		}
	}

	/**
	 *class FreeFormButton extends JButton
	 *{
	 *private PatternCompiler comp = null;
	 *private Pattern pattern = null;
	 *public FreeFormButton(PatternCompiler c)
	 *{
	 *super("FreeForm");
	 *setToolTipText("Search with a free form regexp");
	 *addActionListener(
	 *new ActionListener()
	 *{
	 *public void actionPerformed(ActionEvent e)
	 *{
	 *action(e);
	 *}
	 *});
	 *comp = c;
	 *try
	 *{
	 *pattern = comp.compile(".*");
	 *}
	 *catch(MalformedPatternException excp)
	 *{
	 *System.err.println("FreeFormButton::impossible exception");
	 *}
	 *}
	 *void action(ActionEvent evt)
	 *{
	 *RegexpDialog regexp_dlg = new RegexpDialog(null, pattern.getPattern());
	 *if(regexp_dlg.isOk())
	 *{
	 *try
	 *{
	 *pattern = comp.compile(regexp_dlg.getText());
	 *}
	 *catch(MalformedPatternException excp)
	 *{
	 *debug("FindStatesTable::Incorrect pattern \"" + regexp_dlg.getText() +"\"");
	 *JOptionPane.showMessageDialog(null, "Incorrect pattern: " + regexp_dlg.getText(), "Incorrect pattern", JOptionPane.ERROR_MESSAGE);
	 *return;
	 *}
	 *}
	 *else
	 *return;
	 *doRepaint();
	 *try
	 *{
	 *SearchStates ss = new SearchStates(getAutomata());
	 *ss.search(new Perl5Matcher(), pattern);
	 *showCompositeStates(ss);
	 *}
	 *catch(Exception excp)
	 *{
	 * // Let it silently die, how the f*** do get these excp specs to work?
	 *debug("FindButton - " + excp);
	 *excp.printStackTrace();
	 *}
	 *}
	 *}
	 *
	 * @param  a Description of the Parameter
	 */
	public FindStatesFrame(Automata a)
	{

		// super(400, 300); // for CenteredFrame inheritance
		Utility.setupFrame(this, 500, 300);
		setTitle("Find States");

		automata = a;
		table = new FindStatesTable(a);

		FixedFormPanel fixedformPanel = new FixedFormPanel(table);
		FreeFormPanel freeformPanel = new FreeFormPanel();

		tabbedPane = new JTabbedPane();

		tabbedPane.addTab(fixedformPanel.title, null, fixedformPanel, fixedformPanel.tip);
		tabbedPane.addTab(freeformPanel.title, null, freeformPanel, freeformPanel.tip);

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(setDefaultButton(new FindButton()));
		buttonPanel.add(new QuitButton());

		Container contentPane = getContentPane();

		contentPane.add(tabbedPane, "Center");
		contentPane.add(buttonPanel, "South");
	}
}

public class FindStates
{
	private JFrame frame = null;

	public FindStates(Automata a)
	{
		frame = new FindStatesFrame(a);
	}

	public void execute()
	{
		frame.show();
	}
}
