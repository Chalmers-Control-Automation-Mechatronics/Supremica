//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Simulator
//# PACKAGE: net.sourceforge.waters.gui.simulator
//# CLASS:   AutomatonDisplayPane
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.simulator;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import net.sourceforge.waters.gui.BackupGraphPanel;
import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.GraphPanel;
import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent.EmbedderEventType;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.ForeachEventSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonDisplayPane
  extends BackupGraphPanel
  implements SimulationObserver
{
  //##########################################################################
  //# Constructors
  public AutomatonDisplayPane(final AutomatonProxy aut,
                              final GraphSubject graph,
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
    mFocusedItem = null;
    mTransform = mInverseTransform = null;
    final ModuleSubject module = container.getModule();
    final RenderingContext context = new SimulatorRenderingContext();
    final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
    final SourceInfo sInfo = infomap.get(aut);
    final SimpleExpressionCompiler compiler = sim.getSimpleExpressionCompiler();
    final BindingContext bindings = sInfo.getBindingContext();
    final ProxyShapeProducer producer =
      new SubjectShapeProducer(graph, module, context, compiler, bindings);
    factory = new DisplayPanePopupFactory(container.getIDE().getPopupActionManager(),
                                          this,
                                          (AutomatonDesktopPane) parent.getDesktopPane(),
                                          mContainer.getSourceInfoMap());
    setShapeProducer(producer);
    final int width;
    final int height;
    if (ensureGeometryExists()) {
      // Spring embedder is running, guessing window size ...
      final int numstates = aut.getStates().size();
      width = height = 128 + 32 * numstates;
    } else {
      final Rectangle2D imageRect = this.getMinimumBoundingRectangle();
      width = (int) Math.ceil(imageRect.getWidth());
      height = (int) Math.ceil(imageRect.getHeight());
    }
    setPreferredSize(new Dimension(width, height));
    setBackground(EditorColor.BACKGROUNDCOLOR);
    sim.attach(this);
    final MouseHandler handler = new MouseHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
    addComponentListener(new ResizeHandler());
    updateEnabledProxy();
  }


  //##########################################################################
  //# Simple Access
  public Rectangle2D getMinimumBoundingRectangle()
  {
    return getShapeProducer().getMinimumBoundingRectangle();
  }

  public AutomatonProxy getAutomaton()
  {
    return mAutomaton;
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.gui.simulator.SimulatorObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    if (event.getKind() == SimulationChangeEvent.STATE_CHANGED)
      updateEnabledProxy();
    repaint();
  }

  private void updateEnabledProxy()
  {
    mEnabledProxy = new HashSet<Proxy>();
    mNonOptimizedProxy = new HashSet<Proxy>();
    final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
    if (infomap != null) // If the simulation model isn't currently being changed
    {
      transitions:
      for (final TransitionProxy trans : mSim.getActiveTransitions(mAutomaton)) {
        final EventProxy event = trans.getEvent();
        if (mSim.getActiveEvents().contains(event))
        {
          final Proxy proxy = infomap.get(trans).getGraphSourceObject();
          mEnabledProxy.add(proxy);
          final EdgeSubject edge =
            SubjectTools.getAncestor((Subject) proxy, EdgeSubject.class);
          mEnabledProxy.add(edge);
          continue transitions;
        }
      }
      for (final TransitionProxy trans : mAutomaton.getTransitions())
      {
        if (infomap.get(trans) != null)
        {
          final SourceInfo info = infomap.get(trans);
          final Proxy source = info.getGraphSourceObject();
          final EdgeSubject edge =
            SubjectTools.getAncestor((Subject) source, EdgeSubject.class);
          mNonOptimizedProxy.add(source);
          mNonOptimizedProxy.add(edge);
        }
      }
    }
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.gui.springembedder.EmbedderObserver
  public void embedderChanged(final EmbedderEvent event)
  {
    super.embedderChanged(event);
    if (event.getType() == EmbedderEventType.EMBEDDER_STOP) {
      mParent.storeReferenceFrame();
      mParent.adjustSize(false);
      mParent.storeReferenceFrame();
      mTransform = mInverseTransform = null;
      final Rectangle2D newBounds = AutomatonDisplayPane.this.getMinimumBoundingRectangle();
      this.setPreferredSize(new Dimension((int)newBounds.getWidth(), (int)newBounds.getHeight()));
      mParent.resize(); // Set the size to the initial size, so that the 'Resize Automata' event cannot
      // be fired immediately after the panel has been loaded.
    }
  }


  //#########################################################################
  //# Repaint Support
  public void close()
  {
    mSim.detach(this);
    super.close();
  }

  protected void graphChanged(final ModelChangeEvent event)
  {
    super.graphChanged(event);
    mTransform = mInverseTransform = null;
  }

  //##########################################################################
  //# Repainting
  public void paint(final Graphics g)
  {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    final Graphics2D g2d = (Graphics2D) g;
    final AffineTransform old = g2d.getTransform();
    final AffineTransform transform = createTransform();
    final AffineTransform copy = new AffineTransform(old);
    copy.concatenate(transform);
    g2d.setTransform(copy);
    super.paint(g2d);
    g2d.setTransform(old);
  }

  protected void paintGrid(final Graphics g)
  {
  }


  //##########################################################################
  //# Auxiliary Methods
  private AffineTransform createTransform()
  {
    if (mTransform == null) {
      final ProxyShapeProducer producer = getShapeProducer();
      final Rectangle2D imageRect = producer.getMinimumBoundingRectangle();
      final Dimension panelSize = getSize();
      final double scaleX = panelSize.getWidth() / imageRect.getWidth();
      final double scaleY = panelSize.getHeight() / imageRect.getHeight();
      final double min = Math.min(scaleX, scaleY);
      mTransform = new AffineTransform();
      mTransform.scale(min, min);
      mTransform.translate(-imageRect.getX(), -imageRect.getY());
    }
    return mTransform;
  }

  private AffineTransform createInverseTransform()
  {
    if (mInverseTransform == null) {
      try {
        final AffineTransform transform = createTransform();
        mInverseTransform = transform.createInverse();
      } catch (final NoninvertibleTransformException exception) {
        throw new WatersRuntimeException(exception);
      }
    }
    return mInverseTransform;
  }

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
    if (!isEmbedderRunning())
    {
      final AffineTransform inverse = createInverseTransform();
      final Point location = event.getPoint();
      final Point2D orig = inverse.transform(location, null);
      final int x = (int) Math.round(orig.getX());
      final int y = (int) Math.round(orig.getY());
      final GraphProxy graph = getGraph();
      final ProxyShapeProducer producer = getShapeProducer();
      // Nodes have precedence over labels
      for (final NodeProxy node : graph.getNodes()) {
        final ProxyShape shape = producer.getShape(node);
        if (shape.isClicked(x, y))
        {
          return node;
        }
      }
      // Labels have precedence over edges.
      for (final EdgeProxy edge : graph.getEdges()) {
        for (final Proxy proxy : edge.getLabelBlock().getEventList()) {
          final ProxyShape shape = producer.getShape(proxy);
          if (shape.isClicked(x, y)) {
            return proxy;
          }
        }
      }
      for (final EdgeProxy edge : graph.getEdges()) {
        final ProxyShape shape = producer.getShape(edge);
        if (shape.isClicked(x, y)) {
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
          if (shape.isClicked(x, y))
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
    if (mFocusedItem == null)
    {
      setToolTipText(null);
    }
    else
    {
      final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
      boolean foundState = false;
      for (final StateProxy possibleState : mAutomaton.getStates())
      {
        if (mFocusedItem instanceof GroupNodeProxy)
          return;
        if (mFocusedItem == infomap.get(possibleState).getSourceObject())
        {
          String toolTipText = "";
          if (possibleState.isInitial())
          {
            toolTipText += "Initial state";
          }
          else
            toolTipText += "State";
          toolTipText += " " + possibleState.getName();
          if (mSim.getCurrentStates().get(mAutomaton) == possibleState)
          {
            toolTipText += ", current state";
          }
          if (possibleState.getPropositions().size() != 0)
          {
            toolTipText += ", marked as ";
            for (final EventProxy proposition : possibleState.getPropositions())
            {
              toolTipText += proposition.getName() + ", ";
            }
            toolTipText = toolTipText.substring(0, toolTipText.length() - 2);
          }
          this.setToolTipText(toolTipText);
          foundState = true;
        }
      }
      if (mFocusedItem instanceof NodeProxy && !foundState)
      {
        setToolTipText("State " + ((NodeProxy)mFocusedItem).getName() + " has been removed due to optimisation");
      }
      boolean foundTrans = false;
      for (final TransitionProxy trans : mAutomaton.getTransitions())
      {
        final Proxy source = infomap.get(trans).getGraphSourceObject();
        if (source == mFocusedItem) {
          String toolTipText = "";
          if (trans.getEvent().getKind() == EventKind.CONTROLLABLE)
            toolTipText += "Controllable event ";
          else
            toolTipText += "Uncontrollable event ";
          toolTipText += trans.getEvent().getName();
          boolean enabled = false;
          for (final Step possibleSteps : mSim.getValidTransitions())
          {
            if (trans.getEvent() == possibleSteps.getEvent()
                && (trans.getSource() == possibleSteps.getSource().get(mAutomaton) || possibleSteps.getSource().get(mAutomaton) == null)
                && (trans.getTarget() == possibleSteps.getDest().get(mAutomaton) || possibleSteps.getDest().get(mAutomaton) == null)
                && mSim.getCurrentStates().get(mAutomaton) == trans.getSource())
              enabled = true;
          }
          if (enabled)
            toolTipText += ", currently enabled";
          else
            toolTipText += ", currently disabled";
          Step warning = null;
          for (final Step step : mSim.getWarningProperties().keySet())
          {
            if (step.getEvent() == trans.getEvent())
              warning = step;
          }
          if (warning != null)
            toolTipText +=
              ", firing this event will cause the property " +
              mSim.getWarningProperties().get(warning).getName() +
              " to be disabled";
          this.setToolTipText(toolTipText);
          foundTrans = true;
        }
      }
      if (!foundTrans && mFocusedItem instanceof IdentifierProxy)
        setToolTipText("Transition " + ((IdentifierProxy)mFocusedItem).toString() + " has been removed due to optimisation");
    }
  }

  /**
   * Tests whether the given item represents an enabled transition.
   * @param  clicked   The item to be examined, which should be of type
   *                   {@link IdentifierProxy} or {@link EdgeProxy}.
   * @return <CODE>true</CODE> if the given item represents a transition
   *         that is enabled in the current simulation state;
   *         <CODE>false</CODE> otherwise.
   */
  private boolean isEnabled(final Proxy clicked)
  {
    return mEnabledProxy.contains(clicked);
  }

  private Step findOptions(final List<Step> possibleEvents)
  {
    if (possibleEvents.size() == 0)
    {
      mContainer.getIDE().error("There are no possible events to fire");
      return null;
    }
    else if (possibleEvents.size() == 1)
      return possibleEvents.get(0);
    else {
      final JLabel[] possibilities = new JLabel[possibleEvents.size()];
      final Step[] events = new Step[possibleEvents.size()];
      for (int looper = 0; looper < possibleEvents.size(); looper++) {
        final Step event = possibleEvents.get(looper);
        final JLabel toAdd = new JLabel(event.toString());
        if (event.getEvent().getKind() == EventKind.CONTROLLABLE)
          toAdd.setIcon(IconLoader.ICON_CONTROLLABLE);
        else if (event.getEvent().getKind() == EventKind.UNCONTROLLABLE)
          toAdd.setIcon(IconLoader.ICON_UNCONTROLLABLE);
        else
          toAdd.setIcon(IconLoader.ICON_PROPOSITION);
        possibilities[looper] = toAdd;
        events[looper] = event;
      }
      final EventChooserDialog dialog =
          new EventChooserDialog(mContainer.getIDE(), possibilities, events);
      dialog.setVisible(true);
      final Step event = dialog.getSelectedStep();
      if ((event != null && !dialog.wasCancelled())) {
        for (final Step findEvent : possibleEvents) {
          if (findEvent == event)
            return event;
        }
      }
      return null;
    }
  }

  private boolean eventCanBeFired(final Simulation sim, final TransitionProxy trans)
  {
    for (final Step possibleStep : sim.getValidTransitions())
    {
      if (possibleStep.getEvent() == trans.getEvent() && sim.getCurrentStates().get(mAutomaton) == trans.getSource())
        return true;
    }
    return false;
  }

  private ArrayList<Step> getSteps(final TransitionProxy trans)
  {
    final ArrayList<Step> output = new ArrayList<Step>();
    for (final Step step: mSim.getValidTransitions())
    {
      if (step.getEvent() == trans.getEvent()
          && (step.getSource().get(mAutomaton) == null || step.getSource().get(mAutomaton) == trans.getSource())
          && (step.getDest().get(mAutomaton) == null || step.getDest().get(mAutomaton) == trans.getTarget()))
      {
         output.add(step);
      }
    }
    return output;
  }

  // #########################################################################
  // # Event Handling Access methods

  public boolean canExecute()
  {
    return mFocusedItem != null && isEnabled(mFocusedItem);
  }

  public void execute(final Proxy proxyToFire)
  {
    if (proxyToFire != null && isEnabled(proxyToFire)) {
      final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
      final List<Step> possibleSteps = new ArrayList<Step>();
      if (proxyToFire instanceof IdentifierProxy) {
        for (final TransitionProxy trans : mAutomaton.getTransitions()) {
          final Proxy source = infomap.get(trans).getGraphSourceObject();
          if (source == proxyToFire &&
              eventCanBeFired(mSim, trans)) {
            possibleSteps.addAll(getSteps(trans));
          }
        }
      } else if (proxyToFire instanceof EdgeProxy) {
        for (final TransitionProxy trans : mAutomaton.getTransitions()) {
          final Proxy source = infomap.get(trans).getGraphSourceObject();
          final AbstractSubject subject = (AbstractSubject) source;
          final EdgeSubject edge =
            SubjectTools.getAncestor(subject, EdgeSubject.class);
          if (proxyToFire == edge && eventCanBeFired(mSim, trans)) {
            possibleSteps.addAll(getSteps(trans));
          }
        }
      }
      final Step firedEvent = findOptions(possibleSteps);
      if (firedEvent != null) {
        try {
          mSim.step(firedEvent);
        } catch (final NonDeterministicException exception) {
          mContainer.getIDE().error(exception.getMessage()
              + ". Event will not be fired");
        }
      }
    }
  }


  //##########################################################################
  //# Inner Class MouseHandler
  private class MouseHandler implements MouseListener, MouseMotionListener
  {

    //########################################################################
    //# Interface java.awt.event.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getClickCount() == 2)
        execute(mFocusedItem);
    }

    public void mouseEntered(final MouseEvent event)
    {
      updateFocusedItem(event);
    }

    public void mouseExited(final MouseEvent event)
    {
      updateFocusedItem(event);
    }

    public void mousePressed(final MouseEvent event)
    {
      factory.maybeShowPopup(AutomatonDisplayPane.this, event, mFocusedItem);
    }

    public void mouseReleased(final MouseEvent event)
    {
      // Do nothing
    }

    //########################################################################
    //# Interface java.awt.event.MouseMotionListener
    public void mouseDragged(final MouseEvent event)
    {
      updateFocusedItem(event);
    }

    public void mouseMoved(final MouseEvent event)
    {
      updateFocusedItem(event);
    }
  }


  //##########################################################################
  //# Inner Class ResizeHandler
  private class ResizeHandler extends ComponentAdapter
  {

    //########################################################################
    //# Inner Class ResizeHandler
    public void componentResized(final ComponentEvent event)
    {
      mTransform = mInverseTransform = null;
    }
  }


  //##########################################################################
  //# Inner Class SimulatorRenderingContext
  private class SimulatorRenderingContext extends ModuleRenderingContext
  {

    //#######################################################################
    //# Constructor
    private SimulatorRenderingContext()
    {
      super(mContainer.getModuleContext());
      final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
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
    public PropositionIcon.ColorInfo getColorInfo(final SimpleNodeProxy node)
    {
      // The spring embedder modifies a copy of our graph. When it is running,
      // the items being displayed are not in our compiled graph ...
      final Proxy orig = getOriginal(node);
      final StateProxy state = mStateMap.get(orig);
      if (state != null) {
        return mSim.getMarkingColorInfo(state, mAutomaton, true);
      } else {
        // This state was a victim of compiler optimisation ..,
        return PropositionIcon.getUnmarkedColors();
      }
    }

    public RenderingInformation getRenderingInformation(final Proxy proxy)
    {
      boolean proxyIsActive = false;
      boolean proxyIsEnabled = false;
      boolean proxyIsSelected = false;
      boolean proxyIsInvalid = false;
      // The spring embedder modifies a copy of our graph. When it is running,
      // the items being displayed are not in our compiled graph ...
      final Proxy orig = getOriginal(proxy);
      if (orig == null) {
        // *** BUG? ***
        // Identifiers have no original, this may cause failure to
        // highlight them while spring embedding.
        return super.getRenderingInformation(proxy);
      } else if (orig instanceof SimpleNodeProxy) {
        final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
        final StateProxy currentState = mSim.getCurrentStates().get(mAutomaton);
        if (infomap.get(currentState).getSourceObject() == orig) {
          proxyIsActive = true;
        }
        if (mFocusedItem != null)
        {
          if (orig == mFocusedItem) {
            proxyIsSelected = true;
          }
        }
        boolean found = (proxyIsActive);
        if (!found)
        {
          for (final StateProxy state : mAutomaton.getStates())
          {
            if (infomap.get(state).getSourceObject() == orig)
              found = true;
          }
        }
        if (!found)
          proxyIsInvalid = true;
      } else if (orig instanceof IdentifierProxy ||
                 orig instanceof EdgeProxy) {
        final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
        final TransitionProxy currentTrans =
          mSim.getPreviousTransition(mAutomaton);
        if (currentTrans != null) {
          final Proxy source = infomap.get(currentTrans).getGraphSourceObject();
          if (orig instanceof IdentifierProxy) {
            if (source == orig) {
              proxyIsActive = true;
            }
          } else {
            final AbstractSubject subject = (AbstractSubject) source;
            final EdgeSubject edge =
              SubjectTools.getAncestor(subject, EdgeSubject.class);
            if (edge == orig) {
              proxyIsActive = true;
            }
          }
        }
        if (isEnabled(orig)) {
          proxyIsEnabled = true;
        }
        if (mFocusedItem != null) {
          if (orig == mFocusedItem) {
            proxyIsSelected = true;
          } else if (orig instanceof IdentifierSubject) {
            final Subject subject = (Subject) orig;
            final EdgeSubject edge =
              SubjectTools.getAncestor(subject, EdgeSubject.class);
            if (mFocusedItem == edge) {
              proxyIsSelected = true;
            }
          }
        }
        boolean found = (proxyIsActive || proxyIsEnabled);
        if (!found) {
          found = mNonOptimizedProxy.contains(orig);
        }
        if (!found) {
          proxyIsInvalid = true;
        }
      } else if (orig instanceof ForeachEventSubject) {
        if (orig == mFocusedItem) {
          proxyIsSelected = true;
        } else {
          final Subject subject = (Subject) orig;
          final EdgeSubject edge =
            SubjectTools.getAncestor(subject, EdgeSubject.class);
          if (mFocusedItem == edge) {
            proxyIsSelected = true;
          }
        }
      } else if (orig instanceof LabelGeometryProxy) {
        if (mFocusedItem instanceof SimpleNodeProxy) {
          final LabelGeometryProxy label = ((SimpleNodeProxy)mFocusedItem).getLabelGeometry();
          if (orig == label) {
            proxyIsSelected = true;
          }
        }
        if (orig instanceof LabelGeometrySubject) {
          final SimpleNodeSubject parent = (SimpleNodeSubject)(((LabelGeometrySubject)orig).getParent());
          final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
          final StateProxy currentState = mSim.getCurrentStates().get(mAutomaton);
          if (infomap.get(currentState).getSourceObject() == parent)
            proxyIsActive = true;
          boolean found = (proxyIsActive);
          if (!found)
          {
            for (final StateProxy state : mAutomaton.getStates())
            {
              if (infomap.get(state).getSourceObject() == parent)
                found = true;
            }
          }
          if (!found)
            proxyIsInvalid = true;
        }
      }
      return getRawRenderingInformation
        (orig, proxyIsActive, proxyIsEnabled, proxyIsSelected, proxyIsInvalid);
    }

    //#######################################################################
    //# Auxiliary Methods
    private RenderingInformation getRawRenderingInformation
      (final Proxy orig, final boolean proxyIsActive,
       final boolean proxyIsEnabled, final boolean proxyIsSelected,
       final boolean proxyIsInvalid)
    {
      if (proxyIsInvalid)
        return new RenderingInformation(false, false, EditorColor.SIMULATION_INVALID,
                                        EditorColor.getShadowColor(orig, GraphPanel.DragOverStatus.NOTDRAG, false, false, false),
                                        getPriority(orig));
      if (proxyIsActive || proxyIsEnabled || proxyIsSelected) {
        final Color foreground;
        final Color shadow;
        if (proxyIsEnabled) {
          foreground = EditorColor.SIMULATION_ENABLED;
        } else if (proxyIsActive) {
          foreground = EditorColor.SIMULATION_ACTIVE;
        } else {
          foreground = EditorColor.DEFAULTCOLOR;
        }
        if (proxyIsSelected) {
          if (proxyIsEnabled) {
            shadow = EditorColor.SIMULATION_FOCUSED_SHADOW;
          } else {
            shadow = EditorColor.shadow(EditorColor.SIMULATION_DISABLED_FOCUSED);
          }
        } else {
          if (proxyIsActive) {
            shadow = EditorColor.shadow(EditorColor.SIMULATION_ACTIVE);
          } else if (proxyIsEnabled) {
            shadow = EditorColor.shadow(EditorColor.SIMULATION_ENABLED);
          } else {
            shadow = null;
          }
        }
        return new RenderingInformation(false, true, foreground, shadow,
                                        getPriority(orig));
      } else {
        return super.getRenderingInformation(orig);
      }
    }

    //########################################################################
    //# Data Members
    private final Map<SimpleNodeProxy,StateProxy> mStateMap;

  }
  ;

  //##########################################################################
  //# Data Members
  private final Simulation mSim;
  private final AutomatonProxy mAutomaton;
  private final ModuleContainer mContainer;
  private final AutomatonInternalFrame mParent;
  private final DisplayPanePopupFactory factory;
  private AffineTransform mTransform;
  private AffineTransform mInverseTransform;
  private Proxy mFocusedItem;
  private Set<Proxy> mEnabledProxy;
  private Set<Proxy> mNonOptimizedProxy;

  //##########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
