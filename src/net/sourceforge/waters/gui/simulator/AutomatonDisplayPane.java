//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.gui.BackupGraphPanel;
import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.ColorGroup;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent.EmbedderEventType;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.gui.util.PropositionIcon;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonDisplayPane
  extends BackupGraphPanel
  implements SimulationObserver
{
  //#########################################################################
  //# Constructor
  public AutomatonDisplayPane(final GraphSubject graph,
                              final AutomatonProxy aut,
                              final BindingContext bindings,
                              final ModuleContainer container,
                              final Simulation sim,
                              final AutomatonInternalFrame parent)
    throws GeometryAbsentException
  {
    super(graph, container.getModule());
    mParent = parent;
    mSim = sim;
    mAutomaton = aut;
    mContainer = container;
    mToolTipVisitor = new GraphToolTipVisitor();
    mFocusedItem = null;
    final ModuleSubject module = container.getModule();
    final RenderingContext context = new SimulatorRenderingContext();
    final SimpleExpressionCompiler compiler = sim.getSimpleExpressionCompiler();
    final ProxyShapeProducer producer =
      new SubjectShapeProducer(graph, module, context, compiler, bindings);
    mPopupFactory = new DisplayPanePopupFactory(sim, this);
    setShapeProducer(producer);
    final int width;
    final int height;
    final float scaleFactor = IconAndFontLoader.GLOBAL_SCALE_FACTOR;
    if (ensureGeometryExists()) {
      // Spring embedder is running, guessing window size ...
      final int numStates = aut.getStates().size();
      width = height = Math.round(scaleFactor * (128 + 32 * numStates));
    } else {
      final Rectangle2D imageRect = getMinimumBoundingRectangle();
      width = (int) Math.ceil(scaleFactor * imageRect.getWidth());
      height = (int) Math.ceil(scaleFactor * imageRect.getHeight());
    }
    setPreferredSize(new Dimension(width, height));
    sim.attach(this);
    final MouseHandler handler = new MouseHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
    addComponentListener(new ResizeHandler());
  }


  //#########################################################################
  //# Simple Access
  public Rectangle2D getMinimumBoundingRectangle()
  {
    return getShapeProducer().getMinimumBoundingRectangle();
  }

  public AutomatonProxy getAutomaton()
  {
    return mAutomaton;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.simulator.SimulatorObserver
  @Override
  public void simulationChanged(final SimulationChangeEvent event)
  {
    if (event.getKind() == SimulationChangeEvent.STATE_CHANGED) {
      mRenderingStatusMap = null;
    }
    repaint();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.springembedder.EmbedderObserver
  @Override
  public void embedderChanged(final EmbedderEvent event)
  {
    super.embedderChanged(event);
    if (event.getType() == EmbedderEventType.EMBEDDER_STOP) {
      mParent.storeReferenceFrame();
      mParent.adjustSize();
      clearTransform();
      final Rectangle2D newBounds = AutomatonDisplayPane.this.getMinimumBoundingRectangle();
      this.setPreferredSize(new Dimension((int)newBounds.getWidth(), (int)newBounds.getHeight()));
      mParent.resize(); // Set the size to the initial size, so that the 'Resize Automata' event cannot
      // be fired immediately after the panel has been loaded.
    }
  }


  //#########################################################################
  //# Event Handling Methods
  boolean canExecute()
  {
    if (mFocusedItem == null) {
      return false;
    } else {
      final RenderingStatus status = getRenderingStatus(mFocusedItem);
      return status != null && status.isEnabled();
    }
  }

  void execute(final Proxy proxyToFire)
  {
    if (proxyToFire != null) {
      final RenderingStatus status = getRenderingStatus(mFocusedItem);
      if (status != null && status.isEnabled()) {
        final Map<Object,SourceInfo> infoMap = mContainer.getSourceInfoMap();
        final List<SimulatorStep> possibleSteps =
          new ArrayList<SimulatorStep>();
        if (proxyToFire instanceof IdentifierProxy) {
          for (final TransitionProxy trans : mAutomaton.getTransitions()) {
            final SourceInfo info = infoMap.get(trans);
            if (info != null) {
              final Proxy source = info.getGraphSourceObject();
              if (source == proxyToFire && canBeFired(trans)) {
                addSteps(trans, possibleSteps);
              }
            }
          }
        } else if (proxyToFire instanceof EdgeProxy) {
          for (final TransitionProxy trans : mAutomaton.getTransitions()) {
            final SourceInfo info = infoMap.get(trans);
            if (info != null) {
              final Proxy source = info.getGraphSourceObject();
              final AbstractSubject subject = (AbstractSubject) source;
              final EdgeSubject edge =
                SubjectTools.getAncestor(subject, EdgeSubject.class);
              if (proxyToFire == edge && canBeFired(trans)) {
                addSteps(trans, possibleSteps);
              }
            }
          }
        }
        if (!possibleSteps.isEmpty()) {
          mSim.step(possibleSteps);
        }
      }
    }
  }

  boolean canSetState(final SimpleNodeProxy node)
  {
    final RenderingStatus status = mRenderingStatusMap.get(node);
    return status != null && !status.isActive();
  }

  @Override
  public void close()
  {
    mSim.detach(this);
    super.close();
  }


  //#########################################################################
  //# Painting and Transforming
  @Override
  protected void paintGrid(final Graphics graphics)
  {
  }

  @Override
  protected AffineTransform createTransform()
  {
    final ProxyShapeProducer producer = getShapeProducer();
    final Rectangle2D imageRect = producer.getMinimumBoundingRectangle();
    final Dimension panelSize = getSize();
    final double scaleX = panelSize.getWidth() / imageRect.getWidth();
    final double scaleY = panelSize.getHeight() / imageRect.getHeight();
    final double min = Math.min(scaleX, scaleY);
    final AffineTransform transform = new AffineTransform();
    transform.scale(min, min);
    transform.translate(-imageRect.getX(), -imageRect.getY());
    return transform;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Tries to find an item for highlighting under the mouse cursor.
   * This method searches the graph for an item ({@link EdgeProxy} or
   * {@link IdentifierProxy}) under the muse cursor that can be highlighted
   * in the simulator GUI. If both an identifier and an edge is present,
   * the identifier will be returned.
   * @param  event     Mouse event identifying current pointer position.
   * @return Item at the given position, or <CODE>null</CODE> if nothing
   *         suitable was found.
   */
  private Proxy getClickedItem(final MouseEvent event)
  {
    if (!isEmbedderRunning()) {
      final Point mousePosition = event.getPoint();
      final Point point = applyInverseTransform(mousePosition);
      final GraphProxy graph = getGraph();
      final ProxyShapeProducer producer = getShapeProducer();
      // Nodes have precedence over labels
      for (final NodeProxy node : graph.getNodes()) {
        final ProxyShape shape = producer.getShape(node);
        if (shape.isClicked(point))
        {
          return node;
        }
      }
      // Labels have precedence over edges.
      for (final EdgeProxy edge : graph.getEdges()) {
        for (final Proxy proxy : edge.getLabelBlock().getEventIdentifierList()) {
          final ProxyShape shape = producer.getShape(proxy);
          if (shape.isClicked(point)) {
            return proxy;
          }
        }
      }
      for (final EdgeProxy edge : graph.getEdges()) {
        final ProxyShape shape = producer.getShape(edge);
        if (shape.isClicked(point)) {
          return edge;
        }
      }
      for (final NodeProxy node : graph.getNodes())
      {
        if (node instanceof SimpleNodeProxy)
        {
          final SimpleNodeProxy sNode = (SimpleNodeProxy)node;
          final LabelGeometryProxy label = sNode.getLabelGeometry();
          final ProxyShape shape = producer.getShape(label);
          if (shape.isClicked(point))
            return node;
        }
      }
    }
    return null;
  }

  /**
   * Updates the focused item.
   * This method checks whether the item under the mouse cursor is
   * different from the current focused item ({@link #mFocusedItem}),
   * and if so, replaces the focused item and triggers redrawing of
   * the graph.
   */
  private void updateFocusedItem(final MouseEvent event)
  {
    final Proxy clicked = getClickedItem(event);
    if (clicked != mFocusedItem) {
      mFocusedItem = clicked;
      updateToolTip();
      repaint();
    }
  }

  private void updateToolTip()
  {
    if (!mSim.isAutomatonEnabled(mAutomaton)) {
      final ToolTipVisitor visitor = mSim.getToolTipVisitor();
      final String tooltip = visitor.getToolTip(mAutomaton, true);
      setToolTipText(tooltip);
    } else if (mFocusedItem != null) {
      final String tooltip = mToolTipVisitor.getToolTip(mFocusedItem);
      setToolTipText(tooltip);
    } else {
      setToolTipText(null);
    }
  }

  private void addSteps(final TransitionProxy trans,
                        final List<SimulatorStep> output)
  {
    if (trans.getSource() == mSim.getCurrentState(mAutomaton)) {
      final EventProxy event = trans.getEvent();
      final StateProxy target = trans.getTarget();
      for (final SimulatorStep step: mSim.getEnabledSteps()) {
        if (step.getEvent() == event &&
            step.getTargetState(mAutomaton) == target) {
          output.add(step);
        }
      }
    }
  }

  private boolean canBeFired(final TransitionProxy trans)
  {
    final EventProxy event = trans.getEvent();
    final EventStatus status = mSim.getEventStatus(event);
    return status.canBeFired();
  }

  private RenderingStatus getRenderingStatus(final Proxy item)
  {
    updateRenderingStatus();
    return mRenderingStatusMap.get(item);
  }

  private void updateRenderingStatus()
  {
    if (mRenderingStatusMap == null) {
      if (mSim.isAutomatonEnabled(mAutomaton)) {
        final Collection<StateProxy> states = mAutomaton.getStates();
        final Collection<TransitionProxy> transitions =
          mAutomaton.getTransitions();
        final int size = states.size() + 2 * transitions.size();
        mRenderingStatusMap = new HashMap<Proxy,RenderingStatus>(size);
        final Map<Object,SourceInfo> infomap = mContainer.getSourceInfoMap();
        if (infomap != null) {
          final StateProxy currentState = mSim.getCurrentState(mAutomaton);
          for (final StateProxy state : states) {
            final SourceInfo info = infomap.get(state);
            if (info != null) {
              final Proxy source = info.getGraphSourceObject();
              final boolean active = (state == currentState);
              final RenderingStatus render =
                new RenderingStatus(state, active, false);
              mRenderingStatusMap.put(source, render);
            }
          }
          StateProxy prevState = null;
          EventProxy prevEvent = mSim.getCurrentState().getEvent();
          if (prevEvent != null) {
            final int time = mSim.getCurrentTime();
            final SimulatorState tuple = mSim.getHistoryState(time - 1);
            prevState = tuple.getState(mAutomaton);
          }
          for (final TransitionProxy trans : transitions) {
            final SourceInfo info = infomap.get(trans);
            if (info != null) {
              Proxy source = info.getGraphSourceObject();
              final StateProxy from = trans.getSource();
              final EventProxy event = trans.getEvent();
              final StateProxy to = trans.getTarget();
              final EventStatus status = mSim.getEventStatus(event);
              final boolean enabled =
                from == currentState && status.canBeFired();
              final boolean active =
                from == prevState && event == prevEvent && to == currentState;
              do {
                if (!(source instanceof EventListExpressionProxy)) {
                  RenderingStatus render = mRenderingStatusMap.get(source);
                  if (render == null) {
                    render = new RenderingStatus(trans, active, enabled);
                    mRenderingStatusMap.put(source, render);
                  } else {
                    render.addStatus(active, enabled);
                  }
                }
                final Subject subject = (Subject) source;
                source = SubjectTools.getProxyParent(subject);
              } while (!(source instanceof GraphProxy));
              if (active) {
                prevState = null;
                prevEvent = null;
              }
            }
          }
        }
      } else {
        mRenderingStatusMap = Collections.emptyMap();
      }
    }
  }


  //#########################################################################
  //# Inner Class MouseHandler
  private class MouseHandler implements MouseListener, MouseMotionListener
  {

    //#######################################################################
    //# Interface java.awt.event.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1 &&
          event.getClickCount() == 2) {
        execute(mFocusedItem);
      }
    }

    @Override
    public void mouseEntered(final MouseEvent event)
    {
      updateFocusedItem(event);
    }

    @Override
    public void mouseExited(final MouseEvent event)
    {
      updateFocusedItem(event);
    }

    @Override
    public void mousePressed(final MouseEvent event)
    {
      mPopupFactory.maybeShowPopup(AutomatonDisplayPane.this,
                                   event, mFocusedItem);
    }

    @Override
    public void mouseReleased(final MouseEvent event)
    {
      mPopupFactory.maybeShowPopup(AutomatonDisplayPane.this,
                                   event, mFocusedItem);
    }

    //#######################################################################
    //# Interface java.awt.event.MouseMotionListener
    @Override
    public void mouseDragged(final MouseEvent event)
    {
      updateFocusedItem(event);
    }

    @Override
    public void mouseMoved(final MouseEvent event)
    {
      updateFocusedItem(event);
    }
  }


  //#########################################################################
  //# Inner Class ResizeHandler
  private class ResizeHandler extends ComponentAdapter
  {

    //#######################################################################
    //# Inner Class ResizeHandler
    @Override
    public void componentResized(final ComponentEvent event)
    {
      clearTransform();
    }
  }


  //#########################################################################
  //# Inner Class SimulatorRenderingContext
  private class SimulatorRenderingContext extends ModuleRenderingContext
  {

    //#######################################################################
    //# Constructor
    private SimulatorRenderingContext()
    {
      super(mContainer.getModuleContext());
      final Map<Object,SourceInfo> infomap = mContainer.getSourceInfoMap();
      final Collection<StateProxy> states = mAutomaton.getStates();
      final int size = states.size();
      mStateMap = new HashMap<SimpleNodeProxy,StateProxy>(size);
      for (final StateProxy state : states) {
        final SourceInfo info = infomap.get(state);
        final SimpleNodeProxy node = (SimpleNodeProxy) info.getSourceObject();
        mStateMap.put(node, state);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.renderer.RenderingContext
    @Override
    public PropositionIcon.ColorInfo getMarkingColorInfo(final GraphProxy graph,
                                                  final SimpleNodeProxy node)
    {
      // The spring embedder modifies a copy of our graph. When it is running,
      // the items being displayed are not in our compiled graph ...
      final Proxy orig = getOriginal(node);
      final StateProxy state = mStateMap.get(orig);
      if (state != null) {
        return mSim.getMarkingColorInfo(state, mAutomaton);
      } else {
        // This state was a victim of compiler optimisation ..,
        return PropositionIcon.getUnmarkedColors();
      }
    }

    @Override
    public RenderingInformation getRenderingInformation(final Proxy proxy,
                                                        final ColorGroup group)
    {
      // The spring embedder modifies a copy of our graph. When it is running,
      // the items being displayed are not in our compiled graph ...
      final Proxy orig = getOriginal(proxy);
      if (orig == null) {
        // *** BUG? ***
        // Identifiers have no original, this may cause failure to
        // highlight them while spring embedding.
        return super.getRenderingInformation(proxy, group);
      } else {
        final RenderingStatus status;
        if (orig instanceof LabelGeometrySubject) {
          final LabelGeometrySubject geo = (LabelGeometrySubject) orig;
          final Proxy node = (Proxy) geo.getParent();
          status = getRenderingStatus(node);
        } else {
          status = getRenderingStatus(orig);
        }
        if (status == null) {
          final RenderingInformation info =
            super.getRenderingInformation(orig, group);
          info.setColor(EditorColor.SIMULATION_INVALID);
          return info;
        } else {
          final boolean selected;
          if (orig == mFocusedItem) {
            selected = true;
          } else if (orig instanceof IdentifierSubject ||
                     orig instanceof ForeachSubject) {
            final Subject subject = (Subject) orig;
            final EdgeSubject edge =
              SubjectTools.getAncestor(subject, EdgeSubject.class);
            selected = (mFocusedItem == edge);
          } else if (orig instanceof SimpleNodeSubject) {
            final SimpleNodeSubject node = (SimpleNodeSubject) orig;
            selected = (mFocusedItem == node.getLabelGeometry());
          } else if (orig instanceof LabelGeometrySubject) {
            final LabelGeometrySubject geo = (LabelGeometrySubject) orig;
            selected = (mFocusedItem == geo.getParent());
          } else {
            selected = false;
          }
          if (selected || status.isActive() || status.isEnabled()) {
            final Color foreground = status.getForegroundColor();
            final Color shadow = status.getShadowColor(selected);
            final int prio = getPriority(orig);
            return new RenderingInformation
              (false, false, false, true, foreground, shadow, prio);
          } else {
            return super.getRenderingInformation(orig, group);
          }
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final Map<SimpleNodeProxy,StateProxy> mStateMap;

  }


  //#########################################################################
  //# Inner Class GraphToolTipVisitor
  private class GraphToolTipVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private String getToolTip(final Proxy proxy)
    {
      try {
        return (String) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public String visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
    @Override
    public String visitEdgeProxy(final EdgeProxy edge)
    {
      final RenderingStatus status = getRenderingStatus(edge);
      if (status != null && status.getCount() == 1) {
        return getTransitionToolTip(edge);
      } else {
        return null;
      }
    }

    @Override
    public String visitIdentifierProxy(final IdentifierProxy ident)
    {
      return getTransitionToolTip(ident);
    }

    @Override
    public String visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final RenderingStatus status = getRenderingStatus(node);
      if (status == null) {
        final String name = node.getName();
        return "State " + name + " has been removed due to optimisation";
      } else {
        final ToolTipVisitor master = mSim.getToolTipVisitor();
        final StateProxy state = (StateProxy) status.getAutomatonItem();
        return master.getToolTip(state, mAutomaton);
      }
    }

    //#########################################################################
    //# Inner Class RenderingStatus
    private String getTransitionToolTip(final Proxy source)
    {
      try {
        final StringWriter writer = new StringWriter();
        final RenderingStatus status = getRenderingStatus(source);
        if (status == null) {
          writer.write("Transition ");
          ModuleProxyPrinter.printProxy(writer, source);
          writer.write(" has been removed due to optimisation");
        } else {
          if (status.getCount() == 1) {
            final TransitionProxy trans =
              (TransitionProxy) status.getAutomatonItem();
            final EventProxy event = trans.getEvent();
            final EventKind kind = event.getKind();
            writer.write(ModuleContext.getEventKindToolTip(kind, false));
            if (!event.isObservable()) {
              writer.write(" unobservable");
            }
            writer.write(" transition ");
            writer.write(event.getName());
          } else {
            writer.write("Event group ");
            ModuleProxyPrinter.printProxy(writer, source);
          }
          if (status.isActive()) {
            writer.write(", has just been executed");
          }
          if (!status.isEnabled()) {
            writer.write(", currently disabled");
          } else if (status.getCount() == 1) {
            writer.write(", currently enabled");
          } else {
            writer.write(", contains enabled events");
          }
        }
        return writer.toString();
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

  }


  //#########################################################################
  //# Inner Class RenderingStatus
  private static class RenderingStatus
  {
    //#######################################################################
    //# Constructor
    private RenderingStatus(final Proxy proxy,
                            final boolean active,
                            final boolean enabled)
    {
      mActive = active;
      mEnabled = enabled;
      mCount = 1;
      mAutomatonItem = proxy;
    }

    //#######################################################################
    //# Simple Access
    private boolean isActive()
    {
      return mActive;
    }

    private boolean isEnabled()
    {
      return mEnabled;
    }

    private int getCount()
    {
      return mCount;
    }

    private Proxy getAutomatonItem()
    {
      return mAutomatonItem;
    }

    private void addStatus(final boolean active, final boolean enabled)
    {
      mActive |= active;
      mEnabled |= enabled;
      mCount++;
      mAutomatonItem = null;
    }

    //#######################################################################
    //# Colour Access
    private Color getForegroundColor()
    {
      if (isEnabled()) {
        return EditorColor.SIMULATION_ENABLED;
      } else if (isActive()) {
        return EditorColor.SIMULATION_ACTIVE;
      } else {
        return EditorColor.DEFAULTCOLOR;
      }

    }

    private Color getShadowColor(final boolean selected)
    {
      if (selected) {
        if (isEnabled()) {
          return EditorColor.SIMULATION_FOCUSED_SHADOW;
        } else {
          return EditorColor.shadow(EditorColor.SIMULATION_DISABLED_FOCUSED);
        }
      } else {
        if (isActive()) {
          return EditorColor.shadow(EditorColor.SIMULATION_ACTIVE);
        } else if (isEnabled()) {
          return EditorColor.shadow(EditorColor.SIMULATION_ENABLED);
        } else {
          return null;
        }
      }
    }

    //#######################################################################
    //# Data Members
    /**
     * Whether the state is the current state, or whether a transition has
     * just fired.
     */
    private boolean mActive;
    /**
     * Whether a transition is currently enabled.
     */
    private boolean mEnabled;
    /**
     * The number of automaton transitions compiled from this graph element.
     * This is used when generating tooltips, to identify a label as an
     * &quot;event group&quot;.
     */
    private int mCount;
    /**
     * The unique item in the automaton corresponding to this graph element,
     * or <CODE>null</CODE> if more than one item has been created from it.
     */
    private Proxy mAutomatonItem;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonInternalFrame mParent;
  private final Simulation mSim;
  private final AutomatonProxy mAutomaton;
  private final ModuleContainer mContainer;
  private final DisplayPanePopupFactory mPopupFactory;
  private final GraphToolTipVisitor mToolTipVisitor;

  private Proxy mFocusedItem;
  private Map<Proxy,RenderingStatus> mRenderingStatusMap;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
