//** MF ************** FindStates.java *******************//
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

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.util.*;
//---------------------------------------------------
// The table model manages the input of the regexp patterns
// It takes a compiler to be able to verify the correctness
// of the patterns on-line
class FindStatesTableModel extends AbstractTableModel
{
	private Pattern[] patterns = null;
	private PatternCompiler comp = null;
	private String[] columnNames = { "Automaton", "Type", "Regular Expression" };
	private Object[][] cells = null;

	private static final int AUTOMATON_COL = 0;
	private static final int TYPE_COL = AUTOMATON_COL + 1;
	private static final int REGEXP_COL = TYPE_COL + 1;

	public FindStatesTableModel(Automata a, PatternCompiler c)
	{
		final int size = a.size();
		cells = new Object[size][REGEXP_COL+1];
		for(int it = 0; it < size; ++it)
		{
			cells[it][AUTOMATON_COL] = a.getAutomatonAt(it).getName();
			cells[it][TYPE_COL] = a.getAutomatonAt(it).getType().toString();
			cells[it][REGEXP_COL] = ".*";
		}

		comp = c;
		patterns = new Pattern[size];
		try // I know compile _cannot_ throw here, but Java requires me to catch this exception
		{
			Pattern any_string = comp.compile(".*");
			for(int i = 0; i < patterns.length; ++i)
			{
				patterns[i] = any_string;
			}
		}
		catch(MalformedPatternException excp)
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
		if(isRegexpColumn(col))
		{
			try
			{
				patterns[row] = comp.compile((String)obj);
			}
			catch(MalformedPatternException excp)
			{
				JOptionPane.showMessageDialog(null, "Incorrect pattern", "Incorrect pattern", JOptionPane.ERROR_MESSAGE);
			}
		}

	}
	public boolean isCellEditable(int row, int col)
	{
		return col == REGEXP_COL; // the one and only editable column
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
//-----------------------------------
class FindStatesTable extends JTable
{
	private void doRepaint() { repaint(); }
	
	//** Inner class, needs access to the model
	class RegexpPopupMenu extends JPopupMenu
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
			{	// anonymous class 
				public void actionPerformed(ActionEvent e)
				{
					String str = (String)getModel().getValueAt(row, col);
					RegexpDialog regexp_dialog = new RegexpDialog(null, str);
					if(regexp_dialog.isOk())
					{
						getModel().setValueAt(regexp_dialog.getText(), row, col);
					}
					doRepaint(); // for resolving ambiguity
				}
			});
		}
	
	}
	
	// utility functions
	private TableSorter getTableSorterModel() { return (TableSorter)getModel(); }
	private FindStatesTableModel getStatesTableModel() { return (FindStatesTableModel)getTableSorterModel().getModel(); }
	
	// Wrap the FindStatesTableModel inside a sort filter
	private static TableSorter makeTableModel(Automata a)
	{
		TableSorter sorter = new TableSorter();
		sorter.setModel(new FindStatesTableModel(a, new Perl5Compiler())); // compiler type should be adjustable
		return sorter;
	}
	public FindStatesTable(Automata a)
	{
		//** If TableSorter had a proper constructor we could have done
		//** super(new TableSorter(new FindStatesTableModel(a)));
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
				if(e.isPopupTrigger() && getStatesTableModel().isRegexpColumn(col))
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
					RegexpPopupMenu regexp_popup = new RegexpPopupMenu(row, col);
					regexp_popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

	}

	public Pattern[] getRegexpPatterns()
	{
		return getStatesTableModel().getRegexpPatterns();
	}
}
//-----------------------------------------
class FindStatesFrame extends JFrame /* CenteredFrame */
{
	private FindStatesTable table = null;
	private Automata automata = null;
	
	private static Category thisCategory = LogDisplay.createCategory(FindStatesFrame.class.getName());
	private void Debug(String s) { thisCategory.debug(s); }

	private Automata getAutomata() { return automata; }
	private Pattern[] getRegexpPatterns() { return table.getRegexpPatterns(); }
	private JButton setDefaultButton(JButton b) { getRootPane().setDefaultButton(b); return b; }

	class FindButton extends JButton
	{

		public FindButton()
		{
			super("Find");
			setToolTipText("Go ahead and find");

			addActionListener(
				new ActionListener()
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
				// Debug("SearchStates == " + (ss == null ? "null" : "ok"));
				Pattern[] patterns = getRegexpPatterns();
				ss.search(new Perl5Matcher(), patterns);
				// Debug("Show composite states");
				showCompositeStates(ss);

			}
			catch(Exception excp)
			{
				// Let it silently die, how the f*** do get these excp specs to work?
				Debug("FindButton - " + excp);
				excp.printStackTrace();
			}
		}
		void showCompositeStates(SearchStates ss)
		{
			PresentStates present_states = new PresentStates(ss, getAutomata());
			present_states.execute();
		}
	}

	class QuitButton extends JButton
	{
		public QuitButton()
		{
			super("Quit");
			setToolTipText("Enough of finding states");
			addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						action(e);
					}
				});
		}

		void action(ActionEvent e)
		{
			Debug("QuitButton disposing");
			dispose();
		}
	}

	public FindStatesFrame(Automata a)
	{
		// super(400, 300); // for CenteredFrame inheritance
		Utility.setupFrame(this, 400, 300);
		setTitle("Find States");

		automata = a;
		table = new FindStatesTable(a);

		JPanel panel = new JPanel();
		panel.add(setDefaultButton(new FindButton()));
		panel.add(new QuitButton());

		Container contentPane = getContentPane();
		contentPane.add(new WhitePane(table), "Center");
		contentPane.add(panel, "South");
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