
// ****************** PresentStates.java ***********************//
// * (Various ways of?) Presents the found states for the user
package org.supremica.gui;

import java.util.Vector;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.supremica.log.*;
import org.supremica.util.IntArrayVector;
import org.supremica.automata.Automata;
import org.supremica.automata.State;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.SearchStates;
import org.supremica.gui.Presenter;
import org.supremica.gui.VisualProject;

//-- owner: MF
class PresentStatesTableModel
	extends DefaultTableModel    // AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	private IntArrayVector states;
	private SearchStates ss;
	private final int rows;
	private final int cols;

	private static Vector formColumnNameVector(Automata a)
	{
		Vector v = new Vector();

		for (int i = 0; i < a.size(); ++i)
		{
			v.add(a.getAutomatonAt(i).getName());
		}

		return v;
	}

	public PresentStatesTableModel(SearchStates ss, Automata a)
	{
		super(formColumnNameVector(a), ss.numberFound());

		this.ss = ss;
		this.rows = ss.numberFound();
		this.cols = a.size();
	}

	// col indexes an automaton, row a state
	public Object getValueAt(int row, int col)
	{
		return ss.getState(col, row).getName();
	}

	// None of the cells are editable (DefaultTableMode return true! AbstractTableModel does not!!)
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}
}


interface SelectionListener    // should this be a utility class?
{
	void emptySelection();

	void nonEmptySelection();
}


class PresentStatesTable
	extends JTable
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(PresentStatesTable.class);
	private SelectionListener listener;
	private VisualProject theVisualProject;
	private SearchStates searchStates;
	private Automata theAutomata;

	public PresentStatesTable(SearchStates ss, Automata a, VisualProject theVisualProject)
	{
		super(new PresentStatesTableModel(ss, a));

		this.theVisualProject = theVisualProject;
		this.searchStates = ss;
		this.theAutomata = a;

		// This code handles a flaw in the selection model
		// By default, once a row is selected it cannot be unseleced unless a new row is selected
		// This is changed so that a selected row is unselected by clicking it again
		//-- Note, this works only for right-click selection (and it shouldn't really...)
		//-- For left click the selection has appearently already been effected, so we always deselect
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				int currRow = rowAtPoint(new Point(e.getX(), e.getY()));

				if (currRow < 0)
				{
					return;
				}

				if (isRowSelected(currRow))    // then unselect it
				{
					removeRowSelectionInterval(currRow, currRow);
				}
				else
				{
					addRowSelectionInterval(currRow, currRow);
				}

				if (e.getClickCount() == 2)
				{
					if (theAutomata.size() == 1)
					{
						viewInAutomatonExplorer(currRow);
					}
				}
			}
		});

		if (theAutomata.size() == 1)
		{
			setToolTipText("Doubleclick to view in explorer");
		}

		getTableHeader().setReorderingAllowed(false);
	}

	/**
	 * This is only valid to call when exactly one state in one automaton is selected.
	 */
	private void viewInAutomatonExplorer(int index)
	{
		Automaton currAutomaton = theAutomata.getFirstAutomaton();
		State currState = searchStates.getState(0, index);

		try
		{
			AutomatonExplorer theExplorer = theVisualProject.getAutomatonExplorer(currAutomaton.getName());

			theExplorer.setState(currState);
		}
		catch (Exception ex)
		{
			logger.error("Could not create AutomatonExplorer.");
			logger.debug(ex.getStackTrace());
		}
	}

	public void setSelectionListener(SelectionListener listener)
	{
		this.listener = listener;
	}

	public void valueChanged(ListSelectionEvent e)
	{
		super.valueChanged(e);

		if (listener != null)
		{
			if (selectionModel.isSelectionEmpty())
			{
				listener.emptySelection();
			}
			else
			{
				listener.nonEmptySelection();
			}
		}
	}
}

class PresentStatesFrame
	extends JFrame
	implements SelectionListener    // listens to selection events, en/disables the RouteButton
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(PresentStatesFrame.class);

//      private RouteButton route_button;
	private SearchStates search_states;
	private VisualProject theVisualProject;

	private static void debug(String s)
	{
		logger.debug(s);
	}

	private class FineButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public FineButton()
		{
			super("Close");

			setToolTipText("Close this window");
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

/*
		private class RouteButton
				extends JButton
		{
				public RouteButton()
				{
						super("Route");

						setToolTipText("Find traces to selected states");
						addActionListener(new ActionListener()
						{
								public void actionPerformed(ActionEvent e)
								{
								}
						});
				}
		}
*/
	public PresentStatesFrame(SearchStates ss, Automata a, VisualProject theVisualProject)
	{
		this.search_states = ss;
		this.theVisualProject = theVisualProject;

		Utility.setupFrame(this, 400, 300);
		setTitle("Found States - " + ss.numberFound());

//              route_button = new RouteButton();
//              route_button.setEnabled(false);
		PresentStatesTable table = new PresentStatesTable(ss, a, theVisualProject);

		table.setSelectionListener(this);

		JPanel panel = new JPanel();

		// panel.add(new JLabel(ss.numberFound() + " states found"));
		panel.add(Utility.setDefaultButton(this, new FineButton()));

//              panel.add(route_button);
		Container contentPane = getContentPane();

		contentPane.add(new WhiteScrollPane(table), BorderLayout.CENTER);
		contentPane.add(panel, BorderLayout.SOUTH);
	}

	// SelectionListener interface implementation
	public void emptySelection()
	{

//              route_button.setEnabled(false);
	}

	public void nonEmptySelection()
	{

		// Utility.setDefaultButton(this, route_button);
//              route_button.setEnabled(false); // enable when implemented
	}
}

//
class NoStatesFoundFrame
	extends JFrame
{
	private static final long serialVersionUID = 1L;

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
	private static final long serialVersionUID = 1L;

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
	private VisualProject theVisualProject;

	public PresentStates(JFrame frame, SearchStates ss, Automata a, VisualProject theVisualProject)
	{
		super(ss);

		this.frame = frame;
		this.searchs = ss;
		this.automata = a;
		this.theVisualProject = theVisualProject;
	}

	public void taskFinished()
	{
		if (searchs.numberFound() > 0)
		{
			frame = new PresentStatesFrame(searchs, automata, theVisualProject);
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

			for (SearchStates.StateIterator it2 = searchs.getStateIterator((int[]) it1.next());
					it2.hasNext(); it2.inc())
			{
				System.out.print(it2.getState().getName() + ",");
			}

			System.out.println(">");
		}
	}
}
