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
		try // I know compile _cannot_ throw here, but Java requires me to catch thsi exception
		{
			Pattern any_string = comp.compile(".*");
			for(int i = 0; i < patterns.length; ++i)
				patterns[i] = any_string;
		}
		catch(MalformedPatternException excp)
		{
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
		/*
		String[] strs = new String[getRowCount()];
		for(int i = 0; i < strs.length; ++i)
		{
			strs[i] = (String)cells[i][REGEXP_COL];
		}
		return strs;
		*/
	}
}
//-----------------------------------
class RegexpPopupMenu extends PopupMenu
{
	public RegexpPopupMenu()
	{
	}
	
}
//-----------------------------------
class FindStatesTable extends JTable
{

	//** This class solves a problem with inner classes
	//** Seems an innerclass can only access functions of its container
	//** Thus, we cannot access the regexp_popupmenu unless it's inside
	//** of here (or through a function call to the container)
	//** This is also the reason for getTable(), is there a better way
	//** to get a ref to the container?
	class localMouseAdapter extends MouseAdapter
	{
		private RegexpPopupMenu regexp_popup;
		
		public localMouseAdapter()
		{
			regexp_popup = new RegexpPopupMenu();
		}
		
        public void mouseClicked(MouseEvent e)
        {
        	TableColumnModel columnModel = getColumnModel();
    		int viewColumn = columnModel.getColumnIndexAtX(e.getX());
	        int column = convertColumnIndexToModel(viewColumn);

	        if( /* e.isPopupTrigger() && */ e.getClickCount() == 1 /**/ &&
        		((FindStatesTableModel)((TableSorter)getModel()).getModel()).isRegexpColumn(column))
        	{
				boolean shiftPressed = ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0);
				if(shiftPressed)
					// Pop up a menu
					regexp_popup.show(e.getComponent(), e.getX(), e.getY());

        	}
        }
	}
	
	//** Wrap the FindStatesTableModel inside a sort filter
	static TableSorter makeTableModel(Automata a)
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
		
		((TableSorter)getModel()).addMouseListenerToHeaderInTable(this);
		
		// addMouseListener(new localMouseAdapter());
 
	}
	
	public FindStatesTable getTable()
	{
		return this;
	}
	
	public Pattern[] getRegexpPatterns() 
	{
		return ((FindStatesTableModel)((TableSorter)getModel()).getModel()).getRegexpPatterns();
	}
}
//-----------------------------------------
class FindStatesFrame extends JFrame /* CenteredFrame */
{
	private FindStatesTable table = null;
	private Automata automata = null;
	private static Category thisCategory = LogDisplay.createCategory(FindStatesFrame.class.getName());

	Automata getAutomata() { return automata; }
	Pattern[] getRegexpPatterns() { return table.getRegexpPatterns(); }
	void Debug(String s) { thisCategory.debug(s); }
	
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
			/* build the pattern
			String[] strs = getRegexpColumn();
			Pattern[] patterns = new Pattern[strs.length];
			PatternCompiler comp = new Perl5Compiler();
			// Should test for malformed expression?? (no, should be done 'online')
			try
			{
				for(int i = 0; i < patterns.length; ++i)
				{
					patterns[i] = comp.compile(strs[i]);
				}
			}
			catch(MalformedPatternException excp)
			{
				System.err.println("Bad pattern.");
        		System.err.println(excp.getMessage());
			}
			*/
			// synchronize the automata but don't build the new automaton (throw away the edges)
			// just save the states (efficiently) and mark those that match as matching
			try
			{
				SearchStates ss = new SearchStates(getAutomata());
				Debug("SearchStates == " + (ss == null ? "null" : "ok"));
				Pattern[] patterns = getRegexpPatterns();
				ss.search(new Perl5Matcher(), patterns);
				Debug("Show composite states");
				showCompositeStates(ss);
				
			}
			catch(Exception excp)
			{
				// Let it silently die, how the f*** do get these excp specs to work?
				Debug("FindButton - " + excp);
			}
		}
		void showCompositeStates(SearchStates ss)
		{
			for(Iterator it1 = ss.iterator(); it1.hasNext(); )
			{
				System.out.print("<");
				
				for(SearchStates.StateIterator it2 = ss.getStateIterator((int[])it1.next()); it2.hasNext(); it2.inc())
				{
					System.out.print(it2.getState().getName() + ",");
				}
				
				System.out.println(">");
			}
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
	
	private JButton findButton;
	private JButton quitButton;
	
	public FindStatesFrame(Automata a)
	{
		// super(400, 300); // for CenteredFrame inheritance
		Utility.setupFrame(this, 400, 300);
		setTitle("Find States");
		
		automata = a;
		table = new FindStatesTable(a);
		
		JPanel panel = new JPanel();
		panel.add(new FindButton());
		panel.add(new QuitButton());
		
		Container contentPane = getContentPane();
		contentPane.add(new WhitePane(table), "Center");
		contentPane.add(panel, "South");
	}
}

public class FindStates
{
	private Automata automata;
	private JFrame frame;
	
	public FindStates(Automata a) 
	{ 
		automata = a; 
		frame = new FindStatesFrame(a);
	}	
	
	public void execute()
	{
		frame.show();
	}
	
}