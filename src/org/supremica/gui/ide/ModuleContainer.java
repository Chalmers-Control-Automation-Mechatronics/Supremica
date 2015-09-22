//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
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
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.ModuleCompilationErrors;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.command.UndoableCommand;
import net.sourceforge.waters.gui.command.WatersUndoManager;
import net.sourceforge.waters.gui.compiler.BackgroundCompiler;
import net.sourceforge.waters.gui.compiler.CompilationDialog;
import net.sourceforge.waters.gui.compiler.CompilationObserver;
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
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


public class ModuleContainer
  extends DocumentContainer
  implements UndoInterface, Subject, ModelObserver
{

  //#########################################################################
  //# Constructor
  public ModuleContainer(final IDE ide, final ModuleSubject module)
  {
    super(ide, module);

    mModuleContext = new ModuleContext(module);
    mBackgroundCompiler = new BackgroundCompiler(this);
    final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mExpressionParser = new ExpressionParser(factory, optable);

    mTabPanel = new CompilingTabbedPane();
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
    mEditorPanel.showComment();

    module.addModelObserver(this);
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
    Config.INCLUDE_WATERS_SIMULATOR.removePropertyChangeListener
      (mSimulatorPropertyChangeListener);
    mBackgroundCompiler.terminate();
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
      }
      break;
    case ModelChangeEvent.ITEM_REMOVED:
      final Object value = event.getValue();
      if(value instanceof Proxy){
        mUpdateGraphPanelVisitor.updateGraphPanel((Proxy) value);
      }
      break;
    default:
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
    return mBackgroundCompiler.getCompiledDES();
  }

  public Map<Object,SourceInfo> getSourceInfoMap()
  {
    return mBackgroundCompiler.getSourceInfoMap();
  }

  public ExpressionParser getExpressionParser()
  {
    return mExpressionParser;
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
  //# Compilation
  public void compile(final CompilationObserver observer)
  {
    mBackgroundCompiler.compile(observer);
  }

  public void forceCompile(final CompilationObserver observer)
  {
    mBackgroundCompiler.forceCompile(observer);
  }

  public void setCompilationException(final EvalException exception)
  {
    final ModuleCompilationErrors old = mModuleContext.getCompilationErrors();
    final ModuleCompilationErrors current;
    if (exception == null) {
      current = ModuleCompilationErrors.NONE;
    } else {
      current = new ModuleCompilationErrors();
      for (final EvalException e : exception.getAll()) {
        current.add(e);
      }
    }
    mModuleContext.setCompilationErrors(current);
    for (final ProxySubject location : old.getAllLocations()) {
      fireGeneralNotification(location);
    }
    for (final ProxySubject location : current.getAllLocations()) {
      fireGeneralNotification(location);
    }
  }


  //#######################################################################
  //# Auxiliary Methods
  private void fireUndoRedoEvent()
  {
    final EditorChangedEvent event = new UndoRedoEvent(this);
    fireEditorChangedEvent(event);
  }

  private void fireGeneralNotification(final ProxySubject location)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createGeneralNotification(location, null);
    event.fire();
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

  //#########################################################################
  //# Inner Class CompilingTabbedPane
  private class CompilingTabbedPane
    extends JTabbedPane
    implements CompilationObserver
  {
    //#######################################################################
    //# Overrides for javax.swing.JTabbedPane
    @Override
    public void setSelectedIndex(final int index)
    {
      if (index == -1) {
        setSelectedIndexImpl(index);
      } else {
        mSelected = getComponent(index);
        if (mSelected == mEditorPanel) {
          setSelectedIndexImpl(index);
        } else {
          compile(this);
        }
      }
    }

    private void setSelectedIndexImpl(final int index)
    {
      super.setSelectedIndex(index);
      final EditorChangedEvent eevent = new MainPanelSwitchEvent(this);
      fireEditorChangedEvent(eevent);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.compiler.CompilationObserver
    @Override
    public void compilationSucceeded(final ProductDESProxy compiledDES)
    {
      try {
        if (mSelected == mAnalyzerPanel) {
          mAnalyzerPanel.updateAutomata(compiledDES);
        }
        final int index = indexOfComponent(mSelected);
        setSelectedIndexImpl(index);
      } catch (final EvalException exception) {
        final IDE ide = getIDE();
        final CompilationDialog dialog = new CompilationDialog(ide, null);
        dialog.setEvalException(exception, getVerb());
      }
    }

    @Override
    public String getVerb()
    {
      return (mSelected == mSimulatorPanel) ? "simulated" : "analyzed";
    }

    //#######################################################################
    //# Data Members
    private Component mSelected;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
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
  private final BackgroundCompiler mBackgroundCompiler;
  private final ExpressionParser mExpressionParser;
  private final UpdateGraphPanelVisitor mUpdateGraphPanelVisitor =
    new UpdateGraphPanelVisitor();

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





