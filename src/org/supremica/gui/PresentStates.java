
// ****************** PresentStates.java ***********************//
// * (Various ways of?) Presents the found states for the user
package org.supremica.gui;

import java.util.Vector;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.supremica.log.*;
import org.supremica.util.IntArrayVector;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.SearchStates;
import org.supremica.gui.Presenter;

class PresentStatesTableModel
	extends DefaultTableModel // AbstractTableModel
{
	// private String[] heading = null;
	// private String[][] body = null;
	private IntArrayVector states;
//	private Automata automata;
	private SearchStates ss;
	private final int rows;
	private final int cols;
	
	private static Vector formColumnNameVector(Automata a)
	{
		Vector v = new Vector();
		
		for (int i = 0; i < a.size(); ++i)
		{
			v.add( a.getAutomatonAt(i).getName());
		}
		return v;
	}
	public PresentStatesTableModel(SearchStates ss, Automata a)
	{
		super(formColumnNameVector(a), ss.numberFound());
		
//		this.automata = a;
		this.ss = ss;		
		this.rows = ss.numberFound();
		this.cols = a.size();
		
/*		heading = new String[cols];

		for (int i = 0; i < cols; ++i)
		{
			heading[i] = a.getAutomatonAt(i).getName();
		}
*/
/* Instead of doing this, we dynamically create what is necessary in getValueAt()

		body = new String[rows][cols];

		int row = 0;

		for (Iterator it1 = ss.iterator(); it1.hasNext(); ++row)
		{
			int col = 0;

			for (SearchStates.StateIterator it2 = ss.getStateIterator((int[]) it1.next()); it2.hasNext(); it2.inc())
			{
				body[row][col++] = it2.getState().getName();
			}
		}
*/
	}
/*
	public int getRowCount()
	{
		return rows; // body.length;
	}

	public int getColumnCount()
	{
		return cols; // heading.length;
	}

	public String getColumnName(int col)
	{
		return heading[col];
	}
	public Object getValueAt(int row, int col)
	{
		return body[row][col];
		
	}*/
	// col indexes an automaton, row a state
	public Object getValueAt(int row, int col)
	{
		return ss.getState(col, row).getName();
	}

}

class PresentStatesTable
	extends JTable
{
	public PresentStatesTable(SearchStates ss, Automata a)
	{
		super(new PresentStatesTableModel(ss, a));
	}
}

class PresentStatesFrame
	extends JFrame
{
	private static Logger logger = LoggerFactory.createLogger(PresentStatesFrame.class);

	private static void debug(String s)
	{
		logger.debug(s);
	}

	private JButton setDefaultButton(JButton b)
	{
		getRootPane().setDefaultButton(b);

		return b;
	}

	private class FineButton
		extends JButton
	{
		public FineButton()
		{
			super("Close");

			setToolTipText("I'm fine, thanks");
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

			// debug("FineButton disposing");
			dispose();
		}
	}

	public PresentStatesFrame(SearchStates ss, Automata a)
	{
		Utility.setupFrame(this, 400, 300);
		setTitle("Found States");

		JTable table = new PresentStatesTable(ss, a);
		JPanel panel = new JPanel();

		panel.add(new JLabel(ss.numberFound() + " states found"));
		panel.add(setDefaultButton(new FineButton()));

		Container contentPane = getContentPane();

		contentPane.add(new WhiteScrollPane(table), "Center");
		contentPane.add(panel, "South");
	}
}

// 
class NoStatesFoundFrame
	extends JFrame
{
	public NoStatesFoundFrame()
	{
		Utility.setupFrame(this, 0, 0);
		JOptionPane.showMessageDialog(this, "No matching states found", "Zero States", JOptionPane.INFORMATION_MESSAGE);

		// ** for some reason the frame cannot dispose of itself
		// hide();
		// dispose();
	}
}

// 
class UserInterruptFrame
	extends JFrame
{
	public UserInterruptFrame()
	{
		Utility.setupFrame(this, 0, 0);
		JOptionPane.showMessageDialog(this, "Search interruped by user", "User Interrupt", JOptionPane.INFORMATION_MESSAGE);
	}
}

// 
public class PresentStates
	extends Presenter
{
	private JFrame frame = null;
	private SearchStates searchs = null;
	private Automata automata = null;
	private boolean dispose_frame = false;

	public PresentStates(JFrame frame, SearchStates ss, Automata a)
	{
		super(ss);

		this.frame = frame;
		this.searchs = ss;
		this.automata = a;
	}

	public void taskFinished()
	{
		if (searchs.numberFound() > 0)
		{
			frame = new PresentStatesFrame(searchs, automata);
		}
		else    // it was not stopped but none found
		{
			frame = new NoStatesFoundFrame();
			dispose_frame = true;    // for some reason the frame cannot dispose of itself
		}

		execute();
	}

	public void taskStopped()
	{
		frame = new UserInterruptFrame();
		dispose_frame = true;

		execute();
	}

	public void execute()
	{
		frame.show();

		if (dispose_frame)
		{
			frame.dispose();
		}
	}

	// Debugging only
	private void conOut()
	{

		// Print the number of states
		System.out.println("Number of states found: " + searchs.numberFound());

		// Now we print the automata names - do we know that the indices match?
		for (int i = 0; i < automata.size(); ++i)
		{
			System.out.print(automata.getAutomatonAt(i).getName() + ",");
		}

		System.out.println();

		// Next we print the states one by one
		for (Iterator it1 = searchs.iterator(); it1.hasNext(); )
		{
			System.out.print("<");

			for (SearchStates.StateIterator it2 = searchs.getStateIterator((int[]) it1.next()); it2.hasNext(); it2.inc())
			{
				System.out.print(it2.getState().getName() + ",");
			}

			System.out.println(">");
		}
	}
}
