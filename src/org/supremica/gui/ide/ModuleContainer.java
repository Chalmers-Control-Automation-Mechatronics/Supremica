package org.supremica.gui.ide;

import java.util.*;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.gui.VisualProject;
import org.supremica.automata.Project;
import org.supremica.gui.ide.actions.Actions;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoInterface;

public class ModuleContainer implements UndoInterface
{
	private IDE ide;
	private final ModuleProxy module;

	private EditorPanel editorPanel = null;
	private AnalyzerPanel analyzerPanel = null;
	private SimulatorPanel simulatorPanel = null;
	private Component selectedComponent = null;
	private VisualProject theVisualProject = new VisualProject();
	private Map componentToPanelMap = new HashMap();
    private final UndoManager mUndoManager = new UndoManager();
  
	public ModuleContainer(IDE ide, ModuleProxy module)
	{
		this.ide = ide;
		this.module = module;
		setSelectedComponent(getEditorPanel());
	}
    
        public IDE getIDE() 
        {
	    return ide;
	}

	public String getName()
	{
		return module.getName();
	}

	public ModuleProxy getModuleProxy()
	{
		return module;
	}

	public JToolBar getEditorToolBar(JToolBar mainToolBar)
	{
		return getEditorPanel().getToolBar(mainToolBar);
	}

	public JToolBar getAnalyzerPanel(JToolBar mainToolBar)
	{
		return getAnalyzerPanel().getToolBar(mainToolBar);
	}

	public Actions getActions()
	{
		return ide.getActions();
	}

	public EditorPanel getEditorPanel()
	{
		if (editorPanel == null)
		{
			editorPanel = new EditorPanel(this, "Editor");
		}
		return editorPanel;
	}

	public AnalyzerPanel getAnalyzerPanel()
	{
		if (analyzerPanel == null)
		{
			analyzerPanel = new AnalyzerPanel(this, "Analyzer");
		}
		return analyzerPanel;
	}

	public SimulatorPanel getSimulatorPanel()
	{
		if (simulatorPanel == null)
		{
			simulatorPanel = new SimulatorPanel(this, "Simulator");
		}
		return simulatorPanel;
	}

	public ComponentEditorPanel getComponentEditorPanel(SimpleComponentProxy scp)
	{
		if (scp == null)
		{
			return null;
		}
		if (!componentToPanelMap.containsKey(scp))
		{
			ComponentEditorPanel newPanel = new ComponentEditorPanel(this, scp);
			componentToPanelMap.put(scp, newPanel);
		}
		return (ComponentEditorPanel)componentToPanelMap.get(scp);
	}

	public void setSelectedComponent(Component selectedComponent)
	{
		this.selectedComponent = selectedComponent;
	}

	public Component getSelectedComponent()
	{
		return selectedComponent;
	}

	public JFrame getFrame()
	{
		return ide.getFrame();
	}

	public VisualProject getVisualProject()
	{
		return theVisualProject;
	}

	public EditorWindowInterface getActiveEditorWindowInterface()
	{
		return getEditorPanel().getActiveEditorWindowInterface();
	}

	public void addUndoable(AbstractUndoableEdit e)
	{
		mUndoManager.addEdit(e);
		ide.getActions().editorRedoAction.setEnabled(canRedo());
		ide.getActions().editorUndoAction.setEnabled(canUndo());
	}

	public void executeCommand(Command c)
	{
		c.execute();
		if (c instanceof AbstractUndoableEdit) {
			addUndoable((AbstractUndoableEdit)c);
		}
	}

	public boolean canRedo()
	{
		return mUndoManager.canRedo();
	}

	public boolean canUndo()
	{
		return mUndoManager.canUndo();
	}

	public void redo() throws CannotRedoException
	{
		mUndoManager.redo();
		ide.getActions().editorRedoAction.setEnabled(canRedo());
		ide.getActions().editorUndoAction.setEnabled(canUndo());
	}

	public void undo() throws CannotUndoException
	{
		mUndoManager.undo();
		ide.getActions().editorRedoAction.setEnabled(canRedo());
		ide.getActions().editorUndoAction.setEnabled(canUndo());
	}

	public void updateAutomata()
	{
		ModuleProxy currModule = getModuleProxy();
		ProjectBuildFromWaters builder = new ProjectBuildFromWaters();
		Project supremicaProject = builder.build(currModule);
		theVisualProject.clear();
		theVisualProject.addAutomata(supremicaProject);
	}


}
