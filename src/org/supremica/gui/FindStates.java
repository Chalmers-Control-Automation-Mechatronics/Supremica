
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.supremica.automata.Automata;
import org.supremica.automata.AutomataListener;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.FixedformMatcher;
import org.supremica.automata.algorithms.Forbidder;
import org.supremica.automata.algorithms.FreeformMatcher;
import org.supremica.automata.algorithms.SearchStates;
import org.supremica.automata.algorithms.StateMatcher;
import org.supremica.automata.algorithms.StateMatcherOptions;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;

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
	implements AutomataListener    // could usefully inherit from AutomataTableModel or something like that
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(FindStatesTableModel.class);

	// private Pattern[] patterns = null;
	// private PatternCompiler comp = null;
	private final String[] columnNames = { "Automaton", "Type",
									 "Regular Expression", "Accepting",
									 "Forbidden", "Deadlock" };

	// private Object[][] cells = null;
	private final Automata automata;
	private final HashMap<Automaton, Pattern> patternMap = new HashMap<Automaton, Pattern>();
	private final HashMap<Automaton, StateMatcherOptions> stateMatcherOptionsMap = new HashMap<Automaton, StateMatcherOptions>();
	public final static int AUTOMATON_COL = 0;
	public final static int TYPE_COL = AUTOMATON_COL + 1;
	public final static int REGEXP_COL = TYPE_COL + 1;
	public final static int ACCEPTING_COL = REGEXP_COL + 1;
	public final static int FORBIDDEN_COL = ACCEPTING_COL + 1;
	public final static int DEADLOCK_COL = FORBIDDEN_COL + 1;

	public FindStatesTableModel(final Automata a)
	{
		this.automata = a;

		//this.comp = c;
		// this.patterns = new Pattern[a.size()];
		automata.addListener(this);

		try
		{    // I know compile _cannot_ throw here, but Java requires me to catch this exception
			for (final Iterator<?> it = a.iterator(); it.hasNext(); )
			{
				final Automaton currAutomaton = (Automaton) it.next();

				patternMap.put(currAutomaton, Pattern.compile(".*"));
				stateMatcherOptionsMap.put(currAutomaton, new StateMatcherOptions());
			}
		}
		catch (final PatternSyntaxException ex)
		{
			logger.error("FindStatesTableModel: This should never happen!", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	@Override
  public String getColumnName(final int col)
	{
		return columnNames[col];
	}

/*
		public Class getColumnClass(int col)
		{
				return cells[0][col].getClass();
		}
*/
	@Override
  public int getColumnCount()
	{
		return columnNames.length;
	}

	@Override
  public int getRowCount()
	{
		// return cells.length;
		return automata.nbrOfAutomata();
	}

	@Override
  public Object getValueAt(final int row, final int col)
	{
		final Automaton automaton = automata.getAutomatonAt(row);

		if (col == AUTOMATON_COL)
		{
			return automaton.getName();
		}

		if (col == TYPE_COL)
		{
			return automaton.getType();
		}

		if (col == REGEXP_COL)
		{
			return patternMap.get(automaton).pattern();
		}

		if (col == ACCEPTING_COL)
		{
			final StateMatcherOptions currOptions = stateMatcherOptionsMap.get(automaton);

			return currOptions.getAcceptingCondition();
		}

		if (col == FORBIDDEN_COL)
		{
			final StateMatcherOptions currOptions = stateMatcherOptionsMap.get(automaton);

			return currOptions.getForbiddenCondition();
		}

		if (col == DEADLOCK_COL)
		{
			final StateMatcherOptions currOptions = stateMatcherOptionsMap.get(automaton);

			return currOptions.getDeadlockCondition();
		}

		return null;

		// return cells[row][col];
	}

	@Override
  public void setValueAt(final Object obj, final int row, final int col)
	{

		// cells[row][col] = obj;
		//logger.error("row: " + row + " col: " + col);
		if (row < 0)
		{
			return;
		}

		if (col < 0)
		{
			return;
		}

		if (isRegexpColumn(col))
		{
			try
			{
				final Automaton automaton = automata.getAutomatonAt(row);

				// patterns[row] = comp.compile((String) obj);
				patternMap.put(automaton, Pattern.compile((String) obj));
			}
			catch (final PatternSyntaxException excp)
			{
				JOptionPane.showMessageDialog(null, "Incorrect pattern: " + (String) obj, "Incorrect pattern", JOptionPane.ERROR_MESSAGE);
				logger.debug("FindStatesTable::Incorrect pattern \"" + (String) obj + "\"");
				logger.debug(excp.getStackTrace());
			}
		}
		else if (isAcceptingColumn(col))
		{
			final Automaton automaton = automata.getAutomatonAt(row);
			final StateMatcherOptions currOptions = stateMatcherOptionsMap.get(automaton);

			if (currOptions != null)
			{
				currOptions.setAcceptingCondition((StateMatcherOptions.Accepting) obj);
			}
		}
		else if (isForbiddenColumn(col))
		{
			final Automaton automaton = automata.getAutomatonAt(row);
			final StateMatcherOptions currOptions = stateMatcherOptionsMap.get(automaton);

			if (currOptions != null)
			{
				currOptions.setForbiddenCondition((StateMatcherOptions.Forbidden) obj);
			}
		}
		else if (isDeadlockColumn(col))
		{
			final Automaton automaton = automata.getAutomatonAt(row);
			final StateMatcherOptions currOptions = stateMatcherOptionsMap.get(automaton);

			if (currOptions != null)
			{
				currOptions.setDeadlockCondition((StateMatcherOptions.Deadlock) obj);
			}
		}
	}

	@Override
  public boolean isCellEditable(final int row, final int col)
	{
		if (isAcceptingColumn(col))
		{
			return true;
		}

		if (isForbiddenColumn(col))
		{
			return true;
		}

		if (isDeadlockColumn(col))
		{
			return true;
		}

		return false;
	}

	public boolean isRegexpColumn(final int col)
	{
		return col == REGEXP_COL;
	}

	public boolean isAcceptingColumn(final int col)
	{
		return col == ACCEPTING_COL;
	}

	public boolean isForbiddenColumn(final int col)
	{
		return col == FORBIDDEN_COL;
	}

	public boolean isDeadlockColumn(final int col)
	{
		return col == DEADLOCK_COL;
	}

	public int getRegexpColumn()
	{
		return REGEXP_COL;
	}

	public int getAcceptingColumn()
	{
		return ACCEPTING_COL;
	}

	public int getForbiddenColumn()
	{
		return FORBIDDEN_COL;
	}

	public int getDeadlockColumn()
	{
		return DEADLOCK_COL;
	}

	public Pattern[] getRegexpPatterns()
	{
		final Pattern[] patterns = new Pattern[automata.size()];

		for (int i = 0; i < automata.size(); ++i)
		{
			patterns[i] = patternMap.get(automata.getAutomatonAt(i));
		}

		return patterns;
	}

	public StateMatcherOptions[] getStateMatcherOptions()
	{
		final StateMatcherOptions[] options = new StateMatcherOptions[automata.size()];

		for (int i = 0; i < automata.size(); ++i)
		{
			options[i] = stateMatcherOptionsMap.get(automata.getAutomatonAt(i));
		}

		return options;
	}

	// implementation of AutomataListener interface
	private void updateListeners()
	{
		final TableModelEvent event = new TableModelEvent(this, 0, automata.nbrOfAutomata() - 1);

		fireTableChanged(event);
	}

	@Override
  public void automatonAdded(final Automata automata, final Automaton automaton)
	{
		updateListeners();
	}

	@Override
  public void automatonRemoved(final Automata automata, final Automaton automaton)
	{

		// need to remove its pattern
		patternMap.remove(automaton);
		stateMatcherOptionsMap.remove(automaton);
		updateListeners();
	}

	@Override
  public void automatonRenamed(final Automata automata, final Automaton automaton)
	{
		updateListeners();
	}

	@Override
  public void actionsOrControlsChanged(final Automata automata)
	{    // Do nothing
	}

	@Override
  public void updated(final Object theObject)
	{
		updateListeners();
	}
}

// -----------------------------------
class FindStatesTable
	extends JTable
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(FindStatesTable.class);
	private Automata automata;
	private FindStatesFrame frame;
	@SuppressWarnings("unused")
	private StateMatcherAcceptingCellEditor acceptingEditor;
	@SuppressWarnings("unused")
	private StateMatcherForbiddenCellEditor forbiddenEditor;
	@SuppressWarnings("unused")
	private StateMatcherDeadlockCellEditor deadlockEditor;

	// local utility functions
	private TableSorter getTableSorterModel()
	{
		return (TableSorter) getModel();
	}

	private FindStatesTableModel getStatesTableModel()
	{
		return (FindStatesTableModel) getTableSorterModel().getModel();
	}

	private Automaton getAutomaton(final int row)
	{
		final String name = (String) getModel().getValueAt(row, FindStatesTableModel.AUTOMATON_COL);

		return automata.getAutomaton(name);
	}

	@SuppressWarnings("unused")
	private void deleteAutomaton(final int row)
	{
		automata.removeAutomaton(getAutomaton(row));
		frame.updateForbidButton(automata.isAllAutomataPlants());
	}

	private void doRepaint()
	{
		frame.updateForbidButton(automata.isAllAutomataPlants());
		repaint();
	}

	@SuppressWarnings("unused")
	private FindStatesTable getThisTable()
	{
		return this;
	}

	// Inner class, needs access to the model
	class RegexpPopupMenu
		extends JPopupMenu
	{
		private static final long serialVersionUID = 1L;
		int row;

		public RegexpPopupMenu(final int r)
		{
			super("RegexpPopup");

			this.row = r;

			final JMenuItem edit_item = add("Edit");

			this.add(new JSeparator());

			final JMenuItem delete_item = add("Delete");
			final JMenuItem quit_item = add("Cancel");

			edit_item.addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)    // anonymous class
				{
					final String str = (String) getModel().getValueAt(row, FindStatesTableModel.REGEXP_COL);
					final RegexpDialog regexp_dialog = new RegexpDialog(null, getAutomaton(row), str);

					if (regexp_dialog.isOk())
					{
						getModel().setValueAt(regexp_dialog.getText(), row, FindStatesTableModel.REGEXP_COL);
					}

					doRepaint();
				}
			});
			delete_item.addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)    // anonymous class
				{
					final Automaton automaton = getAutomaton(row);

					logger.debug("Removing " + automaton.getName());
					automata.removeAutomaton(automaton);
					doRepaint();
				}
			});
			quit_item.addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)    // anonymous class
				{
					frame.dispose();
				}
			});
		}
	}

	class StateMatcherAcceptingCellEditor
		implements CellEditorListener
	{
		private final JComboBox<Object> stateMatcherTypeCombo;
		private final FindStatesTableModel theTableModel;

		StateMatcherAcceptingCellEditor()
		{
			stateMatcherTypeCombo = new JComboBox<Object>();

			final Iterator<?> typeIt = StateMatcherOptions.Accepting.iterator();

			while (typeIt.hasNext())
			{
				stateMatcherTypeCombo.addItem(typeIt.next());
			}

			theTableModel = getStatesTableModel();

			final TableColumnModel columnModel = getColumnModel();
			final TableColumn typeColumn = columnModel.getColumn(theTableModel.getAcceptingColumn());
			final DefaultCellEditor cellEditor = new DefaultCellEditor(stateMatcherTypeCombo);

			cellEditor.setClickCountToStart(2);
			typeColumn.setCellEditor(cellEditor);
			cellEditor.addCellEditorListener(this);
		}

		@Override
    public void editingCanceled(final ChangeEvent e) {}

		@Override
    public void editingStopped(final ChangeEvent e)
		{

			// logger.info("editing stopped: " + getSelectedRow());
			if (stateMatcherTypeCombo.getSelectedIndex() >= 0)
			{
				final StateMatcherOptions.Accepting selectedValue = (StateMatcherOptions.Accepting) stateMatcherTypeCombo.getSelectedItem();

				if (selectedValue != null)
				{
					final int selectedRow = getSelectedRow();

					getModel().setValueAt(selectedValue, selectedRow, theTableModel.getAcceptingColumn());
				}
			}
		}
	}

	class StateMatcherForbiddenCellEditor
		implements CellEditorListener
	{
		private final JComboBox<Object>stateMatcherTypeCombo;
		private final FindStatesTableModel theTableModel;

		StateMatcherForbiddenCellEditor()
		{
			stateMatcherTypeCombo = new JComboBox<Object>();

			final Iterator<?> typeIt = StateMatcherOptions.Forbidden.iterator();

			while (typeIt.hasNext())
			{
				stateMatcherTypeCombo.addItem(typeIt.next());
			}

			theTableModel = getStatesTableModel();

			final TableColumnModel columnModel = getColumnModel();
			final TableColumn typeColumn = columnModel.getColumn(theTableModel.getForbiddenColumn());
			final DefaultCellEditor cellEditor = new DefaultCellEditor(stateMatcherTypeCombo);

			cellEditor.setClickCountToStart(2);
			typeColumn.setCellEditor(cellEditor);
			cellEditor.addCellEditorListener(this);
		}

		@Override
    public void editingCanceled(final ChangeEvent e) {}

		@Override
    public void editingStopped(final ChangeEvent e)
		{

			//logger.info("editing stopped: " + getSelectedRow());
			if (stateMatcherTypeCombo.getSelectedIndex() >= 0)
			{
				final StateMatcherOptions.Forbidden selectedValue = (StateMatcherOptions.Forbidden) stateMatcherTypeCombo.getSelectedItem();

				if (selectedValue != null)
				{
					final int selectedRow = getSelectedRow();

					getModel().setValueAt(selectedValue, selectedRow, theTableModel.getForbiddenColumn());
				}
			}
		}
	}

	class StateMatcherDeadlockCellEditor
		implements CellEditorListener
	{
		private final JComboBox<Object> stateMatcherTypeCombo;
		private final FindStatesTableModel theTableModel;

		StateMatcherDeadlockCellEditor()
		{
			stateMatcherTypeCombo = new JComboBox<Object>();

			final Iterator<?> typeIt = StateMatcherOptions.Deadlock.iterator();

			while (typeIt.hasNext())
			{
				stateMatcherTypeCombo.addItem(typeIt.next());
			}

			theTableModel = getStatesTableModel();

			final TableColumnModel columnModel = getColumnModel();
			final TableColumn typeColumn = columnModel.getColumn(theTableModel.getDeadlockColumn());
			final DefaultCellEditor cellEditor = new DefaultCellEditor(stateMatcherTypeCombo);

			cellEditor.setClickCountToStart(2);
			typeColumn.setCellEditor(cellEditor);
			cellEditor.addCellEditorListener(this);
		}

		@Override
    public void editingCanceled(final ChangeEvent e) {}

		@Override
    public void editingStopped(final ChangeEvent e)
		{
			if (stateMatcherTypeCombo.getSelectedIndex() >= 0)
			{
				final StateMatcherOptions.Deadlock selectedValue = (StateMatcherOptions.Deadlock) stateMatcherTypeCombo.getSelectedItem();

				if (selectedValue != null)
				{
					final int selectedRow = getSelectedRow();

					if (selectedRow >= 0)
					{
						getModel().setValueAt(selectedValue, selectedRow, theTableModel.getDeadlockColumn());
					}
				}
			}
		}
	}

	// Wrap the FindStatesTableModel inside a sort filter
	private static TableSorter makeTableModel(final Automata a)
	{
		final TableSorter sorter = new TableSorter();

		sorter.setModel(new FindStatesTableModel(a));

		return sorter;
	}

	public FindStatesTable(final Automata a, final FindStatesFrame frame)
	{
		super(makeTableModel(a));

		this.automata = a;
		this.frame = frame;

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);    // no use allowing multirow selection here (is there?)

		// Note! This code is duplicated (almost) from Supremica.java
		addMouseListener(new MouseAdapter()
		{
			@Override
      public void mousePressed(final MouseEvent e)
			{

				// This is needed for the Linux platform
				// where isPopupTrigger is true only on mousePressed.
				maybeShowPopup(e);
			}

			@Override
      public void mouseReleased(final MouseEvent e)
			{

				// This is for triggering the popup on Windows platforms
				maybeShowPopup(e);
			}

			@Override
      public void mouseClicked(final MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					final int col = columnAtPoint(e.getPoint());
					final int row = rowAtPoint(e.getPoint());

					if (row < 0)
					{
						return;
					}

					if (col == FindStatesTableModel.REGEXP_COL)
					{
						final String str = (String) getModel().getValueAt(row, FindStatesTableModel.REGEXP_COL);
						final RegexpDialog regexp_dialog = new RegexpDialog(null, getAutomaton(row), str);

						if (regexp_dialog.isOk())
						{
							getModel().setValueAt(regexp_dialog.getText(), row, FindStatesTableModel.REGEXP_COL);
						}

						doRepaint();
					}
				}
			}

			private void maybeShowPopup(final MouseEvent e)
			{

				//logger.info("maybeShowpopup");
				final int row = rowAtPoint(e.getPoint());
				//logger.info("row " + row + " col " + col);
				if (row < 0)
				{
					return;
				}

				if (!isRowSelected(row))
				{

					//logger.info("changing selection");
					clearSelection();
					setRowSelectionInterval(row, row);
				}

				if (e.isPopupTrigger())
				{
					final RegexpPopupMenu regexp_popup = new RegexpPopupMenu(row);

					regexp_popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		getTableSorterModel().addMouseListenerToHeaderInTable(this);
		getStatesTableModel().addTableModelListener(this);

		acceptingEditor = new StateMatcherAcceptingCellEditor();
		forbiddenEditor = new StateMatcherForbiddenCellEditor();
		deadlockEditor = new StateMatcherDeadlockCellEditor();

		getTableHeader().setReorderingAllowed(false);
	}

	public Pattern[] getRegexpPatterns()
	{
		return getStatesTableModel().getRegexpPatterns();
	}

	public StateMatcherOptions[] getStateMatcherOptions()
	{
		return getStatesTableModel().getStateMatcherOptions();
	}
}


// ------------------------------------------
interface FindStatesTab
{
	String getTitle();

	String getTip();

	StateMatcher getMatcher();
}


class FreeFormPanel
	extends JPanel    /* FindStatesPanel */
	implements FindStatesTab
{
	private static final long serialVersionUID = 1L;
	private final String title = "Free Form";
	private final String tip = "Search with a free form regexp";
	private final JTextField reg_exp;
	private final JTextField sep_str;
	@SuppressWarnings("unused")
    private JCheckBox pairwise_equal;
	@SuppressWarnings("unused")
	private boolean ok = false;

	@SuppressWarnings("unused")
	private void setOk()
	{
		ok = true;
	}

	private void doRepaint()
	{
		repaint();
	}

	private void replaceSelection(final String s)
	{
		reg_exp.replaceSelection(s);
	}

	class RegexpMenuItem
		extends JMenuItem
		implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		String pattern;

		public RegexpMenuItem(final String s, final String p)
		{
			super(s + " - " + p);

			pattern = p;

			addActionListener(this);
		}

		@Override
    public void actionPerformed(final ActionEvent event)
		{
			replaceSelection(pattern);
			doRepaint();
		}
	}

	class RegexpMenuBar
		extends JMenuBar
	{
		private static final long serialVersionUID = 1L;

		public RegexpMenuBar()
		{
			final JMenu menu = new JMenu("Expressions");

			menu.add(new RegexpMenuItem("any string", ".*"));
			menu.add(new RegexpMenuItem("any uppercase", "[A-Z]"));
			menu.add(new RegexpMenuItem("any lowercase", "[a-z]"));
			menu.add(new RegexpMenuItem("any alphabetic", "[a-zA-Z]"));
			menu.add(new RegexpMenuItem("any digit", "[0-9]"));
			this.add(menu);

			//JMenu help = new JMenu("Help");
			//help.add(new JMenuItem("Help Topics"));
			//help.add(new JSeparator());
			//help.add(new JMenuItem("About..."));
			//this.add(help);
		}
	}

	FreeFormPanel()
	{
		setLayout(new BorderLayout());
		add(new RegexpMenuBar(), BorderLayout.NORTH);

		final JPanel p1 = new JPanel();

		p1.setLayout(new BorderLayout());

		final Box yBox = new Box(BoxLayout.Y_AXIS);
		final Box x1Box = new Box(BoxLayout.X_AXIS);

		x1Box.add(new JLabel("Regexp:"));

		reg_exp = new JTextField(".*", 30);

		x1Box.add(reg_exp);

		final Box x2Box = new Box(BoxLayout.X_AXIS);

		x2Box.add(new JLabel("State Separator: "));

		// sep_str = new JTextField(".", 30);
		sep_str = new JTextField(Config.GENERAL_STATE_SEPARATOR.getAsString(), 30);

		x2Box.add(sep_str);
		yBox.add(Box.createVerticalGlue());
		yBox.add(x1Box);
		yBox.add(Box.createVerticalGlue());
		yBox.add(x2Box);
		yBox.add(Box.createVerticalGlue());

		yBox.add(pairwise_equal = new JCheckBox("Forbid pairwise equally named states", false));

		p1.add(yBox, BorderLayout.NORTH);
		add("Center", p1);
	}

	@Override
  public String getTitle()
	{
		return title;
	}

	@Override
  public String getTip()
	{
		return tip;
	}

	@Override
  public StateMatcher getMatcher()
	{
		try
		{
			final Pattern pattern = Pattern.compile(reg_exp.getText());

			return new FreeformMatcher(pattern, sep_str.getText());
		}
		catch (final PatternSyntaxException ex)
		{

			// debug("FindStatesTable::Incorrect pattern \"" + reg_exp.getText() +"\"");
			JOptionPane.showMessageDialog(null, "Incorrect pattern: " + reg_exp.getText(), "Incorrect pattern", JOptionPane.ERROR_MESSAGE);

			// logger.debug(ex.getStackTrace());
			return null;
		}
	}

	@Override
  public void setVisible(final boolean aFlag)
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
	private static final long serialVersionUID = 1L;
	private static final String title = "Fixed Form";
	private static final String tip = "Search with state specific content";
	FindStatesTable table = null;

	FixedFormPanel(final FindStatesTable t)
	{
		super(t);

		this.table = t;
	}

	@Override
  public String getTitle()
	{
		return title;
	}

	@Override
  public String getTip()
	{
		return tip;
	}

	@Override
  public StateMatcher getMatcher()
	{
		return new FixedformMatcher(table.getRegexpPatterns(), table.getStateMatcherOptions());
	}
}

class SettingsPanel
	extends JPanel
	implements FindStatesTab
{
	private static final long serialVersionUID = 1L;
	private final String title = "Settings";
	private final String tip = "Advanced settings for finding and forbidding states";
	private final JCheckBox use_dump = new JCheckBox("Use dump state instead of self-loop");

	SettingsPanel()
	{
		/* Should disable the Find and Forbid States button when focused, but...
		setFocusable(true);

		addFocusListener(new FocusListener()
		{

			public void focusGained(FocusEvent e)
			{
			}
			public void focusLost(FocusEvent e)
			{
            }
		});
		 *
		 */
		add(use_dump);
	}

	@Override
  public String getTitle()
	{
		return title;
	}

	@Override
  public String getTip()
	{
		return tip;
	}

	@Override
  public StateMatcher getMatcher()
	{
		return null;
	}

	public boolean useDump()
	{
		return use_dump.isSelected();
	}
}
// -----------------------------------------
class FindStatesFrame
	extends JFrame
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(FindStatesFrame.class);
	private FindStatesTable table = null;
	private Automata automata = null;
	private JTabbedPane tabbedPane = null;
	@SuppressWarnings("unused")
	private CancelButton quit_button = null;
	@SuppressWarnings("unused")
	private JButton find_button = null;
	private ForbidButton forbid_button = null;
	private VisualProject theVisualProject = null;
	private final SettingsPanel settingsPanel = new SettingsPanel();

	private static void debug(final String s)
	{
		logger.debug(s);
	}

	public FindStatesFrame(final VisualProject theVisualProject, final Automata selectedAutomata)
	{
		Utility.setupFrame(this, 650, 300);
		setTitle("Find States");

		this.theVisualProject = theVisualProject;
		this.automata = selectedAutomata;
		this.table = new FindStatesTable(automata, this);

		final FixedFormPanel fixedformPanel = new FixedFormPanel(table);
		final FreeFormPanel freeformPanel = new FreeFormPanel();
//		settingsPanel = new SettingsPanel();

		tabbedPane = new JTabbedPane();

		tabbedPane.addTab(fixedformPanel.getTitle(), null, fixedformPanel, fixedformPanel.getTip());
		tabbedPane.addTab(freeformPanel.getTitle(), null, freeformPanel, freeformPanel.getTip());
		tabbedPane.addTab(settingsPanel.getTitle(), null, settingsPanel, settingsPanel.getTip());

		final JPanel buttonPanel = new JPanel();

		buttonPanel.add(quit_button = new CancelButton());
		buttonPanel.add(find_button = Utility.setDefaultButton(this, new FindButton()));
		buttonPanel.add(forbid_button = new ForbidButton());

		// We only allow forbidding plant states, if you have specs, plantify first
		forbid_button.setEnabled(selectedAutomata.isAllAutomataPlants());

		final Container contentPane = getContentPane();

		contentPane.add(tabbedPane, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
	}

	void updateForbidButton(final boolean b)
	{
		forbid_button.setEnabled(b);
	}
	private Automata getAutomata()
	{
		return automata;
	}

	private FindStatesTab getSelectedComponent()
	{
		return (FindStatesTab) tabbedPane.getSelectedComponent();
	}

	class FindButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public FindButton()
		{
			super("Find");

			setToolTipText("Go ahead and find");
			addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)
				{
					action(e);
				}
			});
		}

		void action(final ActionEvent e)
		{
			goAhead(true); // find and present
		}
	}

	private class ForbidButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public ForbidButton()
		{
			super("Forbid");
			setToolTipText("Forbid found states. Only for plants. Plantify first if you have to.");

			addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)
				{
					action(e);
				}
			});
		}

		void action(final ActionEvent e)
		{
			// for each automaton
			//	if its seach criteria is "dont care" ignore it [we do not _have_ to do this but it saves efficiency]
			//	else
			// 		search only this automaton according to its criterion
			// 		self-loop each found state with	the x-event	(a single x-event for each invocation)

			// For now we try this
			goAhead(false);	// find but do not present
		}
	}

        class CancelButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public CancelButton()
		{
			super("Cancel");

			setToolTipText("Enough of finding states");
			addActionListener(new ActionListener()
			{
				@Override
        public void actionPerformed(final ActionEvent e)
				{
					action(e);
				}
			});
		}

		void action(final ActionEvent e)
		{

			// debug("CancelButton disposing");
			dispose();
		}
	}

	private void goAhead(final boolean present)
	{
		try
		{
			final StateMatcher matcher = getSelectedComponent().getMatcher();

			if (matcher != null)
			{
				SearchStates ss;

				try
				{
					ss = new SearchStates(getAutomata(), matcher);
				}
				catch (final Exception ex)
				{
					logger.error("Exception while constructing SearchState. Operation aborted. " + ex.getMessage());
					logger.debug(ex.getStackTrace());

					return;
				}

				ss.start();    // Start the search thread

				final Monitor monitor = new Monitor("Finding states...", "", ss);

				monitor.startMonitor(this, 0, 1000);

				if(present)
				{
					final PresentStates present_states = new PresentStates(this, ss, getAutomata(), theVisualProject, settingsPanel.useDump());
					present_states.start(); // From the docs: Causes this thread to begin execution;
											// the Java Virtual Machine calls the run method of this thread.
											// The result is that two threads are running concurrently: the
											// current thread (which returns from the call to the start method)
											// and the other thread (which executes its run method).
				}
				else
				{
					ss.join(); // wait for ss to stop
					new Forbidder(getAutomata(), ss, theVisualProject, settingsPanel.useDump()); // false meaning, do not use dump state
					// forbidder.start();
					// Should Forbidder be a thread of its own, monitorable/interruptable?
				}
			}
		}
		catch (final Exception ex)
		{

			// Let it silently die, how the f*** do get these excp specs to work?
			debug("FindButton - " + ex);
			logger.debug(ex.getStackTrace());
		}
	}
}

public class FindStates
	extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(FindStates.class);

	public FindStates()
	{
		super("Find States...");

		putValue(SHORT_DESCRIPTION, "Specify and search for significant states");
	}

	// Note, we avoid (short-circut) the ActionMan here... should we?
	@Override
  public void actionPerformed(final ActionEvent e)
	{
		final VisualProject theProject = ActionMan.getGui().getVisualProjectContainer().getActiveProject();
		final Automata selectedAutomata = ActionMan.getGui().getSelectedAutomata();

		try
		{
			execute(theProject, selectedAutomata);
		}
		catch (final Exception ex)
		{
			logger.error("Exception in Find States. ", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	public void execute(final VisualProject theProject, final Automata theAutomata)
		throws Exception
	{
		new FindStatesFrame(theProject, theAutomata).setVisible(true);
	}
}
