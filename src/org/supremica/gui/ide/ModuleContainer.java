package org.supremica.gui.ide;

import java.util.*;
import javax.swing.JComponent;
import net.sourceforge.waters.model.module.ModuleProxy;

public class ModuleContainer
{
	private IDE ide;
	private final ModuleProxy module;

	private EditorPanel editorPanel = null;
	private AnalyzerPanel analyzerPanel = null;
	private SimulatorPanel simulatorPanel = null;

	private EditorEventsPanel editorEventsPanel = null;
	private EditorComponentsPanel editorComponentsPanel = null;
	private EditorAliasesPanel editorAliasesPanel = null;
	private EditorParametersPanel editorParametersPanel = null;

	public ModuleContainer(IDE ide, ModuleProxy module)
	{
		this.ide = ide;
		this.module = module;
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

}