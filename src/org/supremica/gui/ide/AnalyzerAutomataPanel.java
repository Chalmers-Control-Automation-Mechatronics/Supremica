package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.TableSorter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.supremica.gui.VisualProject;
//import org.supremica.gui.MainPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class AnalyzerAutomataPanel
	extends WhiteScrollPane
	implements TableModelListener
{
	private ModuleContainer moduleContainer;
	private String name;
	private JTable theAutomatonTable;
	private TableSorter theTableSorter;
	private TableModel fullTableModel;
//	private MainPopupMenu mainPopupMenu = new MainPopupMenu(this);

	AnalyzerAutomataPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
		initialize();
	}

	public String getName()
	{
		return name;
	}

	private void initialize()
	{
		fullTableModel = getActiveProject().getFullTableModel();
		theTableSorter = new TableSorter(fullTableModel);
		theAutomatonTable = new JTable(theTableSorter);

		theAutomatonTable.getTableHeader().setReorderingAllowed(false);

		getViewport().add(theAutomatonTable);

		theTableSorter.addMouseListenerToHeaderInTable(theAutomatonTable);

		fullTableModel.addTableModelListener(this);
		theAutomatonTable.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
				{
//					ActionMan.automataDelete_actionPerformed(getGui());
				}
			}

			public void keyReleased(KeyEvent e) {}

			public void keyTyped(KeyEvent e) {}
		});


		// Set the preferred column width of the automaton table
		int tableWidth = theAutomatonTable.getWidth();
		int tableWidthEntity = tableWidth / 12;
		TableColumnModel theTableColumnModel = theAutomatonTable.getColumnModel();

		for (int i = 0; i < theAutomatonTable.getColumnCount(); i++)
		{
			TableColumn currColumn = theTableColumnModel.getColumn(i);

			if (i == 0)
			{
				currColumn.setPreferredWidth(tableWidthEntity * 5);
			}
			else if (i == 1)
			{
				currColumn.setPreferredWidth(tableWidthEntity * 3);
			}
			else
			{
				currColumn.setPreferredWidth(tableWidthEntity * 2);
			}
		}

		// This code used to be in the popup menu -------------
		theAutomatonTable.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int col = theAutomatonTable.columnAtPoint(e.getPoint());
					int row = theAutomatonTable.rowAtPoint(e.getPoint());

					if (row < 0)
					{
						return;
					}

					// Show in the panel
					/*
					if (col == TABLE_IDENTITY_COLUMN)
					{
						ActionMan.automatonView_actionPerformed(getGui());
						getGui().repaint();
					}
					*/
				}
			}

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
				if (e.isPopupTrigger())
				{
					int currRow = theAutomatonTable.rowAtPoint(e.getPoint());

					if (currRow < 0)
					{
						return;
					}

					if (!theAutomatonTable.isRowSelected(currRow))
					{
						theAutomatonTable.clearSelection();
						theAutomatonTable.setRowSelectionInterval(currRow, currRow);
					}

					// ToDo
					//mainPopupMenu.show(theAutomatonTable.getSelectedRowCount(), e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		// --------------------------------------//

	}

	private VisualProject getActiveProject()
	{
		return moduleContainer.getVisualProject();
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (!e.getValueIsAdjusting()) {}
	}

	public void tableChanged(TableModelEvent e)
	{

		theAutomatonTable.revalidate();
	}
}