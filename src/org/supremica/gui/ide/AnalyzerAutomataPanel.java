package org.supremica.gui.ide;

import javax.swing.*;
import java.util.*;
import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.TableSorter;
import org.supremica.gui.AutomatonViewer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.supremica.gui.VisualProject;
import org.supremica.log.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Dimension;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;


class AnalyzerAutomataPanel
	extends WhiteScrollPane
	implements TableModelListener
{
	private static Logger logger = LoggerFactory.createLogger(AnalyzerAutomataPanel.class);

	private AnalyzerPanel analyzerPanel;
	private ModuleContainer moduleContainer;
	private String name;
	private JTable theAutomatonTable;
	private TableSorter theTableSorter;
	private TableModel analyzerTableModel;
	private AnalyzerPopupMenu analyzerPopupMenu;

	public static int TABLE_IDENTITY_COLUMN = 0;
	public static int TABLE_TYPE_COLUMN = 1;
	public static int TABLE_STATES_COLUMN = 2;
	public static int TABLE_EVENTS_COLUMN = 3;
	public static int TABLE_TRANSITIONS_COLUMN = 4;

	AnalyzerAutomataPanel(AnalyzerPanel analyzerPanel, ModuleContainer moduleContainer, String name)
	{
		this.analyzerPanel = analyzerPanel;
		this.moduleContainer = moduleContainer;
		this.name = name;
		analyzerPopupMenu = new AnalyzerPopupMenu(moduleContainer.getFrame());

		setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
		setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);
		initialize();
		validate();
	}

	public String getName()
	{
		return name;
	}

	private void initialize()
	{
		analyzerTableModel = getActiveProject().getAnalyzerTableModel();
		theTableSorter = new TableSorter(analyzerTableModel);
		theAutomatonTable = new JTable(theTableSorter);

		theAutomatonTable.getTableHeader().setReorderingAllowed(false);

		getViewport().add(theAutomatonTable);

		theTableSorter.addMouseListenerToHeaderInTable(theAutomatonTable);

		analyzerTableModel.addTableModelListener(this);
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
		int tableWidthEntity = tableWidth / 11;
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
				currColumn.setPreferredWidth(tableWidthEntity * 1);
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
					if (col == TABLE_IDENTITY_COLUMN)
					{
						Automata selectedAutomata = getSelectedAutomata();

						if (selectedAutomata.size() >= 2)
						{
							//moduleContainer.getVisualProject();

							for (Iterator autIt = selectedAutomata.iterator(); autIt.hasNext();)
							{
								Automaton currAutomaton = (Automaton) autIt.next();

								try
								{
									AutomatonViewer viewer = moduleContainer.getVisualProject().getAutomatonViewer(currAutomaton.getName());
								}
								catch (Exception ex)
								{
									logger.error("Exception in AutomatonViewer. Automaton: " + currAutomaton, ex);
									return;
								}
							}
						}
						else if (selectedAutomata.size() == 1)
						{
							Automaton selectedAutomaton = selectedAutomata.getFirstAutomaton();
							AnalyzerAutomatonViewerPanel automatonPanel = new AnalyzerAutomatonViewerPanel(moduleContainer, "Dot View", selectedAutomaton);
							analyzerPanel.setRightComponent(automatonPanel);
						}
						else
						{
							return;
						}

//						ActionMan.automatonView_actionPerformed(getGui());
//						getGui().repaint();
					}
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


					analyzerPopupMenu.show(theAutomatonTable.getSelectedRowCount(), e.getComponent(), e.getX(), e.getY());
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

	public Automata getSelectedAutomata()
	{
		int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
		Automata selectedAutomata = new Automata();

		for (int i = 0; i < selectedRowIndices.length; i++)
		{
			try
			{
				int currIndex = selectedRowIndices[i];
				int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
				Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

				selectedAutomata.addAutomaton(currAutomaton);
			}
			catch (Exception ex)
			{
				logger.error("Trying to get an automaton that does not exist. Index: " + i);
				logger.debug(ex.getStackTrace());
			}
		}

		return selectedAutomata;
	}

}