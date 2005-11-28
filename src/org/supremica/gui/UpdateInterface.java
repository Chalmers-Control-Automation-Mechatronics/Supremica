
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
 * @author knut@supremica.org
 */

// Implements the UpdateInterface dialog with regexps
package org.supremica.gui;

import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import org.supremica.log.*;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomataListener;

class UpdateInterfaceTableModel
	extends AbstractTableModel
	implements AutomataListener    // could usefully inherit from AutomataTableModel or something like that
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(UpdateInterfaceTableModel.class);
	private String[] columnNames = { "Automaton", "Type" };
	private Automata selectableAutomata;
	public final static int AUTOMATON_COL = 0;
	public final static int TYPE_COL = AUTOMATON_COL + 1;

	public UpdateInterfaceTableModel(Automata selectableAutomata)
	{
		this.selectableAutomata = selectableAutomata;

		selectableAutomata.addListener(this);
	}

	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	public int getColumnCount()
	{
		return columnNames.length;
	}

	public int getRowCount()
	{
		return selectableAutomata.nbrOfAutomata();
	}

	public Object getValueAt(int row, int col)
	{
		Automaton automaton = selectableAutomata.getAutomatonAt(row);

		switch (col)
		{

		case AUTOMATON_COL :
			return automaton.getName();

		case TYPE_COL :
			return automaton.getType();
		}

		return null;
	}

	public void setValueAt(Object obj, int row, int col) {}

	public boolean isCellEditable(int row, int col)
	{
		return false;
	}

	// implementation of AutomataListener interface
	private void updateListeners()
	{
		TableModelEvent event = new TableModelEvent(this, 0, selectableAutomata.nbrOfAutomata() - 1);

		fireTableChanged(event);
	}

	public void automatonAdded(Automata automata, Automaton automaton)
	{
		updateListeners();
	}

	public void automatonRemoved(Automata automata, Automaton automaton)
	{
		updateListeners();
	}

	public void automatonRenamed(Automata automata, Automaton automaton)
	{
		updateListeners();
	}

	public void actionsOrControlsChanged(Automata automata)
	{    // Do nothing
	}

	public void updated(Object theObject)
	{
		updateListeners();
	}
}

// -----------------------------------
class UpdateInterfaceTable
	extends JTable
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(UpdateInterfaceTable.class);
	private Automata selectableAutomata;
	private Automata selectedAutomata;
	private UpdateInterfaceDialog theFrame;

	public UpdateInterfaceTable(Automata selectableAutomata, Automata selectedAutomata, UpdateInterfaceDialog theFrame)
	{
		super(makeTableModel(selectableAutomata));

		this.selectableAutomata = selectableAutomata;
		this.selectedAutomata = selectedAutomata;
		this.theFrame = theFrame;

		getTableSorterModel().addMouseListenerToHeaderInTable(this);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setSelectedAutomata();

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

			// Does not do anything useful
			private void maybeShowPopup(MouseEvent e)
			{

/*
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

								}
*/
			}
		});
	}

	// local utility functions
	private TableSorter getTableSorterModel()
	{
		return (TableSorter) getModel();
	}

	private Automaton getAutomaton(int row)
	{
		String name = (String) getModel().getValueAt(row, UpdateInterfaceTableModel.AUTOMATON_COL);

		System.err.println(name);

		return selectableAutomata.getAutomaton(name);
	}

	private int getRow(Automaton theAutomaton)
	{    // This implementation is only valid if no resorting has been made
		return selectedAutomata.getAutomatonIndex(theAutomaton);
	}

	private void doRepaint()
	{
		repaint();
	}

	private UpdateInterfaceTable getThisTable()
	{
		return this;
	}

	public void setSelectedAutomata()
	{
		ListSelectionModel selectionModel = getSelectionModel();

		selectionModel.clearSelection();

		for (Iterator autIt = selectedAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			int currRow = getRow(currAutomaton);

			if (currRow < 0)
			{
				logger.error("setSelectedAutomata: currRow < 0");

				return;
			}

			setRowSelectionInterval(currRow, currRow);
		}
	}

	public void updateSelectedAutomata()
	{
		int[] selectedRows = getSelectedRows();

		selectedAutomata.clear();

		for (int i = 0; i < selectedRows.length; i++)
		{
			int currRow = selectedRows[i];
			Automaton currAutomaton = getAutomaton(currRow);

			System.err.println(currRow + " " + currAutomaton);
			selectedAutomata.addAutomaton(currAutomaton);
		}
	}

	// Wrap the UpdateInterfaceTableModel inside a sort filter
	private static TableSorter makeTableModel(Automata a)
	{
		TableSorter sorter = new TableSorter();

		sorter.setModel(new UpdateInterfaceTableModel(a));

		return sorter;
	}
}


// ------------------------------------------
interface UpdateInterfaceTab
{
	String getTitle();

	String getTip();
}


class InterfaceAutomataPanel
	extends WhiteScrollPane
	implements UpdateInterfaceTab
{
	private static final long serialVersionUID = 1L;
	private static final String title = "Interface Automata";
	private static final String tip = "Select the automata dependencies for this interface";
	UpdateInterfaceTable table = null;

	InterfaceAutomataPanel(UpdateInterfaceTable t)
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
}

// -----------------------------------------
class UpdateInterfaceDialog
	extends JDialog
{
	private static final long serialVersionUID = 1L;
	private UpdateInterfaceTable masterTable = null;
	private UpdateInterfaceTable slaveTable = null;
	private VisualProject theProject = null;
	private Automaton theInterface = null;
	private JTabbedPane tabbedPane = null;
	private JButton ok_button = null;
	private CancelButton cancel_button = null;
	private static Logger logger = LoggerFactory.createLogger(UpdateInterfaceDialog.class);

	public UpdateInterfaceDialog(JFrame owner, VisualProject theProject, Automaton theInterface)
	{
		super(owner, "Update Interface", true);

		Utility.setupDialog(this, 500, 300);

		this.theInterface = theInterface;
		this.theProject = theProject;

		Automata selectableAutomata = new Automata(theProject, true);

		selectableAutomata.removeAutomaton(theInterface);
		theInterface.purgeInterfaceAutomata(theProject);

		this.masterTable = new UpdateInterfaceTable(selectableAutomata, theInterface.getMasterAutomata(), this);
		this.slaveTable = new UpdateInterfaceTable(selectableAutomata, theInterface.getSlaveAutomata(), this);

		InterfaceAutomataPanel masterPanel = new InterfaceAutomataPanel(masterTable);
		InterfaceAutomataPanel slavePanel = new InterfaceAutomataPanel(slaveTable);

		tabbedPane = new JTabbedPane();

		tabbedPane.addTab(masterPanel.getTitle(), null, masterPanel, masterPanel.getTip());
		tabbedPane.addTab(slavePanel.getTitle(), null, slavePanel, slavePanel.getTip());

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(ok_button = Utility.setDefaultButton(this, new OkButton()));
		buttonPanel.add(cancel_button = new CancelButton());

		Container contentPane = getContentPane();

		contentPane.add(tabbedPane, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
	}

	private static void debug(String s)
	{
		logger.debug(s);
	}

	private void doRepaint()
	{
		repaint();
	}

	private UpdateInterfaceTab getSelectedComponent()
	{
		return (UpdateInterfaceTab) tabbedPane.getSelectedComponent();
	}

	class OkButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public OkButton()
		{
			super("Ok");

			setToolTipText("Ok");
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
		private static final long serialVersionUID = 1L;

		public CancelButton()
		{
			super("Cancel");

			setToolTipText("Cancel");
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
			dispose();
		}
	}

	private void goAhead()
	{
		masterTable.updateSelectedAutomata();
		slaveTable.updateSelectedAutomata();
		setVisible(false);
	}
}

public class UpdateInterface
{
	JDialog theDialog = null;

	public UpdateInterface(JFrame owner, VisualProject theProject, Automaton theInterface)
	{
		theDialog = new UpdateInterfaceDialog(owner, theProject, theInterface);
	}

	public void execute()
	{
		theDialog.setVisible(true);
	}
}
