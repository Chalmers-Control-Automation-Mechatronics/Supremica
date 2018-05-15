//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.Forbidder;
import org.supremica.automata.algorithms.Remover;
import org.supremica.automata.algorithms.SearchStates;


/**
 * @author Martin Fabian
 */

class PresentStatesTableModel
	extends DefaultTableModel    // AbstractTableModel
{
	private static final long serialVersionUID = 1L;

	private final SearchStates ss;
	@SuppressWarnings("unused")
	private final int rows;
	@SuppressWarnings("unused")
	private final int cols;

	private static Vector<String> formColumnNameVector(final Automata a)
	{
		final Vector<String> v = new Vector<>();

		for (int i = 0; i < a.size(); ++i)
		{
			v.add(a.getAutomatonAt(i).getName());
		}

		return v;
	}

	public PresentStatesTableModel(final SearchStates ss, final Automata a)
	{
		super(formColumnNameVector(a), ss.numberFound());

		this.ss = ss;
		this.rows = ss.numberFound();
		this.cols = a.size();
	}

	// col indexes an automaton, row a state
	@Override
	public Object getValueAt(final int row, final int col)
	{
		return ss.getState(col, row).getName();
	}

	// None of the cells are editable (DefaultTableMode return true! AbstractTableModel does not!!)
	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex)
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
	private static final Logger logger = LogManager.getLogger(PresentStatesTable.class);
	private SelectionListener listener;
	private final VisualProject theVisualProject;
	private final SearchStates searchStates;
	private final Automata theAutomata;

	public PresentStatesTable(final SearchStates ss, final Automata a, final VisualProject theVisualProject)
	{
		super(new PresentStatesTableModel(ss, a));

		this.theVisualProject = theVisualProject;
		this.searchStates = ss;
		this.theAutomata = a;

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				final int currRow = rowAtPoint(new Point(e.getX(), e.getY()));

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
	private void viewInAutomatonExplorer(final int index)
	{
		final Automaton currAutomaton = theAutomata.getFirstAutomaton();
		final State currState = searchStates.getState(0, index);

		try
		{
			final AutomatonExplorer theExplorer = theVisualProject.getAutomatonExplorer(currAutomaton.getName());

			theExplorer.setState(currState);
		}
		catch (final Exception ex)
		{
			logger.error("Could not create AutomatonExplorer.");
			logger.debug(ex.getStackTrace());
		}
	}

	public void setSelectionListener(final SelectionListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void valueChanged(final ListSelectionEvent e)
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
	private static final Logger logger = LogManager.getLogger(PresentStatesFrame.class);

	private final SearchStates search_states;
	private final Automata automata;
	private final VisualProject theVisualProject;
	private final PresentStatesTable table;
	private final ForbidButton forbid_button;	// This duplicates code from FindStates.java
	private final RemoveButton remove_button;	// as above
//      private RouteButton route_button;

	@SuppressWarnings("unused")
	private static void debug(final String s)
	{
		logger.debug(s);
	}

	private class CloseButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public CloseButton()
		{
			super("Close");

			setToolTipText("Close this window");
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

			// debug("CloseButton disposing");
			dispose();
		}
	}

	private class RemoveButton extends JButton
	{
		private static final long serialVersionUID = 1L;

		public RemoveButton()
		{
			super("Remove");
			setToolTipText("Remove outgoing transitions from selected states");
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
			final Remover remover = new Remover(automata, table.getSelectedRows(), search_states, theVisualProject);
			remover.remove();
			dispose();
		}
	}

	private class ForbidButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;
		private final boolean use_dump;

		public ForbidButton(final boolean use_dump)
		{
			super("Forbid");
			setToolTipText("Modularly forbid selected states"); // if none selected, should forbid all?
			setEnabled(false);
			this.use_dump = use_dump;

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
			final Forbidder forbidder = new Forbidder(automata, table.getSelectedRows(), search_states, theVisualProject, use_dump);
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
	public PresentStatesFrame(final SearchStates ss, final Automata a, final VisualProject theVisualProject, final boolean use_dump)
	{
		this.search_states = ss;
		this.automata = a;
		this.theVisualProject = theVisualProject;

		Utility.setupFrame(this, 400, 300);
		setTitle("Found States - " + ss.numberFound());

//              route_button = new RouteButton();
//              route_button.setEnabled(false);
		forbid_button = new ForbidButton(use_dump);
		forbid_button.setEnabled(a.isAllAutomataPlants());
		table = new PresentStatesTable(ss, automata, theVisualProject);

		table.setSelectionListener(this);

		final JPanel panel = new JPanel();

		// panel.add(new JLabel(ss.numberFound() + " states found"));
		panel.add(Utility.setDefaultButton(this, new CloseButton()));
		panel.add(forbid_button);
//              panel.add(route_button);
		panel.add(remove_button = new RemoveButton());
		remove_button.setEnabled(false);

		final Container contentPane = getContentPane();

		contentPane.add(new WhiteScrollPane(table), BorderLayout.CENTER);
		contentPane.add(panel, BorderLayout.SOUTH);
	}

	// SelectionListener interface implementation
	@Override
	public void emptySelection()
	{
		forbid_button.setEnabled(false);
//              route_button.setEnabled(false);
		remove_button.setEnabled(false);
	}

	@Override
	public void nonEmptySelection()
	{
		forbid_button.setEnabled(true);
		// Utility.setDefaultButton(this, route_button);
//              route_button.setEnabled(false); // enable when implemented
		remove_button.setEnabled(true);
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
	private JFrame frame;
	private final SearchStates searchs;
	private final Automata automata;
	private boolean dispose_frame;
	private final VisualProject theVisualProject;
	private final boolean use_dump;

	public PresentStates(final JFrame frame, final SearchStates ss, final Automata a, final VisualProject theVisualProject, final boolean use_dump)
	{
		super(ss);	// PresentStates is a Presenter, which is a Thread. Calling start() on PresentStates
					// invokes Presenter::run() which waits for ss to finish, before calling taskStopped()
					// or taskFinished, depending on whether the user stopped the task (ss) or not.

		this.frame = frame;
		this.searchs = ss;
		this.automata = a;
		this.theVisualProject = theVisualProject;
		this.use_dump = use_dump;
	}

	@Override
	public void taskFinished()
	{
		if (searchs.numberFound() > 0)
		{
			frame = new PresentStatesFrame(searchs, automata, theVisualProject, use_dump);
		}
		else    // it was not stopped but none found
		{
			frame = new NoStatesFoundFrame();
			dispose_frame = true;    // for some reason the frame cannot dispose of itself
		}

		execute();
	}

	@Override
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
		for (final Iterator<?> it1 = searchs.iterator(); it1.hasNext(); )
		{
			System.out.print("<");

			for (final SearchStates.StateIterator it2 = searchs.getStateIterator((int[]) it1.next());
					it2.hasNext(); it2.inc())
			{
				System.out.print(it2.getState().getName() + ",");
			}

			System.out.println(">");
		}
	}
}
