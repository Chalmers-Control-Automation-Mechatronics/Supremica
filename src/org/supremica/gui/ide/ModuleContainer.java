//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
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

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.HTMLPrinter;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.command.UndoableCommand;
import net.sourceforge.waters.gui.command.WatersUndoManager;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.MainPanelSwitchEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.observer.UndoRedoEvent;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
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

  //#########################################################################
  //# Constructor
  public ModuleContainer(final IDE ide, final ModuleSubject module)
  {
    super(ide, module);

    mModuleContext = new ModuleContext(module);
    final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mExpressionParser = new ExpressionParser(factory, optable);
    mPrinter = new HTMLPrinter();
    final DocumentManager manager = ide.getDocumentManager();
    final ProductDESProxyFactory desfactory =
      ProductDESElementFactory.getInstance();
    mCompiler = new ModuleCompiler(manager, desfactory, module);
    mCompiler.setSourceInfoEnabled(true);
    mCompiler.setOptimizationEnabled(Config.OPTIMIZING_COMPILER.isTrue());
    mCompilerPropertyChangeListener =
      new CompilerPropertyChangeListener();
    Config.OPTIMIZING_COMPILER.addPropertyChangeListener
      (mCompilerPropertyChangeListener);

    mTabPanel = new JTabbedPane();
    mEditorPanel = new EditorPanel(this, "Editor");
    mSimulatorPanel = new SimulatorPanel(this, "Simulator");
    mAnalyzerPanel = new AnalyzerPanel(this, "Analyzer");
    mTabPanel.add(mEditorPanel);
    mSimulatorPropertyChangeListener =
      new SimulatorPropertyChangeListener();
    Config.INCLUDE_WATERS_SIMULATOR.addPropertyChangeListener
    (mSimulatorPropertyChangeListener);
    if (Config.INCLUDE_WATERS_SIMULATOR.isTrue()) {
      mTabPanel.add(mSimulatorPanel);
    }
    mTabPanel.add(mAnalyzerPanel);
    mTabPanel.addChangeListener(this);
    mEditorPanel.showComment();

    module.addModelObserver(this);

    mTabPanel.addMouseListener(new java.awt.event.MouseAdapter()
    {
      @Override
      public void mouseClicked(final java.awt.event.MouseEvent e)
      {
        if (mTabPanel.getSelectedComponent().getName().equals
              (mAnalyzerPanel.getName())) {
          mAnalyzerPanel.sortAutomataByName();
        }
      }
    });
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# org.supremica.gui.ide.DocumentContainer
  @Override
  public boolean hasUnsavedChanges()
  {
    return mUndoIndex != mUndoCheckPoint;
  }

  @Override
  public void setCheckPoint()
  {
    mUndoCheckPoint = mUndoIndex;
  }

  @Override
  public void close()
  {
    mEditorPanel.close();
    Config.OPTIMIZING_COMPILER.removePropertyChangeListener
      (mCompilerPropertyChangeListener);
    Config.INCLUDE_WATERS_SIMULATOR.removePropertyChangeListener
      (mSimulatorPropertyChangeListener);
  }

  @Override
  public Component getPanel()
  {
    return mTabPanel;
  }

  @Override
  public EditorPanel getEditorPanel()
  {
    return mEditorPanel;
  }

  @Override
  public AnalyzerPanel getAnalyzerPanel()
  {
    return mAnalyzerPanel;
  }

  @Override
  public Component getActivePanel()
  {
    return mTabPanel.getSelectedComponent();
  }

  @Override
  public String getTypeString()
  {
    return TYPE_STRING;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Subject
  @Override
  public void attach(final Observer o)
  {
    mObservers.add(o);
  }

  @Override
  public void detach(final Observer o)
  {
    mObservers.remove(o);
  }

  @Override
  public void fireEditorChangedEvent(final EditorChangedEvent event)
  {
    // Just in case they try to register or unregister observers
    // in response to the update ...
    final Collection<Observer> copy = new LinkedList<Observer>(mObservers);
    for (final Observer observer : copy) {
      observer.update(event);
    }
    getIDE().fireEditorChangedEvent(event);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  /**
   * Implementation of {@link ModelObserver} interface.
   * If a component is removed from the module, and its automaton
   * is currently displayed, show the comment editor instead.
   */
  @Override
  public void modelChanged(final ModelChangeEvent event)
  {
    final int kind = event.getKind();
    switch (kind) {
    case ModelChangeEvent.NAME_CHANGED:
      if (event.getSource() == getModule()) {
        setDocumentNameHasChanged(true);
      } else {
        mCompiledDES = null;
      }
      break;
    case ModelChangeEvent.STATE_CHANGED:
      if (event.getSource() != getModule()) {
        mCompiledDES = null;
      }
      break;
    case ModelChangeEvent.ITEM_REMOVED:
      final Object value = event.getValue();
      if(value instanceof Proxy){
        mUpdateGraphPanelVisitor.updateGraphPanel((Proxy) value);
      }
      mCompiledDES = null;
      break;
    case ModelChangeEvent.GEOMETRY_CHANGED:
      break;
    default:
      mCompiledDES = null;
      break;
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.CLEANUP_PRIORITY_0;
  }


  //#########################################################################
  //# Simple Access
  public SimulatorPanel getSimulatorPanel()
  {
    return mSimulatorPanel;
  }

  public ModuleSubject getModule()
  {
    return (ModuleSubject) getDocument();
  }

  public ModuleContext getModuleContext()
  {
    return mModuleContext;
  }

  public ProductDESProxy getCompiledDES()
  {
    return mCompiledDES;
  }

  public Map<Object,SourceInfo> getSourceInfoMap()
  {
    if (mCompiledDES == null) {
      return null;
    } else {
      return mCompiler.getSourceInfoMap();
    }
  }

  public ExpressionParser getExpressionParser()
  {
    return mExpressionParser;
  }

  public ProxyPrinter getPrinter()
  {
    return mPrinter;
  }

  public void showComment()
  {
    mTabPanel.setSelectedComponent(mEditorPanel);
    mEditorPanel.showComment();
  }

  public ComponentEditorPanel showEditor(final SimpleComponentSubject comp)
    throws GeometryAbsentException
  {
    final ComponentEditorPanel panel = createComponentEditorPanel(comp);
    mTabPanel.setSelectedComponent(mEditorPanel);
    mEditorPanel.setRightComponent(panel);
    return panel;
  }

  ComponentEditorPanel createComponentEditorPanel
    (final SimpleComponentSubject comp)
    throws GeometryAbsentException
  {
    ComponentEditorPanel panel = getComponentEditorPanel(comp);
    if (panel == null) {
      final EditorPanel editorPanel = getEditorPanel();
      final JComponent right = editorPanel.getRightComponent();
      final Dimension oldsize = right.getSize();
      panel = new ComponentEditorPanel(this, comp, oldsize);
      mComponentToPanelMap.put(comp, panel);
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
    if (panel == null) {
      final AnalyzerPanel analyzerPanel = getAnalyzerPanel();
      final JComponent right = analyzerPanel.getRightComponent();
      final Dimension oldsize = right.getSize();
      try {
        panel = new ComponentViewPanel(this, comp, oldsize);
        mComponentToViewPanelMap.put(comp, panel);
      } catch (final GeometryAbsentException exception) {
        JOptionPane.showMessageDialog(getIDE(), exception.getMessage());
      }
    }
    return panel;
  }

  public ComponentViewPanel getComponentViewPanel(final String name)
  {
    final List<Proxy> components = getModule().getComponentList();
    for (final Proxy proxy : components) {
      if (proxy instanceof NamedProxy) {
        final NamedProxy namedProxy = (NamedProxy) proxy;
        if (name.equals(namedProxy.getName())) {
          if (proxy instanceof SimpleComponentSubject) {
            return getComponentViewPanel(((SimpleComponentSubject)proxy));
          } else {
            return null;
          }
        }
      }
    }
    return null;
  }

  public void switchToTraceMode(final TraceProxy trace)
  {
    mTabPanel.setSelectedComponent(mSimulatorPanel);
    mSimulatorPanel.switchToTraceMode(trace);
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
  @Override
  public void executeCommand(final Command c)
  {
    c.execute();
    addUndoable(new UndoableCommand(c));
  }

  @Override
  public void addUndoable(final UndoableCommand command)
  {
    assert(command.isSignificant());
      mUndoManager.addCommand(command);
      if (mUndoIndex++ < mUndoCheckPoint) {
        mUndoCheckPoint = -1;
      }
      fireUndoRedoEvent();
  }

  @Override
  public boolean canRedo()
  {
    return mUndoManager.canRedo();
  }

  @Override
  public boolean canUndo()
  {
    return mUndoManager.canUndo();
  }

  @Override
  public void clearList()
  {
    mUndoManager.discardAllEdits();
    mUndoIndex = 0;
    fireUndoRedoEvent();
  }

  @Override
  public String getRedoPresentationName()
  {
    return mUndoManager.getRedoPresentationName();
  }

  @Override
  public String getUndoPresentationName()
  {
    return mUndoManager.getUndoPresentationName();
  }

  @Override
  public void redo() throws CannotRedoException
  {
    mUndoManager.redo();
    mUndoIndex++;
    fireUndoRedoEvent();
  }

  @Override
  public void undo() throws CannotUndoException
  {
    mUndoManager.undo();
    mUndoIndex--;
    fireUndoRedoEvent();
  }

  @Override
  public void undoAndRemoveLastCommand() throws CannotUndoException
  {
    mUndoManager.undo();
    mUndoIndex--;
    removeLastCommand();
    fireUndoRedoEvent();
  }

  @Override
  public void removeLastCommand(){
    mUndoManager.removeLast();
  }

  @Override
  public Command getLastCommand(){
    return mUndoManager.getLastCommand();
  }


  //#######################################################################
  //# Interface javax.swing.event.ChangeListener
  @Override
  public void stateChanged(final ChangeEvent event)
  {
    final Component selected = mTabPanel.getSelectedComponent();
    try {
      if (selected == mSimulatorPanel) {
        recompile();
      } else if (selected == mAnalyzerPanel) {
        recompile();
        mAnalyzerPanel.updateAutomata();
      }
      final EditorChangedEvent eevent = new MainPanelSwitchEvent(this);
      // This line of code fires whenever a tab is changed.
      fireEditorChangedEvent(eevent);
    } catch (final EvalException exception) {
      final String msg = exception.getMessage();
      final IDE ide = getIDE();
      ide.error(msg);
      setLastCompilationException(exception);
      mTabPanel.setSelectedComponent(mEditorPanel);
    }
  }


  //#######################################################################
  //# Compilation
  public ProductDESProxy recompile()
    throws EvalException
  {
    if (mCompiledDES == null) {
      mCompiledDES = mCompiler.compile();
      setLastCompilationException(null);
    }
    return mCompiledDES;
  }

  public EvalException getLastCompilationException()
  {
    return mLastCompilationException;
  }

  public void setLastCompilationException(final EvalException exception)
  {
    final EvalException previousException = mLastCompilationException;
    if (exception != null
        && !(exception.getLocation() instanceof net.sourceforge.waters.subject.base.Subject)) {
      // location is invalid, so ignore it
      mLastCompilationException = null;
    } else {
      mLastCompilationException = exception;
    }
    fireCompilationExceptionEvent(previousException);
    fireCompilationExceptionEvent(mLastCompilationException);
  }

  private void fireCompilationExceptionEvent(final EvalException exception)
  {
    if (exception != null) {
      net.sourceforge.waters.subject.base.Subject location;
      ModelChangeEvent event;
      location = (net.sourceforge.waters.subject.base.Subject) exception.getLocation();
      event = ModelChangeEvent.createStateChanged(location);
      event.fire();
    }
  }


  //#######################################################################
  //# Auxiliary Methods
  private void fireUndoRedoEvent()
  {
    final EditorChangedEvent event = new UndoRedoEvent(this);
    fireEditorChangedEvent(event);
  }


  //#######################################################################
  //# Inner Class CompilerPropertyChangeListener
  private class CompilerPropertyChangeListener
      implements SupremicaPropertyChangeListener
  {

    @Override
    public void propertyChanged(final SupremicaPropertyChangeEvent event)
    {
      mCompiler.setOptimizationEnabled(Config.OPTIMIZING_COMPILER.isTrue());
      mCompiledDES = null;
    }

  }


  //#######################################################################
  //# Inner Class SimulatorPropertyChangeListener
  private class SimulatorPropertyChangeListener
      implements SupremicaPropertyChangeListener
  {

    @Override
    public void propertyChanged(final SupremicaPropertyChangeEvent event)
    {
      if (Config.INCLUDE_WATERS_SIMULATOR.isTrue()) {
        mTabPanel.add(mSimulatorPanel, 1);
      } else {
        mTabPanel.remove(mSimulatorPanel);
      }
    }
  }


  //#######################################################################
  //# Inner Class UpdateGraphPanelVisitor
  /**
   * This visitor is used to make sure an automaton is no longer visible in
   * the graph panel if it (or its parent) has been deleted from the module.
   * @author Carly Hona
   */
  private class UpdateGraphPanelVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    public void updateGraphPanel(final Proxy proxy)
    {
      try {
        if (mEditorPanel.getActiveEditorWindowInterface() != null) {
          proxy.acceptVisitor(this);
        }
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
      throws VisitorException
    {
      for (final Proxy proxy : foreach.getBody()) {
        if (proxy.acceptVisitor(this) != null) {
          return null;
        }
      }
      return null;
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy simple)
      throws VisitorException
    {
      final ComponentEditorPanel panel =
        mEditorPanel.getActiveEditorWindowInterface();
      if (panel.getComponent() == simple) {
        mEditorPanel.showComment();
        return simple; //stop iterating children if in a foreach
      }
      return null;
    }
  }


  //#######################################################################
  //# Data Members
  private final JTabbedPane mTabPanel;
  private final EditorPanel mEditorPanel;
  private final SimulatorPanel mSimulatorPanel;
  private final AnalyzerPanel mAnalyzerPanel;

  private final Map<SimpleComponentSubject,ComponentEditorPanel>
    mComponentToPanelMap =
    new HashMap<SimpleComponentSubject,ComponentEditorPanel>();
  private final Map<SimpleComponentSubject,ComponentViewPanel>
    mComponentToViewPanelMap =
    new HashMap<SimpleComponentSubject,ComponentViewPanel>();

  private final ModuleContext mModuleContext;
  private final ModuleCompiler mCompiler;
  private final ExpressionParser mExpressionParser;
  private final ProxyPrinter mPrinter;
  private final UpdateGraphPanelVisitor mUpdateGraphPanelVisitor =
    new UpdateGraphPanelVisitor();
  private ProductDESProxy mCompiledDES;
  private EvalException mLastCompilationException;

  private final CompilerPropertyChangeListener
    mCompilerPropertyChangeListener;
  private final SupremicaPropertyChangeListener
    mSimulatorPropertyChangeListener;
  private final WatersUndoManager mUndoManager = new WatersUndoManager();
  private int mUndoIndex = 0;
  private int mUndoCheckPoint = 0;
  private final Collection<Observer> mObservers = new LinkedList<Observer>();


  //#######################################################################
  //# Class Constants
  static final String TYPE_STRING = "Waters module";

}
