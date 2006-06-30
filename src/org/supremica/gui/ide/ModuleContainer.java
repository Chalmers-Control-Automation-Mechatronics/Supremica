//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainer
//###########################################################################
//# $Id: ModuleContainer.java,v 1.26 2006-06-30 15:40:17 knut Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.HTMLPrinter;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoableCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.UndoRedoEvent;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.Project;
import org.supremica.gui.VisualProject;
import org.supremica.gui.ide.actions.Actions;

import javax.swing.JOptionPane;


public class ModuleContainer implements UndoInterface
{

	//#######################################################################
	//# Constructor
	public ModuleContainer(final IDE ide, final ModuleSubject module)
	{
		mIDE = ide;
		mModule = module;
		final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
		final OperatorTable optable = CompilerOperatorTable.getInstance();
		mExpressionParser = new ExpressionParser(factory, optable);
		mPrinter = new HTMLPrinter();
		setSelectedComponent(getEditorPanel());
	}


	//#######################################################################
	//# Simple Access
	public IDE getIDE()
	{
	    return mIDE;
	}

	public String getName()
	{
		return mModule.getName();
	}

	public ModuleSubject getModule()
	{
		return mModule;
	}

	public ExpressionParser getExpressionParser()
	{
		return mExpressionParser;
	}

	public ProxyPrinter getPrinter()
	{
		return mPrinter;
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
		return mIDE.getActions();
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

	public ComponentEditorPanel getComponentEditorPanel
		(final SimpleComponentSubject scp)
	{
		if (scp == null) {
			return null;
		} else {
			ComponentEditorPanel panel = mComponentToPanelMap.get(scp);
			if (panel == null) {
				panel =	new ComponentEditorPanel(this, scp);
				mComponentToPanelMap.put(scp, panel);
			}
			return panel;
		}
	}

	public void setSelectedComponent(Component selectedComponent)
	{
		mSelectedComponent = selectedComponent;
	}

	public Component getSelectedComponent()
	{
		return mSelectedComponent;
	}

	public JFrame getFrame()
	{
		return mIDE.getFrame();
	}

	public VisualProject getVisualProject()
	{
		return mVisualProject;
	}

	public void addComponent()
	{

	}

	//#######################################################################
	//# Undo & Redo
	public EditorWindowInterface getActiveEditorWindowInterface()
	{
		return getEditorPanel().getActiveEditorWindowInterface();
	}

	public void addUndoable(UndoableEdit e)
	{
		mUndoManager.addEdit(e);
		mIDE.getActions().editorRedoAction.setEnabled(canRedo());
		mIDE.getActions().editorUndoAction.setEnabled(canUndo());
		fireEditorChangedEvent(new UndoRedoEvent());
	}

	public void executeCommand(Command c)
	{
		c.execute();
		//if (c instanceof UndoableEdit) {
		addUndoable(new UndoableCommand(c));
		//}
	}

	public boolean canRedo()
	{
		return mUndoManager.canRedo();
	}

	public boolean canUndo()
	{
		return mUndoManager.canUndo();
	}

	public void clearList()
	{
		mUndoManager.discardAllEdits();
		fireEditorChangedEvent(new UndoRedoEvent());
	}

	public String getRedoPresentationName()
	{
		return mUndoManager.getRedoPresentationName();
	}

	public String getUndoPresentationName()
	{
	    return mUndoManager.getUndoPresentationName();
	}

	public void redo() throws CannotRedoException
	{
		mUndoManager.redo();
		mIDE.getActions().editorRedoAction.setEnabled(canRedo());
		mIDE.getActions().editorUndoAction.setEnabled(canUndo());
		fireEditorChangedEvent(new UndoRedoEvent());
	}

	public void undo() throws CannotUndoException
	{
		mUndoManager.undo();
		mIDE.getActions().editorRedoAction.setEnabled(canRedo());
		mIDE.getActions().editorUndoAction.setEnabled(canUndo());
		fireEditorChangedEvent(new UndoRedoEvent());
	}

	//#######################################################################
	//# Observer Support
	public void attach(final Observer o)
	{
		mObservers.add(o);
	}

	public void detach(final Observer o)
	{
		mObservers.remove(o);
	}

	public void fireEditorChangedEvent(EditorChangedEvent e)
	{
		for (final Observer o : mObservers) {
			o.update(e);
		}
	}

	public void updateAutomata()
	{
		try
		{
			ProjectBuildFromWaters builder = new ProjectBuildFromWaters();
			Project supremicaProject = builder.build(mModule);
			mVisualProject.clear();
			mVisualProject.addAutomata(supremicaProject);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(mIDE, ex.getMessage(), "Error in graph",
										  JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			return;
		}
	}


	//#######################################################################
	//# Data Members
	private final IDE mIDE;
	private final ModuleSubject mModule;
	private final ExpressionParser mExpressionParser;
	private final ProxyPrinter mPrinter;
	private final UndoManager mUndoManager = new UndoManager();
    private final Collection<Observer> mObservers = new LinkedList<Observer>();
	private final Map<SimpleComponentSubject,ComponentEditorPanel>
		mComponentToPanelMap =
		new HashMap<SimpleComponentSubject,ComponentEditorPanel>();

	private EditorPanel editorPanel = null;
	private AnalyzerPanel analyzerPanel = null;
	private SimulatorPanel simulatorPanel = null;
	private Component mSelectedComponent = null;
	private VisualProject mVisualProject = new VisualProject();

}
