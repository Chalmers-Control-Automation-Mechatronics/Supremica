
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
 * Haradsgatan 26A
 * 431 42 Molndal
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
package org.supremica.gui;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import javax.swing.table.*;
import org.supremica.automata.*;
import org.supremica.log.*;
import org.supremica.gui.editor.*;

/**
 * VisualProject is responsible for keeping track of all windows and other "visual" resources
 * that are associated with a project.
 */
public class VisualProject
	extends Project
{
	private static Logger logger = LoggerFactory.createLogger(VisualProject.class);
	private Automata selectedAutomata = null;
	private AutomataEditor theAutomataEditor = null;    // Lazy construction
	private HashMap theAutomatonViewerContainer = new HashMap();
	private HashMap theAutomatonExplorerContainer = new HashMap();
	private HashMap theAutomatonFrameContainer = new HashMap();
	private HashMap theAutomatonDocumentContainer = new HashMap();
	private HashMap theAlphabetViewerContainer = new HashMap();
	private LightTableModel lightTableModel = new LightTableModel();
	private FullTableModel fullTableModel = new FullTableModel();
	private File projectFile = null;

	public VisualProject()
	{
		initialize();
	}

	public VisualProject(String name)
	{
		super(name);

		initialize();
	}

	private void initialize()
	{
		addListener(lightTableModel);
		addListener(fullTableModel);
	}

	public void setSelectedAutomata(Automata theAutomata)
	{
		this.selectedAutomata = theAutomata;
	}

	public Automata getSelectedAutomata()
	{
		return selectedAutomata;
	}

	public void clearSelection()
	{
		selectedAutomata = null;
	}

	public synchronized AutomataEditor getAutomataEditor()
	{
		if (theAutomataEditor == null)
		{
			theAutomataEditor = new AutomataEditor(this);

			theAutomataEditor.setVisible(true);

			return theAutomataEditor;
		}
		else
		{
			theAutomataEditor.setVisible(true);

			return theAutomataEditor;
		}
	}

	public void updateFrameTitles()
	{
		String title = "Supremica - " + getName();

		if (theAutomataEditor != null)
		{
			theAutomataEditor.setTitle(title);
		}
	}

	public File getProjectFile()
	{
		return projectFile;
	}

	public void setProjectFile(File projectFile)
	{
		this.projectFile = projectFile;
	}

	public AutomatonViewer getAutomatonViewer(String automatonName)
		throws Exception
	{
		if (theAutomatonViewerContainer.containsKey(automatonName))
		{
			AutomatonViewer viewer = (AutomatonViewer) theAutomatonViewerContainer.get(automatonName);

			viewer.setVisible(true);
			viewer.setState(Frame.NORMAL);

			return viewer;
		}
		else
		{
			Automaton currAutomaton = getAutomaton(automatonName);

			if (currAutomaton != null)
			{
				try
				{
					AutomatonViewer viewer = new AutomatonViewer(currAutomaton);

					theAutomatonViewerContainer.put(automatonName, viewer);
					viewer.setVisible(true);

					return viewer;
				}
				catch (Exception ex)
				{
					throw new Exception("Error while viewing: " + automatonName);
				}
			}
			else
			{
				throw new Exception(automatonName + " does not exist in VisualProjectContainer");
			}
		}
	}

	public JInternalFrame getAutomatonFrame(String automatonName)
		throws Exception
	{
		if (theAutomatonFrameContainer.containsKey(automatonName))
		{
			JInternalFrame theFrame = (JInternalFrame) theAutomatonFrameContainer.get(automatonName);

			theFrame.setVisible(true);

			return theFrame;
		}
		else
		{
			AutomatonDocument currDocument = getAutomatonDocument(automatonName);

			if (currDocument == null)
			{
				return null;
			}

			if (theAutomataEditor == null)
			{
				return null;
			}

			JInternalFrame theFrame = theAutomataEditor.createFrame(currDocument);

			theAutomatonFrameContainer.put(automatonName, theFrame);
			theFrame.setVisible(true);

			return theFrame;
		}
	}

	public AutomatonDocument getAutomatonDocument(String automatonName)
		throws Exception
	{
		if (theAutomatonDocumentContainer.containsKey(automatonName))
		{
			AutomatonDocument document = (AutomatonDocument) theAutomatonDocumentContainer.get(automatonName);

			return document;
		}

		return null;

		/*
										else
										{
														Automaton currAutomaton = getAutomaton(automatonName);

														if (currAutomaton != null)
														{
																		try
																		{
																						AutomatonDocument document = new AutomatonDocument(this, currAutomaton);

																						theAutomatonDocumentContainer.put(automaton, document);

																						return document;
																		}
																		catch (Exception ex)
																		{
																						throw new Exception("Error while viewing: " + automaton);
																		}
														}
														else
														{
																		throw new Exception(automaton + " does not exist in VisualProjectContainer");
														}
										}
		*/
	}

	public AutomatonExplorer getAutomatonExplorer(String automaton)
		throws Exception
	{
		if (theAutomatonExplorerContainer.containsKey(automaton))
		{
			AutomatonExplorer explorer = (AutomatonExplorer) theAutomatonExplorerContainer.get(automaton);

			explorer.setVisible(true);

			return explorer;
		}
		else
		{
			Automaton currAutomaton = getAutomaton(automaton);

			if (currAutomaton != null)
			{
				try
				{
					AutomatonExplorer explorer = new AutomatonExplorer(currAutomaton);

					theAutomatonExplorerContainer.put(automaton, explorer);
					explorer.setVisible(true);
					explorer.initialize();

					return explorer;
				}
				catch (Exception ex)
				{
					throw new Exception("Error while exploring: " + automaton);
				}
			}
			else
			{
				throw new Exception(automaton + " does not exist in VisualProjectContainer");
			}
		}
	}

	public AlphabetViewer getAlphabetViewer(String automaton)
		throws Exception
	{
		if (theAlphabetViewerContainer.containsKey(automaton))
		{
			AlphabetViewer viewer = (AlphabetViewer) theAlphabetViewerContainer.get(automaton);

			viewer.setVisible(true);

			return viewer;
		}
		else
		{
			Automaton currAutomaton = getAutomaton(automaton);

			if (currAutomaton != null)
			{
				try
				{
					AlphabetViewer viewer = new AlphabetViewer(currAutomaton);

					theAlphabetViewerContainer.put(automaton, viewer);
					viewer.setVisible(true);
					viewer.initialize();

					return viewer;
				}
				catch (Exception ex)
				{
					throw new Exception("Error while viewing: " + automaton);
				}
			}
			else
			{
				throw new Exception(automaton + " does not exist in VisualProjectContainer");
			}
		}
	}

	public int getSize()
	{
		return lightTableModel.getRowCount();
	}

	public TableModel getLightTableModel()
	{
		return lightTableModel;
	}

	public TableModel getFullTableModel()
	{
		return fullTableModel;
	}

	private class LightTableModel
		implements TableModel, AutomataListener
	{
		private LinkedList listeners = new LinkedList();

		public LightTableModel() {}

		public void addTableModelListener(TableModelListener l)
		{
			listeners.addLast(l);
		}

		public void removeTableModelListener(TableModelListener l)
		{
			listeners.remove(l);
		}

		public int getColumnCount()
		{
			return 1;
		}

		public String getColumnName(int columnIndex)
		{
			if (columnIndex == 0)
			{
				return "Automata";
			}

			return "Unknown";
		}

		public Class getColumnClass(int column)
		{
			if (column == 0)
			{
				return String.class;
			}

			return String.class;
		}

		public int getRowCount()
		{
			return getNbrOfAutomata();
		}

		public int getSize()
		{
			return getRowCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			Automaton theAutomaton = getAutomatonAt(rowIndex);

			if (columnIndex == 0)
			{
				return theAutomaton.getName();
			}

			return "Unknown";
		}

		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

		public void updateListeners()
		{

			// logger.debug("LightTableModel.updateListeners");
			Iterator theIt = listeners.iterator();
			TableModelEvent event = new TableModelEvent(this, 0, getNbrOfAutomata() - 1);

			while (theIt.hasNext())
			{
				TableModelListener theListener = (TableModelListener) theIt.next();

				theListener.tableChanged(event);
			}
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

		public void updated(Object theObject)
		{
			updateListeners();
		}
	}

	private class FullTableModel
		implements TableModel, AutomataListener
	{
		private LinkedList listeners = new LinkedList();

		public FullTableModel() {}

		public void addTableModelListener(TableModelListener l)
		{
			listeners.addLast(l);
		}

		public void removeTableModelListener(TableModelListener l)
		{
			listeners.remove(l);
		}

		public int getColumnCount()
		{
			return 4;
		}

		public String getColumnName(int columnIndex)
		{
			if (columnIndex == 0)
			{
				return "Automata";
			}

			if (columnIndex == 1)
			{
				return "Type";
			}

			if (columnIndex == 2)
			{
				return "Number of states";
			}

			if (columnIndex == 3)
			{
				return "Number of events";
			}

			return "Unknown";
		}

		public Class getColumnClass(int column)
		{
			if (column == 0)
			{
				return String.class;
			}

			if (column == 1)
			{
				return String.class;
			}

			if ((column == 2) || (column == 3))
			{
				return Integer.class;
			}

			return String.class;
		}

		public int getRowCount()
		{
			return getNbrOfAutomata();
		}

		public int getSize()
		{
			return getRowCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			Automaton theAutomaton = getAutomatonAt(rowIndex);

			if (columnIndex == 0)
			{
				return theAutomaton.getName();
			}

			if (columnIndex == 1)
			{
				AutomatonType currType = theAutomaton.getType();

				return currType.toString();
			}

			if (columnIndex == 2)
			{
				return new Integer(theAutomaton.nbrOfStates());
			}

			if (columnIndex == 3)
			{
				return new Integer(theAutomaton.nbrOfEvents());
			}

			return "Unknown";
		}

		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			if (columnIndex == 1)
			{
				return true;
			}

			return false;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

		public void updateListeners()
		{

			// logger.debug("LightTableModel.updateListeners");
			Iterator theIt = listeners.iterator();
			TableModelEvent event = new TableModelEvent(this, 0, getNbrOfAutomata() - 1);

			while (theIt.hasNext())
			{
				TableModelListener theListener = (TableModelListener) theIt.next();

				theListener.tableChanged(event);
			}
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

		public void updated(Object theObject)
		{
			updateListeners();
		}
	}
}
