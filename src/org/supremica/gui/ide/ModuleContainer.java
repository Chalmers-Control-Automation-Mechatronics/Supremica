//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainer
//###########################################################################
//# $Id: ModuleContainer.java,v 1.60 2007-06-24 18:40:06 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.HTMLPrinter;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoableCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.MainPanelSwitchEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.observer.UndoRedoEvent;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.EventKind;

public class ModuleContainer 
    extends DocumentContainer
    implements UndoInterface, Subject, ChangeListener
{

    //#######################################################################
    //# Constructor
    public ModuleContainer(final IDE ide, final ModuleSubject module)
    {
        super(ide, module);

        final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
        final OperatorTable optable = CompilerOperatorTable.getInstance();
        mExpressionParser = new ExpressionParser(factory, optable);
        mPrinter = new HTMLPrinter();

        mTabPanel = new JTabbedPane();
		mEditorPanel = new EditorPanel(this, "Editor");
		mAnalyzerPanel = new AnalyzerPanel(this, "Analyzer");
        mTabPanel.add(mEditorPanel);
        mTabPanel.add(mAnalyzerPanel);
        mTabPanel.addChangeListener(this);
		mEditorPanel.showComment();
    }
    

    //#######################################################################
    //# Overrides for Abstract Base Class
	//# org.supremica.gui.ide.DocumentContainer
    public Component getPanel()
	{
		return mTabPanel;
	}

    public EditorPanel getEditorPanel()
	{
		return mEditorPanel;
	}

    public AnalyzerPanel getAnalyzerPanel()
	{
		return mAnalyzerPanel;
	}

	public boolean isEditorActive()
	{
		return mTabPanel.getSelectedComponent() == mEditorPanel;
	}

	public boolean isAnalyzerActive()
	{
		return mTabPanel.getSelectedComponent() == mAnalyzerPanel;
	}


    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Subject
    public void attach(final Observer o)
    {
        mObservers.add(o);
    }
    
    public void detach(final Observer o)
    {
        mObservers.remove(o);
    }
    
    public void fireEditorChangedEvent(final EditorChangedEvent event)
    {
		// Just in case they try to register or deregister observers
		// in response to the update ...
		final Collection<Observer> copy = new LinkedList<Observer>(mObservers);
        for (final Observer observer : copy) {
            observer.update(event);
        }
		getIDE().fireEditorChangedEvent(event);
    }
    

    //#######################################################################
    //# Simple Access
    public ModuleSubject getModule()
    {
        return (ModuleSubject) getDocument();
    }
    
    public ExpressionParser getExpressionParser()
    {
        return mExpressionParser;
    }
    
    public EventKind guessEventKind(final IdentifierProxy ident)
    {
        final String name = ident.getName();
        final IndexedList<EventDeclSubject> decls =
            getModule().getEventDeclListModifiable();
        final EventDeclSubject decl = decls.get(name);
		return decl == null ? null : decl.getKind();
    }
    
    public void addStandardPropositions()
    {
        EventDeclSubject accepting = new EventDeclSubject(EventDeclProxy.DEFAULT_MARKING_NAME,
            EventKind.PROPOSITION);
        if (!getModule().getEventDeclListModifiable().containsName(EventDeclProxy.DEFAULT_MARKING_NAME))
        {
            getModule().getEventDeclListModifiable().add(accepting);
        }
        EventDeclSubject forbidden = new EventDeclSubject(EventDeclProxy.DEFAULT_FORBIDDEN_NAME,
            EventKind.PROPOSITION);
        if (!getModule().getEventDeclListModifiable().containsName(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
        {
            getModule().getEventDeclListModifiable().add(forbidden);
        }
    }
    
    public ProxyPrinter getPrinter()
    {
        return mPrinter;
    }
    
    public JToolBar getEditorToolBar(JToolBar mainToolBar)
    {
        return getEditorPanel().getToolBar(mainToolBar);
    }
    
    public JToolBar getAnalyzerToolBar(JToolBar mainToolBar)
    {
        return getAnalyzerPanel().getToolBar(mainToolBar);
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
        ComponentEditorPanel panel = mComponentToPanelMap.get(scp);
        if (panel == null)
        {
            final EditorPanel editorPanel = getEditorPanel();
            final JComponent right = editorPanel.getRightComponent();
            final Dimension oldsize = right.getSize();
            try
            {
                panel =	new ComponentEditorPanel(this, scp, oldsize);
                mComponentToPanelMap.put(scp, panel);
            }
            catch (GeometryAbsentException g)
            {
                JOptionPane.showMessageDialog(getIDE(), g.getMessage());
            }
        }
        return panel;
    }
    
    public ComponentViewPanel getComponentViewPanel
        (final SimpleComponentSubject scp)
    {
        ComponentViewPanel panel = mComponentToViewPanelMap.get(scp);
        if (panel == null)
        {
            final AnalyzerPanel analyzerPanel = getAnalyzerPanel();
            final JComponent right = analyzerPanel.getRightComponent();
            final Dimension oldsize = right.getSize();
            try
            {
                panel =	new ComponentViewPanel(this, scp, oldsize);
                mComponentToViewPanelMap.put(scp, panel);
            }
            catch (GeometryAbsentException g)
            {
                JOptionPane.showMessageDialog(getIDE(), g.getMessage());
            }
        }
        return panel;
    }
    
    public ComponentViewPanel getComponentViewPanel(String name)
    {
        List<Proxy> components = getModule().getComponentList();
        for (Proxy proxy : components)
        {
            if (proxy instanceof NamedProxy)
            {
                NamedProxy namedProxy = (NamedProxy)proxy;
                if (name.equals(namedProxy.getName()))
                {
                    if (proxy instanceof SimpleComponentSubject)
                    {
                        return getComponentViewPanel(((SimpleComponentSubject)proxy));
                    }
                    else
                    {
                        System.err.println("ModuleContainer.getComponentViewPanel proxy: " + name + " not a SimpleComponentSubject");
                        return null;
                    }
                }
            }
        }
        
        return null;
    }
    
    public JFrame getFrame()
    {
        return getIDE().getFrame();
    }

    public EditorWindowInterface getActiveEditorWindowInterface()
    {
        return getEditorPanel().getActiveEditorWindowInterface();
    }


    //#######################################################################
    //# Undo & Redo
    public void addUndoable(UndoableEdit e)
    {
        if (e.isSignificant()) {
            mInsignificant.end();
            mUndoManager.addEdit(mInsignificant);
            mInsignificant = new CompoundEdit();
            mUndoManager.addEdit(e);
			fireUndoRedoEvent();
        } else {
            mInsignificant.addEdit(e);
        }
    }
    
    public void executeCommand(Command c)
    {
        c.execute();
        addUndoable(new UndoableCommand(c));
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
 		fireUndoRedoEvent();
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
        mInsignificant.end();
        mInsignificant.undo();
        mInsignificant = new CompoundEdit();
        mUndoManager.redo();
 		fireUndoRedoEvent();
   }
    
    public void undo() throws CannotUndoException
    {
        mInsignificant.end();
        mInsignificant.undo();
        mInsignificant = new CompoundEdit();
        mUndoManager.undo();
		fireUndoRedoEvent();
    }

	private void fireUndoRedoEvent()
	{
		final EditorChangedEvent event = new UndoRedoEvent(this);
		fireEditorChangedEvent(event);
	}


	//#######################################################################
	//# Interface javax.swing.event.ChangeListener
    public void stateChanged(final ChangeEvent event)
    {
		final Component selected = mTabPanel.getSelectedComponent();
        if (selected == mAnalyzerPanel &&
			!mAnalyzerPanel.updateAutomata()) {
			mTabPanel.setSelectedComponent(mEditorPanel);
        }
		final EditorChangedEvent eevent = new MainPanelSwitchEvent(this);
		fireEditorChangedEvent(eevent);
    }


    //#######################################################################
    //# Data Members
	private final JTabbedPane mTabPanel;
    private final EditorPanel mEditorPanel;
    private final AnalyzerPanel mAnalyzerPanel;
    private SimulatorPanel simulatorPanel = null;

    private final Map<SimpleComponentSubject,ComponentEditorPanel>
        mComponentToPanelMap =
        new HashMap<SimpleComponentSubject,ComponentEditorPanel>();
    private final Map<SimpleComponentSubject,ComponentViewPanel>
        mComponentToViewPanelMap =
        new HashMap<SimpleComponentSubject,ComponentViewPanel>();

    private final ExpressionParser mExpressionParser;
    private final ProxyPrinter mPrinter;

    private final UndoManager mUndoManager = new UndoManager();
    private CompoundEdit mInsignificant = new CompoundEdit();
    private final Collection<Observer> mObservers = new LinkedList<Observer>();

}
