
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

/**
 * @author fabian@supremica.org
 */

// Implements the FindStates dialog with regexps
package org.supremica.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.util.regex.*;

import org.supremica.log.*;
import org.supremica.automata.algorithms.*;
import org.supremica.util.*;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomataListener;
import org.supremica.gui.VisualProject;

// ----------------------------------------------------------------------------------
// compiler type should be adjustable, but as for now, we only support a single type
/*
class CompilerFactory
{
	public static PatternCompiler getCompiler()
	{
		return new Perl5Compiler();
	}
}
*/
// The table model manages the input of the regexp patterns
// It takes a compiler to be able to verify the correctness
// of the patterns on-line
class FindStatesTableModel
	extends AbstractTableModel
	implements AutomataListener // could usefully inherit from AutomataTableModel or something like that
{
	private static Logger logger = LoggerFactory.createLogger(FindStatesTableModel.class);
	// private Pattern[] patterns = null;
	// private PatternCompiler comp = null;
	private String[] columnNames = { "Automaton", "Type", "Regular Expression" };
	// private Object[][] cells = null;
	private Automata automata;
	private HashMap hashmap = new HashMap();

	public final static int AUTOMATON_COL = 0;
	public final static int TYPE_COL = AUTOMATON_COL + 1;
	public final static int REGEXP_COL = TYPE_COL + 1;

	public FindStatesTableModel(Automata a)
	{

		this.automata = a;
		//this.comp = c;
		// this.patterns = new Pattern[a.size()];

		automata.addListener(this);

		try
		{	// I know compile _cannot_ throw here, but Java requires me to catch this exception

			for(Iterator it = a.iterator(); it.hasNext(); )
			{
				hashmap.put(it.next(), Pattern.compile(".*"));
			}
		}
		catch (PatternSyntaxException excp)
		{
			System.err.println("This should never happen!");
		}
	}

	public String getColumnName(int col)
	{
		return columnNames[col];
	}
/*
	public Class getColumnClass(int col)
	{
		return cells[0][col].getClass();
	}
*/
	public int getColumnCount()
	{
		return columnNames.length;
	}

	public int getRowCount()
	{
		// return cells.length;
		return automata.getNbrOfAutomata();
	}

	public Object getValueAt(int row, int col)
	{
		Automaton automaton = automata.getAutomatonAt(row);
		switch(col)
		{
			case AUTOMATON_COL: return automaton.getName();
			case TYPE_COL: return automaton.getType();
			case REGEXP_COL: return ((Pattern)hashmap.get(automaton)).pattern();
		}
		return null;
		// return cells[row][col];
	}

	public void setValueAt(Object obj, int row, int col)
	{
		// cells[row][col] = obj;

		if (isRegexpColumn(col))
		{
			try
			{
				Automaton automaton = automata.getAutomatonAt(row);
				// patterns[row] = comp.compile((String) obj);
				hashmap.put(automaton, Pattern.compile((String) obj));
			}
			catch (PatternSyntaxException excp)
			{
				logger.debug("FindStatesTable::Incorrect pattern \"" + (String) obj + "\"");
				JOptionPane.showMessageDialog(null, "Incorrect pattern: " + (String) obj, "Incorrect pattern", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public boolean isCellEditable(int row, int col)
	{
		// the one and only editable column
		// return col == REGEXP_COL;
		// Dec'01 no (directly) editable column
		return false;
	}

	public boolean isRegexpColumn(int col)
	{
		return col == REGEXP_COL;
	}

	public Pattern[] getRegexpPatterns()
	{
		Pattern[] patterns = new Pattern[automata.size()];
		for(int i = 0; i < automata.size(); ++i)
		{
			patterns[i] = (Pattern)hashmap.get(automata.getAutomatonAt(i));
		}
		return patterns;
	}

	// implementation of AutomataListener interface
	private void updateListeners()
	{
		TableModelEvent event = new TableModelEvent(this, 0, automata.getNbrOfAutomata() - 1);
		fireTableChanged(event);
	}

	public void automatonAdded(Automata automata, Automaton automaton)
	{
		updateListeners();
	}

	public void automatonRemoved(Automata automata, Automaton automaton)
	{
		// need to remove its pattern
		hashmap.remove(automaton);
		updateListeners();
	}

	public void automatonRenamed(Automata automata, Automaton automaton)
	{
		updateListeners();
	}

	public void updated(Object theObject)
	{
		updateListeners();
	}
}

// -----------------------------------
class FindStatesTable
	extends JTable
{
	private static Logger logger = LoggerFactory.createLogger(FindStatesTable.class);

	private Automata automata;
	private JFrame frame;

	// local utility functions
	private TableSorter getTableSorterModel()
	{
		return (TableSorter) getModel();
	}
	private FindStatesTableModel getStatesTableModel()
	{
		return (FindStatesTableModel) getTableSorterModel().getModel();
	}
	private Automaton getAutomaton(int row)
	{
		String name = (String) getModel().getValueAt(row, FindStatesTableModel.AUTOMATON_COL);
		return automata.getAutomaton(name);
	}
	private void deleteAutomaton(int row)
	{
		automata.removeAutomaton(getAutomaton(row));

	}
	private void doRepaint()
	{
		repaint();
	}

	private FindStatesTable getThisTable()
	{
		return this;
	}

	// Inner class, needs access to the model
	class RegexpPopupMenu
		extends JPopupMenu
	{
		int row;

		public RegexpPopupMenu(int r)
		{
			super("RegexpPopup");

			this.row = r;

			JMenuItem edit_item = add("Edit");
			this.add(new JSeparator());
			JMenuItem delete_item = add("Delete");
			JMenuItem quit_item = add("Cancel");

			edit_item.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e) // anonymous class
				{
					String str = (String) getModel().getValueAt(row, FindStatesTableModel.REGEXP_COL);
					RegexpDialog regexp_dialog = new RegexpDialog(null, getAutomaton(row), str);

					if (regexp_dialog.isOk())
					{
						getModel().setValueAt(regexp_dialog.getText(), row, FindStatesTableModel.REGEXP_COL);
					}

					doRepaint();
				}
			});

			delete_item.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e) // anonymous class
				{
					Automaton automaton = getAutomaton(row);
					logger.debug("Removing " + automaton.getName());
					automata.removeAutomaton(automaton);
					doRepaint();
				}
			});

			quit_item.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e) // anonymous class
				{
					frame.dispose();
				}
			});

		}
	}

	// Wrap the FindStatesTableModel inside a sort filter
	private static TableSorter makeTableModel(Automata a)
	{
		TableSorter sorter = new TableSorter();

		sorter.setModel(new FindStatesTableModel(a));

		return sorter;
	}

	public FindStatesTable(Automata a, JFrame frame)
	{
		super(makeTableModel(a));

		this.automata = a;
		this.frame = frame;

		getTableSorterModel().addMouseListenerToHeaderInTable(this);
		getStatesTableModel().addTableModelListener(this);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// no use allowing multirow selection here (is there?)

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

					// Dec'01, only edit through the dialog and through click in any column
					//if (getStatesTableModel().isRegexpColumn(col))
					{
						RegexpPopupMenu regexp_popup = new RegexpPopupMenu(row);

						regexp_popup.show(e.getComponent(), e.getX(), e.getY());
					}
//					else
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

interface FindStatesTab
{
	public String getTitle();

	public String getTip();

	public StateMatcher getMatcher();
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
		p1.setLayout(new BorderLayout());

		Box yBox = new Box(BoxLayout.Y_AXIS);

		Box x1Box = new Box(BoxLayout.X_AXIS);
		x1Box.add(new JLabel("Regexp:"));
		reg_exp = new JTextField(".*", 30);
		x1Box.add(reg_exp);
		Box x2Box = new Box(BoxLayout.X_AXIS);
		x2Box.add(new JLabel("State Separator: "));
		sep_str = new JTextField(".", 30);
		x2Box.add(sep_str);
		yBox.add(Box.createVerticalGlue());
		yBox.add(x1Box);
		yBox.add(Box.createVerticalGlue());
		yBox.add(x2Box);
		yBox.add(Box.createVerticalGlue());
		p1.add(yBox, BorderLayout.NORTH);
		add("Center", p1);
	}

	public String getTitle()
	{
		return title;
	}

	public String getTip()
	{
		return tip;
	}

	public StateMatcher getMatcher()
	{
		try
		{
			Pattern pattern = Pattern.compile(reg_exp.getText());

			return new FreeformMatcher(pattern, sep_str.getText());
		}
		catch (PatternSyntaxException excp)
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

	public StateMatcher getMatcher()
	{
		return new FixedformMatcher(table.getRegexpPatterns());
	}
}

// -----------------------------------------
class FindStatesFrame
	extends JFrame
{
	private static Logger logger = LoggerFactory.createLogger(FindStatesFrame.class);
	private FindStatesTable table = null;
	private Automata automata = null;
	private JTabbedPane tabbedPane = null;
	private JButton find_button = null;
	private CancelButton quit_button = null;
	private VisualProject theVisualProject = null;

	private static void debug(String s)
	{
		logger.debug(s);
	}

	public FindStatesFrame(VisualProject theVisualProject, Automata selectedAutomata)
	{
		Utility.setupFrame(this, 500, 300);
		setTitle("Find States");

		this.theVisualProject = theVisualProject;
		this.automata = selectedAutomata;
		this.table = new FindStatesTable(automata, this);

		FixedFormPanel fixedformPanel = new FixedFormPanel(table);
		FreeFormPanel freeformPanel = new FreeFormPanel();

		tabbedPane = new JTabbedPane();

		tabbedPane.addTab(fixedformPanel.getTitle(), null, fixedformPanel, fixedformPanel.getTip());
		tabbedPane.addTab(freeformPanel.getTitle(), null, freeformPanel, freeformPanel.getTip());

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(find_button = Utility.setDefaultButton(this, new FindButton()));
		buttonPanel.add(quit_button = new CancelButton());

		Container contentPane = getContentPane();

		contentPane.add(tabbedPane, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
	}

	private Automata getAutomata()
	{
		return automata;
	}

	private Pattern[] getRegexpPatterns()
	{
		return table.getRegexpPatterns();
	}

	private void doRepaint()
	{
		repaint();
	}

	private FindStatesTab getSelectedComponent()
	{
		return (FindStatesTab) tabbedPane.getSelectedComponent();
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
		try
		{
			StateMatcher matcher = ((FindStatesTab) getSelectedComponent()).getMatcher();

			if (matcher != null)
			{

				SearchStates ss;
				try
				{
					ss = new SearchStates(getAutomata(), matcher);
				}
				catch (Exception e)
				{
					logger.error("Exception while constructing SearchState. Operation aborted. " + e.getMessage());
					return;
				}
				ss.start();    // Start the synchronization thread

				Monitor monitor = new Monitor("Finding states...", "", ss);
				monitor.startMonitor(this, 0, 1000);

				PresentStates present_states = new PresentStates(this, ss, getAutomata(), theVisualProject);
				present_states.start();

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

}

public class FindStates
{
	private JFrame frame = null;

	public FindStates(VisualProject theVisualProject, Automata selectedAutomata)
	{
		frame = new FindStatesFrame(theVisualProject, selectedAutomata);
	}

	public void execute()
	{
		frame.show();
	}
}
