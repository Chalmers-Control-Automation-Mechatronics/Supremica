
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
package org.supremica.automata;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import javax.swing.table.*;
import java.io.File;
import org.supremica.gui.*;
import org.supremica.gui.editor.*;

public class AutomatonContainer
	implements AutomatonListener
{
	private ArrayList theAutomatonNames = null;
	private HashMap theAutomatonContainer = null;
	private HashMap theAutomatonViewerContainer = null;
	private HashMap theAutomatonExplorerContainer = null;
	private HashMap theAutomatonFrameContainer = null;
	private HashMap theAutomatonDocumentContainer = null;
	private HashMap theAlphabetViewerContainer = null;
	private AutomataEditor theAutomataEditor = null;
	private AutomatonContainerListeners automataListeners = null;
	private Supremica workbench = null;
	private LightTableModel lightTableModel = new LightTableModel();
	private FullTableModel fullTableModel = new FullTableModel();
	private String projectName = "Untitled";
	private File projectFile = null;

	public AutomatonContainer()
	{
		theAutomatonNames = new ArrayList();
		theAutomatonContainer = new HashMap();

		initializeGUIContainers();

		this.workbench = null;
	}

	/**
	 * This make a shallow copy of the other container. No copies of
	 * automata or windows are made.
	 **/
	public AutomatonContainer(AutomatonContainer other)
	{
		theAutomatonNames = new ArrayList(other.theAutomatonNames);
		theAutomatonContainer = new HashMap(other.theAutomatonContainer);

		initializeGUIContainers();

		this.workbench = other.workbench;
	}

	public AutomatonContainer(Supremica workbench)
	{
		theAutomatonNames = new ArrayList();
		theAutomatonContainer = new HashMap();

		initializeGUIContainers();

		this.workbench = workbench;
	}

	private void initializeGUIContainers()
	{
		theAutomatonViewerContainer = new HashMap();
		theAutomatonExplorerContainer = new HashMap();
		theAutomatonFrameContainer = new HashMap();
		theAutomatonDocumentContainer = new HashMap();
		theAlphabetViewerContainer = new HashMap();
	}

	public void setProjectName(String projectName)
	{
		this.projectName = projectName;

		updateFrameTitles();
	}

	public String getProjectName()
	{
		return projectName;
	}

	public void updateFrameTitles()
	{
		String title = "Supremica - " + getProjectName();

		if (workbench != null)
		{
			workbench.setTitle(title);
		}

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

	private void updateListeners()
	{
		lightTableModel.updateListeners();
		fullTableModel.updateListeners();
	}

	public void add(Automaton automaton)
		throws Exception
	{
		String name = automaton.getName();

		if (containsAutomaton(name))
		{
			throw new Exception(name + " does already exist.");
		}

		add(automaton, name, false);
		automaton.getListeners().addListener(this);
		update();
		notifyListeners(AutomatonContainerListeners.MODE_AUTOMATON_ADDED, automaton);
	}

	public void add(Automata automata)
		throws Exception
	{
		for (Iterator autIt = automata.iterator(); autIt.hasNext(); )
		{
			add((Automaton) autIt.next());
		}
	}

	public void remove(String automatonName)
		throws Exception
	{
		if (!containsAutomaton(automatonName))
		{
			throw new Exception(automatonName + " does not exist.");
		}

		Automaton theAutomaton = (Automaton) theAutomatonContainer.get(automatonName);

		theAutomaton.getListeners().removeListener(this);
		remove(automatonName, false);
		update();
		notifyListeners(AutomatonContainerListeners.MODE_AUTOMATON_REMOVED, theAutomaton);
	}

	public void rename(Automaton automaton, String newName)
		throws Exception
	{
		String name = automaton.getName();

		if (!containsAutomaton(name))
		{
			throw new Exception(name + " does not exist.");
		}

		if (containsAutomaton(newName))
		{
			throw new Exception(newName + " does already exist.");
		}

		remove(automaton.getName(), true);

		int autIndex = theAutomatonNames.indexOf(automaton.getName());

		automaton.setName(newName);
		theAutomatonNames.set(autIndex, newName);
		add(automaton, automaton.getName(), true);
		update();
		notifyListeners(AutomatonContainerListeners.MODE_AUTOMATON_RENAMED, automaton);
	}

	public void renamed(Automaton automaton, String oldName)
		throws Exception
	{
		if (!containsAutomaton(oldName))
		{
			throw new Exception(oldName + " does not exist.");
		}

		if (containsAutomaton(automaton.getName()))
		{
			throw new Exception(automaton.getName() + " does already exist.");
		}

		remove(oldName, true);

		int autIndex = theAutomatonNames.indexOf(oldName);

		theAutomatonNames.set(autIndex, automaton.getName());
		add(automaton, automaton.getName(), true);
		update();
		notifyListeners(AutomatonContainerListeners.MODE_AUTOMATON_RENAMED, automaton);
	}

	public String getUniqueAutomatonName()
	{
		return getUniqueAutomatonName("Untitled");
	}

	public String getUniqueAutomatonName(String prefix)
	{
		if (!containsAutomaton(prefix))
		{
			return prefix;
		}

		int index = 1;
		String newName;

		do
		{
			newName = prefix + "(" + index++ + ")";
		}
		while (containsAutomaton(newName));

		return newName;
	}

	public AutomataEditor getAutomataEditor()
	{
		if (workbench == null)
		{
			return null;
		}

		if (theAutomataEditor == null)
		{
			theAutomataEditor = new AutomataEditor(workbench);

			theAutomataEditor.setVisible(true);
			updateFrameTitles();

			return theAutomataEditor;
		}
		else
		{
			theAutomataEditor.setVisible(true);

			return theAutomataEditor;
		}
	}

	public AutomatonViewer getAutomatonViewer(String automaton)
		throws Exception
	{
		if (theAutomatonViewerContainer.containsKey(automaton))
		{
			AutomatonViewer viewer = (AutomatonViewer) theAutomatonViewerContainer.get(automaton);

			viewer.setVisible(true);
			viewer.setState(Frame.NORMAL);

			return viewer;
		}
		else
		{
			Automaton currAutomaton = (Automaton) theAutomatonContainer.get(automaton);

			if (currAutomaton != null)
			{
				try
				{
					AutomatonViewer viewer = new AutomatonViewer(currAutomaton);

					theAutomatonViewerContainer.put(automaton, viewer);

					// addAutomatonListener(currAutomaton, viewer);
					viewer.setVisible(true);

					// viewer.initialize();
					return viewer;
				}
				catch (Exception ex)
				{
					throw new Exception("Error while viewing: " + automaton);
				}
			}
			else
			{
				throw new Exception(automaton + " does not exist in AutomatonContainer");
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

	public AutomatonDocument getAutomatonDocument(String automaton)
		throws Exception
	{
		if (theAutomatonDocumentContainer.containsKey(automaton))
		{
			AutomatonDocument document = (AutomatonDocument) theAutomatonDocumentContainer.get(automaton);

			return document;
		}
		else
		{
			Automaton currAutomaton = (Automaton) theAutomatonContainer.get(automaton);

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
				throw new Exception(automaton + " does not exist in AutomatonContainer");
			}
		}
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
			Automaton currAutomaton = (Automaton) theAutomatonContainer.get(automaton);

			if (currAutomaton != null)
			{
				try
				{
					AutomatonExplorer explorer = new AutomatonExplorer(currAutomaton);

					theAutomatonExplorerContainer.put(automaton, explorer);

					// addAutomatonListener(currAutomaton, explorer);
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
				throw new Exception(automaton + " does not exist in AutomatonContainer");
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
			Automaton currAutomaton = (Automaton) theAutomatonContainer.get(automaton);

			if (currAutomaton != null)
			{
				try
				{
					AlphabetViewer viewer = new AlphabetViewer(currAutomaton);

					theAlphabetViewerContainer.put(automaton, viewer);

					// addAutomatonListener(currAutomaton, viewer);
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
				throw new Exception(automaton + " does not exist in AutomatonContainer");
			}
		}
	}

	public boolean containsAutomaton(String automaton)
	{
		return theAutomatonNames.contains(automaton);
	}

	public Automaton getAutomaton(String automatonName)
		throws Exception
	{
		if (!containsAutomaton(automatonName))
		{
			throw new Exception(automatonName + " does not exist.");
		}

		return (Automaton) theAutomatonContainer.get(automatonName);
	}

	public Automaton getAutomatonAt(int index)
		throws Exception
	{
		String automatonName = (String) theAutomatonNames.get(index);

		return getAutomaton(automatonName);
	}

	public Automata getAutomata()
	{
		Automata theAutomata = new Automata();

		if (projectName != null)
		{
			theAutomata.setName(projectName);
		}

		Collection theAutomataCollection = theAutomatonContainer.values();
		Iterator autIt = theAutomataCollection.iterator();

		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			theAutomata.addAutomaton(currAutomaton);
		}

		return theAutomata;
	}

	/**
	 * Returns an iterator over all automata names.
	 */
	public Iterator automatonIterator()
	{
		return theAutomatonNames.iterator();
	}

	public void clear()
	{
		ArrayList aCopy = new ArrayList(theAutomatonNames);
		Iterator autIt = aCopy.iterator();

		while (autIt.hasNext())
		{
			String name = (String) autIt.next();

			try
			{
				remove(name, false);
			}
			catch (Exception e)
			{    // do nothing - maybe send an error to the log file
			}
		}

		update();
	}

	public void remove(String automatonName, boolean rename)
		throws Exception
	{
		if (!rename)
		{
			theAutomatonNames.remove(automatonName);
		}

		theAutomatonContainer.remove(automatonName);

		// theAutomatonListeners.remove(automaton);
		// Clear automaton viewer, if it exists
		AutomatonViewer automatonViewer = (AutomatonViewer) theAutomatonViewerContainer.get(automatonName);

		if (automatonViewer != null)
		{
			automatonViewer.setVisible(false);
			automatonViewer.dispose();
			theAutomatonViewerContainer.remove(automatonName);
		}

		// Clear automaton frame, if it exists
		JInternalFrame automatonFrame = (JInternalFrame) theAutomatonFrameContainer.get(automatonName);

		if (automatonFrame != null)
		{
			automatonFrame.setVisible(false);
			automatonFrame.dispose();
			theAutomatonFrameContainer.remove(automatonName);
		}

		// Clear automaton document, if it exists
		AutomatonDocument automatonDocument = (AutomatonDocument) theAutomatonDocumentContainer.get(automatonName);

		if (automatonDocument != null)
		{
			theAutomatonDocumentContainer.remove(automatonName);
		}

		// Clear automaton explorer, if it exists
		AutomatonExplorer automatonExplorer = (AutomatonExplorer) theAutomatonExplorerContainer.get(automatonName);

		if (automatonExplorer != null)
		{
			automatonExplorer.setVisible(false);
			automatonExplorer.dispose();
			theAutomatonExplorerContainer.remove(automatonName);
		}

		// Clear alphabet viewer, if it exists
		AlphabetViewer alphabetViewer = (AlphabetViewer) theAlphabetViewerContainer.get(automatonName);

		if (alphabetViewer != null)
		{
			alphabetViewer.setVisible(false);
			alphabetViewer.dispose();
			theAlphabetViewerContainer.remove(automatonName);
		}
	}

	private void add(Automaton automaton, String name, boolean rename)
		throws Exception
	{
		if (!rename)
		{
			theAutomatonNames.add(name);
		}

		theAutomatonContainer.put(name, automaton);

		// theAutomatonListeners.put(name, new LinkedList());
	}

	public void update()
	{
		updateListeners();
	}

	public AutomatonContainerListeners getListeners()
	{
		if (automataListeners == null)
		{
			automataListeners = new AutomatonContainerListeners(this);
		}

		return automataListeners;
	}

	private void notifyListeners()
	{
		if (automataListeners != null)
		{
			automataListeners.notifyListeners();
		}
	}

	private void notifyListeners(int mode, Automaton a)
	{
		if (automataListeners != null)
		{
			automataListeners.notifyListeners(mode, a);
		}
	}

	public void beginTransaction()
	{
		if (automataListeners != null)
		{
			automataListeners.beginTransaction();
		}
	}

	public void endTransaction()
	{
		if (automataListeners != null)
		{
			automataListeners.endTransaction();
		}
	}

	public void updated(Object o)
	{
		update();
	}

	public void stateAdded(Automaton aut, State q)
	{
		update();
	}

	public void stateRemoved(Automaton aut, State q)
	{
		update();
	}

	public void arcAdded(Automaton aut, Arc a)
	{    // Do nothing
	}

	public void arcRemoved(Automaton aut, Arc a)
	{    // Do nothing
	}

	public void attributeChanged(Automaton aut)
	{    // Do nothing
	}

	public void automatonRenamed(Automaton aut, String oldName)
	{
		try
		{
			renamed(aut, oldName);
		}
		catch (Exception e)
		{

			// Fix this
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
		implements TableModel
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
			return theAutomatonNames.size();
		}

		public int getSize()
		{
			return getRowCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			String name = (String) theAutomatonNames.get(rowIndex);
			Automaton theAutomaton = (Automaton) theAutomatonContainer.get(name);

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
			Iterator theIt = listeners.iterator();
			TableModelEvent event = new TableModelEvent(this, 0, theAutomatonNames.size() - 1);

			while (theIt.hasNext())
			{
				TableModelListener theListener = (TableModelListener) theIt.next();

				theListener.tableChanged(event);
			}
		}
	}

	private class FullTableModel
		implements TableModel
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
			return theAutomatonNames.size();
		}

		public int getSize()
		{
			return getRowCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			String name = (String) theAutomatonNames.get(rowIndex);
			Automaton theAutomaton = (Automaton) theAutomatonContainer.get(name);

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
			Iterator theIt = listeners.iterator();
			TableModelEvent event = new TableModelEvent(this, 0, theAutomatonNames.size() - 1);

			while (theIt.hasNext())
			{
				TableModelListener theListener = (TableModelListener) theIt.next();

				theListener.tableChanged(event);
			}
		}
	}
}
