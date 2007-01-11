//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainer
//###########################################################################
//# $Id: ModuleContainer.java,v 1.50 2007-01-11 16:11:52 flordal Exp $
//###########################################################################


package org.supremica.gui.ide;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
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
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.UndoRedoEvent;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.EventParameterSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.ParameterSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.properties.Config;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.Project;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
import org.supremica.gui.VisualProject;
import org.supremica.gui.ide.actions.Actions;


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

    public EventKind guessEventKind(final IdentifierProxy ident)
    {
        final String name = ident.getName();
        final IndexedList<EventDeclSubject> decls =
            mModule.getEventDeclListModifiable();
        final EventDeclSubject decl = decls.get(name);
        if (decl != null)
        {
            return decl.getKind();
        }
        final IndexedList<ParameterSubject> params =
            mModule.getParameterListModifiable();
        final ParameterSubject param = params.get(name);
        if (param != null && param instanceof EventParameterSubject)
        {
            final EventParameterSubject eparam = (EventParameterSubject) param;
            final EventDeclSubject edecl = eparam.getEventDecl();
            return edecl.getKind();
        }
        return null;
    }

    public void addStandardPropositions()
    {
        EventDeclSubject accepting = new EventDeclSubject(EventDeclProxy.DEFAULT_MARKING_NAME,
            EventKind.PROPOSITION);
        if (!getModule().getEventDeclListModifiable()
        .containsName(EventDeclProxy.DEFAULT_MARKING_NAME))
        {
            getModule().getEventDeclListModifiable().add(accepting);
        }
        EventDeclSubject forbidden = new EventDeclSubject(EventDeclProxy.DEFAULT_FORBIDDEN_NAME,
            EventKind.PROPOSITION);
        if (!getModule().getEventDeclListModifiable()
        .containsName(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
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
                JOptionPane.showMessageDialog(mIDE, g.getMessage());
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
                JOptionPane.showMessageDialog(mIDE, g.getMessage());
            }
        }
        return panel;
    }

    public ComponentViewPanel getComponentViewPanel(String name)
    {
        List<Proxy> components = mModule.getComponentList();
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



    public void setSelectedComponent(Component selectedComponent)
    {
        mSelectedComponent = selectedComponent;
    }

    public Component getSelectedComponent()
    {
        return mSelectedComponent;
    }

    public int numberOfSelectedAutomata()
    {
        return getSelectedAutomata().size();
    }

    public Automata getSelectedAutomata()
    {
        return getAnalyzerPanel().getSelectedAutomata();
    }
/*
    public Project getSelectedProject()
    {
        return getAnalyzerPanel().getSelectedProject();
    }
 */
/*
    public Project getProject()
    {
        return getAnalyzerPanel().getSelectedProject();
    }
 */
    public Automata getUnselectedAutomata()
    {
        return getAnalyzerPanel().getUnselectedAutomata();
    }

    public Automata getAllAutomata()
    {
        return getAnalyzerPanel().getAllAutomata();
    }

    public JFrame getFrame()
    {
        return mIDE.getFrame();
    }

    public VisualProject getVisualProject()
    {
        return mVisualProject;
    }

    public boolean addAutomaton(Automaton theAutomaton)
    {
        getVisualProject().addAutomaton(theAutomaton);
        return true;// To Do Fix
    }


    public int addAutomata(Automata theAutomata)
    {
        getVisualProject().addAutomata(theAutomata);
        return theAutomata.size(); // TO DO Fix
    }


    //#######################################################################
    //# Undo & Redo
    public EditorWindowInterface getActiveEditorWindowInterface()
    {
        return getEditorPanel().getActiveEditorWindowInterface();
    }

    public void addUndoable(UndoableEdit e)
    {
        if (e.isSignificant())
        {
            mInsignificant.end();
            mUndoManager.addEdit(mInsignificant);
            mInsignificant = new CompoundEdit();
            mUndoManager.addEdit(e);
            fireEditorChangedEvent(new UndoRedoEvent());
        }
        else
        {
            mInsignificant.addEdit(e);
        }
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
        mInsignificant.end();
        mInsignificant.undo();
        mInsignificant = new CompoundEdit();
        mUndoManager.redo();
        mIDE.getActions().editorRedoAction.setEnabled(canRedo());
        mIDE.getActions().editorUndoAction.setEnabled(canUndo());
        fireEditorChangedEvent(new UndoRedoEvent());
    }

    public void undo() throws CannotUndoException
    {
        mInsignificant.end();
        mInsignificant.undo();
        mInsignificant = new CompoundEdit();
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
        for (final Observer o : mObservers)
        {
            o.update(e);
        }
    }

    public ModuleContainer getFlatModuleContainer()
    {
        return flatModuleContainer;
    }

/*
    public GraphProxy getFlatGraphProxy(String name)
    {
        List<Proxy> components = flatModule.getComponentList();
        for (Proxy proxy : components)
        {
            if (proxy instanceof NamedProxy)
            {
                NamedProxy namedProxy = (NamedProxy)proxy;
                if (name.equals(namedProxy.getName()))
                {
                    if (proxy instanceof SimpleComponentProxy)
                    {
                        return ((SimpleComponentProxy)proxy).getGraph();
                    }
                    else
                    {
                        System.err.println("ModuleContainer.getFlatGraphProxy proxy: " + name + " not a GraphProxy");
                        return null;
                    }
                }
            }
        }

        return null;
    }

    public ModuleProxy getFlatModuleProxy()
    {
        return flatModule;
    }
*/
    /**
     * Updates the automata in the analyzer-tab.
     */
    public boolean updateAutomata()
    {
        ProjectBuildFromWaters builder = null;
        Project supremicaProject = null;
        try
        {
            builder = new ProjectBuildFromWaters();
            supremicaProject = builder.build(mModule);
        }
        catch (EvalException eex)
        {
            JOptionPane.showMessageDialog(mIDE, eex.getMessage(),
                "Error in graph",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(mIDE, ex.getMessage(),
                "Error in graph",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
        mVisualProject.clear();
        mVisualProject.addAutomata(supremicaProject);
        mVisualProject.updated();

        if (Config.GUI_ANALYZER_AUTOMATONVIEWER_USE_CONTROLLED_SURFACE.isTrue())
        {
            ProductDESImporter importer = new ProductDESImporter(ModuleSubjectFactory.getInstance());
            ModuleSubject flatModule = (ModuleSubject) importer.importModule(mVisualProject);
            flatModuleContainer = new ModuleContainer(mIDE, flatModule);
        }

        return true;
    }


    //#######################################################################
    //# Data Members
    private final IDE mIDE;
    private final ModuleSubject mModule;
//    private ModuleSubject flatModule = null;
    private ModuleContainer flatModuleContainer = null;
    private final ExpressionParser mExpressionParser;
    private final ProxyPrinter mPrinter;
    private final UndoManager mUndoManager = new UndoManager();
    private CompoundEdit mInsignificant = new CompoundEdit();
    private final Collection<Observer> mObservers = new LinkedList<Observer>();
    private final Map<SimpleComponentSubject,ComponentEditorPanel>
        mComponentToPanelMap =
        new HashMap<SimpleComponentSubject,ComponentEditorPanel>();
    private final Map<SimpleComponentSubject,ComponentViewPanel>
        mComponentToViewPanelMap =
        new HashMap<SimpleComponentSubject,ComponentViewPanel>();

    private EditorPanel editorPanel = null;
    private AnalyzerPanel analyzerPanel = null;
    private SimulatorPanel simulatorPanel = null;
    private Component mSelectedComponent = null;
    private VisualProject mVisualProject = new VisualProject();

}
