
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
import org.supremica.automata.algorithms.Forbidder;
import org.supremica.gui.Presenter;
import org.supremica.gui.VisualProject;

//-- owner: MF
class PresentStatesTableModel
	extends DefaultTableModel    // AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private IntArrayVector states;
	private SearchStates ss;
	@SuppressWarnings("unused")
	private final int rows;
	@SuppressWarnings("unused")
	private final int cols;

	private static Vector<String> formColumnNameVector(Automata a)
	{
		Vector<String> v = new Vector<String>();

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

		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				int currRow = rowAtPoint(new Point(e.getX(), e.getY()));
				
/*	Why would this be an error? When is this problematic?
 *		// This code handles a flaw in the selection model
		// By default, once a row is selected it cannot be unseleced unless a new row is selected
		// This is changed so that a selected row is unselected by clicking it again
		//-- Note, this works only for right-click selection (and it shouldn't really...)
		//-- For left click the selection has appearently already been effected, so we always deselect

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
*/
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
	implements SelectionListener    // listens to selection events, en/disables the RouteButton, ForbidButton
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(PresentStatesFrame.class);

	private SearchStates search_states;
	private Automata automata;
	private VisualProject theVisualProject;
	private PresentStatesTable table;
	private ForbidButton forbid_button;
//      private RouteButton route_button;

	@SuppressWarnings("unused")
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

	private class ForbidButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public ForbidButton()
		{
			super("Forbid");
			setToolTipText("Forbid selected states"); // if none selected, should forbid all?
			setEnabled(false);

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
			
			// Get selected states
			// Get involved automata
			// Set mousepointer to timeglass
			// Instantiate a Forbidder object
			// Let it do its work
			// Reset mousepointer
			
			/** / For now, iterate over all state
			Iterator it = search_states.iterator();
			while(it.hasNext())
			{
				// Write out the global state
				int[] composite_state = (int[])it.next();
				String name = search_states.toString(composite_state);
				logger.info(name);

				// Write out each partial state by itself prefixed with automaton name
				int i = 0; // holds automaton index
				SearchStates.StateIterator state_it = search_states.getStateIterator(composite_state);
				while(state_it.hasNext())
				{
					Automaton automaton = automata.getAutomatonAt(i++);
					
					State state = state_it.getState();
					logger.info(automaton.getName() + ": " + state.getName());
					state_it.inc();
				}
			}
			**/
			
			/** For now: iterate over the selected composite states
			int[] selects = table.getSelectedRows();	// holds the indices for all selected rows
			// Each row is a composite state, loop over all rows/composite states
			for(int i = 0; i < selects.length; ++i)
			{
				int indx = selects[i];	// This is the index for one particular composite state (row)
				
				// Loop over all automata -- a is the index for one particular automaton (col)
				for(int a = 0; a < automata.nbrOfAutomata(); ++a)
				{
					Automaton automaton = automata.getAutomatonAt(a);
					State state = search_states.getState(a, indx);
					// logger.info(automaton.getName() + ": " + table.getValueAt(indx, a));
					logger.info(automaton.getName() + ": " + state.getName());

				}
			}
			**/ 
			@SuppressWarnings("unused")
			Forbidder forbidder = new Forbidder(automata, table.getSelectedRows(), search_states, theVisualProject);
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
		this.automata = a;
		this.theVisualProject = theVisualProject;

		Utility.setupFrame(this, 400, 300);
		setTitle("Found States - " + ss.numberFound());

//              route_button = new RouteButton();
//              route_button.setEnabled(false);
		forbid_button = new ForbidButton();
		table = new PresentStatesTable(ss, automata, theVisualProject);

		table.setSelectionListener(this);

		JPanel panel = new JPanel();

		// panel.add(new JLabel(ss.numberFound() + " states found"));
		panel.add(Utility.setDefaultButton(this, new FineButton()));
		panel.add(forbid_button);
//              panel.add(route_button);
		Container contentPane = getContentPane();

		contentPane.add(new WhiteScrollPane(table), BorderLayout.CENTER);
		contentPane.add(panel, BorderLayout.SOUTH);
	}

	// SelectionListener interface implementation
	public void emptySelection()
	{
		forbid_button.setEnabled(false);
//              route_button.setEnabled(false);
	}

	public void nonEmptySelection()
	{
		forbid_button.setEnabled(true);
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
		super(ss);	// PresentStates is a Presenter, which is a Thread. Calling start() on PresentStates
					// invokes Presenter::run() which waits for ss to finish, before calling taskStopped()
					// or taskFinished, depending on whether the user stopped the task (ss) or not.

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
		frame.setVisible(true);

		if (dispose_frame)
		{
			frame.dispose();
		}
	}

	// Debugging only
	@SuppressWarnings("unused")
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
		for (Iterator<?> it1 = searchs.iterator(); it1.hasNext(); )
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
