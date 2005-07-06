
package org.supremica.gui.ide;

import org.supremica.gui.ide.actions.IDEAction;
import org.supremica.gui.ide.actions.IDEActionInterface;
import net.sourceforge.waters.gui.EditorWindowInterface;


import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.util.*;
import org.supremica.gui.Utility;
import org.supremica.gui.InterfaceManager;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.properties.SupremicaProperties;
import org.supremica.log.*;
import org.supremica.Version;

public class IDE
    extends JFrame
    implements ChangeListener, IDEActionInterface
{
	private static final long serialVersionUID = 1L;

	static
	{
		SupremicaProperties.setUseSecurity(false);
		SupremicaProperties.setXmlRpcActive(false);
		SupremicaProperties.setAllowSuperUserLogin(false);
		SupremicaProperties.setUseDot(true);
		SupremicaProperties.setLogToConsole(false);
		SupremicaProperties.setLogToGUI(true);
	}
	private static Logger logger = LoggerFactory.createLogger(IDE.class);
	private final static InterfaceManager interfaceManager = InterfaceManager.getInstance();

	private Actions theActions;

	private JPanel contentPanel;
	private BorderLayout contentLayout;

	private IDEMenuBar menuBar;
	private IDEToolBar ideToolBar;
	private JToolBar currToolBar = null;

	private ModuleContainers moduleContainers;

	private LogPanel logPanel;

	private JTabbedPane tabPanel;
	private JSplitPane splitPanelVertical;

	private final String ideName = "Supremica";

    public IDE()
    {
		Utility.setupFrame(this, IDEDimensions.mainWindowPreferredSize);
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

		setToolBar(createToolBar());

		tabPanel = new JTabbedPane();
		tabPanel.addChangeListener(this);

		ModuleContainer currModuleContainer = moduleContainers.getActiveModuleContainer();
		tabPanel.add(currModuleContainer.getEditorPanel());
		tabPanel.add(currModuleContainer.getAnalyzerPanel());
//		tabPanel.add(currModuleContainer.getSimulatorPanel());

		tabPanel.validate();

		logPanel = new LogPanel(this, "Logger");

		splitPanelVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabPanel, logPanel);
		splitPanelVertical.setContinuousLayout(false);
		splitPanelVertical.setOneTouchExpandable(false);
		splitPanelVertical.setDividerLocation(0.8);
		splitPanelVertical.setResizeWeight(1.0);

		contentPanel.add(splitPanelVertical, BorderLayout.CENTER);

		pack();
		validate();

		logger.info("Supremica version: " + (new Version()).toString());
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
//			tabPanel.remove(oldModuleContainer.getSimulatorPanel());

			tabPanel.add(moduleContainer.getEditorPanel());
			tabPanel.add(moduleContainer.getAnalyzerPanel());
//			tabPanel.add(moduleContainer.getSimulatorPanel());

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

	public IDE getIDE()
	{
		return this;
	}
	private void setToolBar(JToolBar toolBar)
	{
		if (toolBar == null)
		{
			return;
		}
		if (toolBar == currToolBar)
		{
			return;
		}
		if (currToolBar != null)
		{
			contentPanel.remove(currToolBar);
		}
    	contentPanel.add(toolBar, BorderLayout.NORTH);
    	currToolBar = toolBar;
	}

	private IDEToolBar createToolBar()
	{
    	ideToolBar = new IDEToolBar(this);

		// Set standard actions
		ideToolBar.add(getActions().newAction);
		ideToolBar.add(getActions().openAction);
		ideToolBar.add(getActions().saveAction);

		getActiveModuleContainer().getAnalyzerPanel().addToolBarEntries(ideToolBar);
		getActiveModuleContainer().getEditorPanel().addToolBarEntries(ideToolBar);
		return ideToolBar;
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

	public void stateChanged(ChangeEvent e)
	{
		Component currTab = tabPanel.getSelectedComponent();
		if (currTab == getActiveModuleContainer().getAnalyzerPanel())
		{
//			setToolBar(getActiveModuleContainer().getAnalyzerPanel().getToolBar(ideToolBar));
			getActiveModuleContainer().updateAutomata();
			getActiveModuleContainer().getEditorPanel().disablePanel();
			getActiveModuleContainer().getAnalyzerPanel().enablePanel();
		}
		if (currTab == getActiveModuleContainer().getEditorPanel())
		{
//			setToolBar(getActiveModuleContainer().getEditorPanel().getToolBar(ideToolBar));
			getActiveModuleContainer().getEditorPanel().enablePanel();
			getActiveModuleContainer().getAnalyzerPanel().disablePanel();
		}
		validate();
		repaint();
	}

	public void setEditorMode(IDEAction theAction)
	{
	    ideToolBar.setCommand((String)theAction.getValue(Action.ACTION_COMMAND_KEY));
	}

	public EditorWindowInterface getActiveEditorWindowInterface()
	{
		return getActiveModuleContainer().getActiveEditorWindowInterface();
	}

	public static void main(String args[])
	{
		SupremicaProperties.loadProperties(args);

		IDE ide = new IDE();
		ide.setVisible(true);


	}

}
