package org.supremica.gui.ide;

import java.util.*;
import java.awt.Component;
import javax.swing.JFrame;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.gui.VisualProject;
import org.supremica.automata.Project;

public class ModuleContainer
{
	private IDE ide;
	private final ModuleProxy module;

	private EditorPanel editorPanel = null;
	private AnalyzerPanel analyzerPanel = null;
	private SimulatorPanel simulatorPanel = null;
	private Component selectedComponent = null;
	private VisualProject theVisualProject = new VisualProject();
	private Map componentToPanelMap = new HashMap();

	public ModuleContainer(IDE ide, ModuleProxy module)
	{
		this.ide = ide;
		this.module = module;
		setSelectedComponent(getEditorPanel());
	}

	public String getName()
	{
		return module.getName();
	}

	public ModuleProxy getModuleProxy()
	{
		return module;
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

	public void updateAutomata()
	{
		System.err.println("updateAutomata");
		ModuleProxy currModule = getModuleProxy();
		ProjectBuildFromWaters builder = new ProjectBuildFromWaters();
		Project supremicaProject = builder.build(currModule);
		theVisualProject.clear();
		theVisualProject.addAutomata(supremicaProject);
	}


}