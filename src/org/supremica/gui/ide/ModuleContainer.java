//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainer
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.command.UndoableCommand;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.MainPanelSwitchEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.observer.UndoRedoEvent;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


public class ModuleContainer
    extends DocumentContainer
    implements UndoInterface, Subject, ChangeListener, ModelObserver
{

    //#######################################################################
    //# Constructor
    public ModuleContainer(final IDE ide, final ModuleSubject module)
    {
        super(ide, module);

        mModuleContext = new ModuleContext(module);
        final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
        final OperatorTable optable = CompilerOperatorTable.getInstance();
        mExpressionParser = new ExpressionParser(factory, optable);
        mPrinter = new HTMLPrinter();

        mTabPanel = new JTabbedPane();
        mEditorPanel = new EditorPanel(this, "Editor");
        mSimulatorPanel = new SimulatorPanel(this, "Simulator");
        mAnalyzerPanel = new AnalyzerPanel(this, "Analyzer");
        mTabPanel.add(mEditorPanel);
        Config.INCLUDE_WATERS_SIMULATOR.addPropertyChangeListener(new SimulatorPropertyChangeListener());
        if (Config.INCLUDE_WATERS_SIMULATOR.isTrue())
          mTabPanel.add(mSimulatorPanel);
        mTabPanel.add(mAnalyzerPanel);
        mTabPanel.addChangeListener(this);
        mEditorPanel.showComment();

        final ListSubject<AbstractSubject> comps =
            module.getComponentListModifiable();
        comps.addModelObserver(this);

        mTabPanel.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent e)
            {
                if (mTabPanel.getSelectedComponent().getName().equals(mAnalyzerPanel.getName()))
                {
                    mAnalyzerPanel.sortAutomataByName();
                }
            }
        });
    }


    //#######################################################################
    //# Overrides for Abstract Base Class
    //# org.supremica.gui.ide.DocumentContainer
    public boolean hasUnsavedChanges()
    {
            return mUndoIndex != mUndoCheckPoint;
    }

    public void setCheckPoint()
    {
            mUndoCheckPoint = mUndoIndex;
    }

    public void close()
    {
        mEditorPanel.close();
    }

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

    public String getTypeString()
    {
        return TYPE_STRING;
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
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    /**
     * Implementation of {@link ModelObserver} interface.
     * If a component is removed from the module, and its automaton
     * is currently displayed, show the comment editor instead.
     */
    public void modelChanged(final ModelChangeEvent event)
    {
        if (event.getKind() == ModelChangeEvent.ITEM_REMOVED) {
            final Object value = event.getValue();
            if (value instanceof SimpleComponentSubject) {
                final ComponentEditorPanel panel =
                    mEditorPanel.getActiveEditorWindowInterface();
                if (panel != null && panel.getComponent() == value) {
                    mEditorPanel.showComment();
                    // Do not event try to close or dispose the graph panel:
                    // deletions can be undone!
                }
            }
        }
    }


    //#######################################################################
    //# Simple Access
    public ModuleSubject getModule()
    {
        return (ModuleSubject) getDocument();
    }

    public ModuleContext getModuleContext()
    {
        return mModuleContext;
    }

    public ExpressionParser getExpressionParser()
    {
        return mExpressionParser;
    }

    public ProxyPrinter getPrinter()
    {
        return mPrinter;
    }

    public SimulatorPanel getSimulatorPanel()
    {
        if (simulatorPanel == null)
        {
            simulatorPanel = new SimulatorPanel(this, "Simulator");
        }
        return simulatorPanel;
    }

    ComponentEditorPanel createComponentEditorPanel
        (final SimpleComponentSubject comp)
    {
        ComponentEditorPanel panel = getComponentEditorPanel(comp);
        if (panel == null)
        {
            final EditorPanel editorPanel = getEditorPanel();
            final JComponent right = editorPanel.getRightComponent();
            final Dimension oldsize = right.getSize();
            try
            {
                panel = new ComponentEditorPanel(this, comp, oldsize);
                mComponentToPanelMap.put(comp, panel);
            }
            catch (GeometryAbsentException g)
            {
                JOptionPane.showMessageDialog(getIDE(), g.getMessage());
            }
        }
        return panel;
    }

    ComponentEditorPanel getComponentEditorPanel
        (final SimpleComponentSubject comp)
    {
        return mComponentToPanelMap.get(comp);
    }

    public ComponentViewPanel getComponentViewPanel
        (final SimpleComponentSubject comp)
    {
        ComponentViewPanel panel = mComponentToViewPanelMap.get(comp);
        if (panel == null)
        {
            final AnalyzerPanel analyzerPanel = getAnalyzerPanel();
            final JComponent right = analyzerPanel.getRightComponent();
            final Dimension oldsize = right.getSize();
            try
            {
                panel = new ComponentViewPanel(this, comp, oldsize);
                mComponentToViewPanelMap.put(comp, panel);
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
    //# Interface net.sourceforge.waters.gui.command.UndoInterface
    public void executeCommand(Command c)
    {
        c.execute();
        addUndoable(new UndoableCommand(c));
    }

    public void addUndoable(UndoableEdit e)
    {
        if (e.isSignificant()) {
            mInsignificant.end();
            mUndoManager.addEdit(mInsignificant);
            mInsignificant = new CompoundEdit();
            mUndoManager.addEdit(e);
			if (mUndoIndex++ < mUndoCheckPoint) {
				mUndoCheckPoint = -1;
			}
            fireUndoRedoEvent();
        } else {
            mInsignificant.addEdit(e);
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

    public void clearList()
    {
        mUndoManager.discardAllEdits();
		mUndoIndex = 0;
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
		mUndoIndex++;
        fireUndoRedoEvent();
   }

    public void undo() throws CannotUndoException
    {
        mInsignificant.end();
        mInsignificant.undo();
        mInsignificant = new CompoundEdit();
        mUndoManager.undo();
		mUndoIndex--;
        fireUndoRedoEvent();
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
    //# Auxiliary Methods
    private void fireUndoRedoEvent()
    {
        final EditorChangedEvent event = new UndoRedoEvent(this);
        fireEditorChangedEvent(event);
    }


    //#######################################################################
    //# Inner Classes

    private class SimulatorPropertyChangeListener implements SupremicaPropertyChangeListener
    {

      public void propertyChanged(SupremicaPropertyChangeEvent event)
      {
        if (Config.INCLUDE_WATERS_SIMULATOR.isTrue())
        {
          mTabPanel.add(mSimulatorPanel, 1);
        }
        else
        {
          mTabPanel.remove(mSimulatorPanel);
        }
      }

    }

    //#######################################################################
    //# Data Members
    private final JTabbedPane mTabPanel;
    private final EditorPanel mEditorPanel;
    private final SimulatorPanel mSimulatorPanel;
    private final AnalyzerPanel mAnalyzerPanel;
    private SimulatorPanel simulatorPanel = null;

    private final Map<SimpleComponentSubject,ComponentEditorPanel>
        mComponentToPanelMap =
        new HashMap<SimpleComponentSubject,ComponentEditorPanel>();
    private final Map<SimpleComponentSubject,ComponentViewPanel>
        mComponentToViewPanelMap =
        new HashMap<SimpleComponentSubject,ComponentViewPanel>();

    private final ModuleContext mModuleContext;
    private final ExpressionParser mExpressionParser;
    private final ProxyPrinter mPrinter;

    private final UndoManager mUndoManager = new UndoManager();
    private CompoundEdit mInsignificant = new CompoundEdit();
	private int mUndoIndex = 0;
	private int mUndoCheckPoint = 0;
    private final Collection<Observer> mObservers = new LinkedList<Observer>();


    //#######################################################################
    //# Class Constants
    static final String TYPE_STRING = "Waters module";

}
