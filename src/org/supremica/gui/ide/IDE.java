
package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.util.*;
import org.supremica.gui.Utility;
import org.supremica.gui.InterfaceManager;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.automata.IO.FileFormats;

public class IDE
    extends JFrame
{
	private final static InterfaceManager interfaceManager = InterfaceManager.getInstance();

	private Actions theActions;

	private JPanel contentPanel;
	private BorderLayout contentLayout;

	private IDEMenuBar menuBar;
	private IDEToolBar toolBar;

	private ModuleContainers moduleContainers;

	private LogPanel logPanel;

	private JTabbedPane tabPanel;
	private JSplitPane splitPanelVertical;

	private final String ideName = "Supremica with Waters";

    public IDE()
    {
		Utility.setupFrame(this, 900, 700);
		setTitle(getName());
		moduleContainers = new ModuleContainers(this);
		ModuleContainer defaultModule = createNewModuleContainer();
		moduleContainers.add(defaultModule);
		moduleContainers.setActive(defaultModule);

		contentPanel = (JPanel)getContentPane();
		contentLayout = new BorderLayout();
		contentPanel.setLayout(contentLayout);

		theActions = new Actions(this);

    	menuBar = new IDEMenuBar(this);
    	setJMenuBar(menuBar);

    	toolBar = new IDEToolBar(this);
    	contentPanel.add(toolBar, BorderLayout.NORTH);

		tabPanel = new JTabbedPane();

		ModuleContainer currModuleContainer = moduleContainers.getActiveModuleContainer();
		tabPanel.add(currModuleContainer.getEditorPanel());
		tabPanel.add(currModuleContainer.getAnalyzerPanel());
		tabPanel.add(currModuleContainer.getSimulatorPanel());

		logPanel = new LogPanel(this, "Logger");

		splitPanelVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabPanel, logPanel);
		splitPanelVertical.setContinuousLayout(false);
		splitPanelVertical.setOneTouchExpandable(false);
		splitPanelVertical.setDividerLocation(0.8);
		splitPanelVertical.setResizeWeight(1.0);

		contentPanel.add(splitPanelVertical, BorderLayout.CENTER);

//		pack();

    }

	public Actions getActions()
	{
		return theActions;
	}

	public Iterator moduleContainerIterator()
	{
		return moduleContainers.iterator();
	}

	public void add(ModuleContainer moduleContainer)
	{
		moduleContainers.add(moduleContainer);
	}

	public void remove(ModuleContainer moduleContainer)
	{
		ModuleContainer activeModuleContainer = getActiveModuleContainer();
		ModuleContainer nextModuleContainer = null;

		if (activeModuleContainer == moduleContainer)
		{
			if (moduleContainers.size() <= 1)
			{
				nextModuleContainer = createNewModuleContainer();
				moduleContainers.add(nextModuleContainer);
			}
			else
			{
				nextModuleContainer = moduleContainers.getNext(moduleContainer);
			}
		}
		setActive(nextModuleContainer);
		moduleContainers.remove(moduleContainer);
	}

	public ModuleContainer getActiveModuleContainer()
	{
		return moduleContainers.getActiveModuleContainer();
	}

	public void setActive(ModuleContainer moduleContainer)
	{
		ModuleContainer oldModuleContainer = getActiveModuleContainer();
		if (moduleContainer != oldModuleContainer)
		{
			moduleContainers.setActive(moduleContainer);

			oldModuleContainer.setSelectedComponent(tabPanel.getSelectedComponent());

			tabPanel.remove(oldModuleContainer.getEditorPanel());
			tabPanel.remove(oldModuleContainer.getAnalyzerPanel());
			tabPanel.remove(oldModuleContainer.getSimulatorPanel());

			tabPanel.add(moduleContainer.getEditorPanel());
			tabPanel.add(moduleContainer.getAnalyzerPanel());
			tabPanel.add(moduleContainer.getSimulatorPanel());

			tabPanel.setSelectedComponent(moduleContainer.getSelectedComponent());
		}
	}

	public String getName()
	{
		return ideName;
	}

	public ModuleContainer createNewModuleContainer()
	{
		return moduleContainers.createNewModuleContainer();
	}

	public JFrame getFrame()
	{
		return this;
	}

	// Overridden so we can exit when window is closed
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			getActions().exitAction.doAction();
		}
	}


	public static void main(String args[])
	{
		IDE ide = new IDE();
		ide.setVisible(true);
	}

}
