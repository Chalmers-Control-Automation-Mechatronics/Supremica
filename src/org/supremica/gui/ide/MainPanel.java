//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   MainPanel
//###########################################################################
//# $Id: MainPanel.java,v 1.13 2006-08-09 02:53:58 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.ide.actions.Actions;


abstract class MainPanel
	extends JPanel
	implements ModuleWindowInterface
{
	private IDEToolBar thisToolBar = null;
	private IDEToolBar currParentToolBar = null;

	private GridBagConstraints constraints = new GridBagConstraints();

	private EmptyRightPanel emptyRightPanel = new EmptyRightPanel();

	private ModuleContainer moduleContainer;
	private String name;

	protected JSplitPane splitPanelHorizontal;

	public MainPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;

		setPreferredSize(IDEDimensions.mainPanelPreferredSize);
		setMinimumSize(IDEDimensions.mainPanelMinimumSize);

		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);

		constraints.gridy = 0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
	}


	//######################################################################
	//# Interface net.sourceforge.waters.gui.ModuleWindowInterface
	public ModuleSubject getModuleSubject()
	{
		return moduleContainer.getModule();
	}

	public ExpressionParser getExpressionParser()
	{
		return moduleContainer.getExpressionParser();
	}
	
	public Frame getRootWindow()
	{
		return (Frame) getTopLevelAncestor();
	}

	public EditorWindowInterface showEditor(SimpleComponentSubject component)
	{
		final EditorPanel editorPanel =
			moduleContainer.getEditorPanel();
		if (component != null) {
			editorPanel.setRightComponent
				(moduleContainer.getComponentEditorPanel(component));
		}
		return editorPanel.getActiveEditorWindowInterface();
	}


	//######################################################################
	//#
	public String getName()
	{
		return name;
	}

	public Actions getActions()
	{
		return moduleContainer.getActions();
	}

	protected GridBagConstraints getGridBagConstraints()
	{
		return constraints;
	}

	public abstract void addToolBarEntries(IDEToolBar toolbar);

	public abstract void disablePanel();

	public abstract void enablePanel();

	public void setRightComponent(JComponent newComponent)
	{
		JComponent oldComponent = getRightComponent();
		if (oldComponent != newComponent)
		{
			JScrollPane emptyRightPanel = getEmptyRightPanel();
			Dimension oldSize = emptyRightPanel.getSize();

			if (oldComponent != null)
			{
				splitPanelHorizontal.remove(oldComponent);
				oldSize = oldComponent.getSize();
			}

			if (newComponent == null || newComponent == getEmptyRightPanel())
			{
//				emptyRightPanel.setPreferredScrollableViewportSize(oldSize);
				emptyRightPanel.setPreferredSize(oldSize);
				splitPanelHorizontal.setRightComponent(emptyRightPanel);
				disablePanel();
			}
			else
			{
//				newComponent.setPreferredScrollableViewportSize(oldSize);
				newComponent.setPreferredSize(oldSize);
				splitPanelHorizontal.setRightComponent(newComponent);
				enablePanel();
			}
		}
		validate();
	}

	public JComponent getRightComponent()
	{
		return (JComponent)splitPanelHorizontal.getRightComponent();
	}

	public JScrollPane getEmptyRightPanel()
	{
		return emptyRightPanel;
	}

	public JToolBar getToolBar(JToolBar parentToolBar)
	{
		if (parentToolBar instanceof IDEToolBar)
		{
			if (parentToolBar == currParentToolBar)
			{
				return thisToolBar;
			}
			thisToolBar = new IDEToolBar((IDEToolBar)parentToolBar);

			addToolBarEntries(thisToolBar);

			currParentToolBar = (IDEToolBar)parentToolBar;
			return thisToolBar;
		}
		return null;
	}

 	class EmptyRightPanel
 		extends WhiteScrollPane
 	{
		private static final long serialVersionUID = 1L;

		public EmptyRightPanel()
		{
			setPreferredSize(IDEDimensions.rightEmptyPreferredSize);
			setMinimumSize(IDEDimensions.rightEmptyMinimumSize);
		}
	}
}
