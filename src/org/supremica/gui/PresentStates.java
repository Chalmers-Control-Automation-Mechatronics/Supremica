
// ****************** PresentStates.java ***********************//
// * (Various ways of?) Presents the found states for the user
package org.supremica.gui;

import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.apache.log4j.Category;
import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.SearchStates;

class PresentStatesTableModel
	extends AbstractTableModel
{
	String[] heading = null;
	String[][] body = null;

	public PresentStatesTableModel(SearchStates ss, Automata a)
	{
		heading = new String[a.size()];

		for (int i = 0; i < a.size(); ++i)
		{
			heading[i] = a.getAutomatonAt(i).getName();
		}

		body = new String[ss.numberFound()][a.size()];

		int row = 0;

		for (Iterator it1 = ss.iterator(); it1.hasNext(); ++row)
		{
			int col = 0;

			for (SearchStates.StateIterator it2 = ss.getStateIterator((int[]) it1.next()); it2.hasNext(); it2.inc())
			{
				body[row][col++] = it2.getState().getName();
			}
		}
	}

	public int getRowCount()
	{
		return body.length;
	}

	public int getColumnCount()
	{
		return heading.length;
	}

	public String getColumnName(int col)
	{
		return heading[col];
	}

	public Object getValueAt(int row, int col)
	{
		return body[row][col];
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
	private static Category thisCategory = LogDisplay.createCategory(PresentStatesFrame.class.getName());

	private static void debug(String s)
	{
		thisCategory.debug(s);
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

		contentPane.add(new WhitePane(table), "Center");
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
public class PresentStates
{
	private JFrame frame = null;
	private SearchStates searchs = null;
	private Automata automata = null;
	private boolean dispose_frame = false;

	public PresentStates(SearchStates ss, Automata a)
	{
		searchs = ss;
		automata = a;

		// conOut(); // for debug purposes
		if (ss.numberFound() > 0)
		{
			frame = new PresentStatesFrame(ss, a);
		}
		else
		{
			frame = new NoStatesFoundFrame();
			dispose_frame = true;    // for some reason the frame cannot dispose of itself
		}
	}

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

	public void execute()
	{
		frame.show();

		if (dispose_frame)
		{
			frame.dispose();
		}
	}
}
