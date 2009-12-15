package net.sourceforge.waters.gui.simulator;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import net.sourceforge.waters.gui.BackupGraphPanel;
import net.sourceforge.waters.gui.EditorColor;
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
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
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
    hoveredEdge = null;
    hoveredLabel = null;
    final ModuleSubject module = container.getModule();
    final RenderingContext context = new SimulatorRenderingContext();
    final ProxyShapeProducer producer =
      new SubjectShapeProducer(graph, module, context);
    setShapeProducer(producer);
    final int width;
    final int height;
    if (ensureGeometryExists()) {
      // Spring embedder is running, guessing window size ...
      final int numstates = aut.getStates().size();
      width = height = 128 + 32 * numstates;
    } else {
      final Rectangle2D imageRect = producer.getMinimumBoundingRectangle();
      width = (int) Math.ceil(imageRect.getWidth());
      height = (int) Math.ceil(imageRect.getHeight());
    }
    setPreferredSize(new Dimension(width, height));
    setBackground(EditorColor.BACKGROUNDCOLOR);
    sim.attach(this);
    this.addMouseListener(new MouseListener(){

      public void mouseClicked(final MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          final ArrayList<EventProxy> possibleEvents = new ArrayList<EventProxy>();
          if (hoveredLabel != null)
          {
            for (final TransitionProxy trans : mAutomaton.getTransitions())
            {
              final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
              final SimpleIdentifierSubject identifier = (SimpleIdentifierSubject) infomap.get(trans).getSourceObject();
              if (identifier == hoveredLabel && mSim.getValidTransitions().contains(trans.getEvent()))
                possibleEvents.add(trans.getEvent());
            }
          }
          else if (hoveredEdge != null)
          {
            for (final TransitionProxy trans : mAutomaton.getTransitions())
            {
              final Map<Proxy,SourceInfo> infomap = mContainer.getSourceInfoMap();
              final SimpleIdentifierSubject identifier = (SimpleIdentifierSubject) infomap.get(trans).getSourceObject();
              if (hoveredEdge.getLabelBlock().getEventList().contains(identifier) && mSim.getValidTransitions().contains(trans.getEvent()))
                possibleEvents.add(trans.getEvent());
            }
          }
          final EventProxy firedEvent = findOptions(possibleEvents);
          if (firedEvent != null)
            try {
              mSim.step(firedEvent);
            } catch (final UncontrollableException exception) {
              System.out.println("ERROR: " + exception.getMessage() + ". Event will not be fired");
            }
        }
      }

      public void mouseEntered(final MouseEvent e)
      {
        final EdgeProxy newhoveredEdge = getNearestEdge(e);
        if (newhoveredEdge != hoveredEdge)
        {
          hoveredEdge = newhoveredEdge;
          repaint();
        }
        final IdentifierProxy newHoveredLabel = getNearestLabel(e);
        if (newHoveredLabel != hoveredLabel)
        {
          hoveredLabel = newHoveredLabel;
          repaint();
        }
      }

      public void mouseExited(final MouseEvent e)
      {
        if (hoveredEdge != null)
        {
          hoveredEdge = null;
          repaint();
        }
        if (hoveredLabel != null)
        {
          hoveredLabel = null;
          repaint();
        }
      }

      public void mousePressed(final MouseEvent e)
      {
        // Do nothing
      }

      public void mouseReleased(final MouseEvent e)
      {
        // Do nothing
      }

    });
    this.addMouseMotionListener(new MouseMotionListener(){

      public void mouseDragged(final MouseEvent e)
      {
        final EdgeProxy newhoveredEdge = getNearestEdge(e);
        if (newhoveredEdge != hoveredEdge)
        {
          hoveredEdge = newhoveredEdge;
          repaint();
        }
        final IdentifierProxy newHoveredLabel = getNearestLabel(e);
        if (newHoveredLabel != hoveredLabel)
        {
          hoveredLabel = newHoveredLabel;
          repaint();
        }
      }

      public void mouseMoved(final MouseEvent e)
      {
        final EdgeProxy newhoveredEdge = getNearestEdge(e);
        if (newhoveredEdge != hoveredEdge)
        {
          hoveredEdge = newhoveredEdge;
          repaint();
        }
        final IdentifierProxy newHoveredLabel = getNearestLabel(e);
        if (newHoveredLabel != hoveredLabel)
        {
          hoveredLabel = newHoveredLabel;
          repaint();
        }
      }
    });
  }

  //##########################################################################
  //# Simple Access
  public Rectangle2D getMinimumBoundingRectangle()
  {
    return getShapeProducer().getMinimumBoundingRectangle();
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.gui.simulator.SimulatorObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    repaint();
  }


  //##########################################################################
  //# Class BackupGraphPanel
  public void paint(final Graphics g)
  {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    final Graphics2D g2d = (Graphics2D) g;
    final AffineTransform trans = g2d.getTransform();
    final ProxyShapeProducer producer = getShapeProducer();
    final Rectangle2D imageRect = producer.getMinimumBoundingRectangle();
    final Dimension panelSize = getSize();
    final double scaleX = panelSize.getWidth() / imageRect.getWidth();
    final double scaleY = panelSize.getHeight() / imageRect.getHeight();
    final double min = Math.min(scaleX, scaleY);
    g2d.scale(min, min);
    g2d.translate(-imageRect.getX(), -imageRect.getY());
    super.paint(g2d);
    g2d.setTransform(trans);
  }

  protected void paintGrid(final Graphics g)
  {
  }

  public void close()
  {
    mSim.detach(this);
    super.close();
  }

  public void embedderChanged(final EmbedderEvent event)
  {
    super.embedderChanged(event);
    if (event.getType() == EmbedderEventType.EMBEDDER_STOP) {
      mParent.storeReferenceFrame();
      mParent.adjustSize(false);
      mParent.storeReferenceFrame();
    }
  }

  // ##########################################################################
  // # Auxillary Functions

  public EdgeProxy getNearestEdge(final MouseEvent e)
  {
    for (final EdgeProxy edge : getGraph().getEdges())
    {
      final ProxyShape shape = getShapeProducer().getShape(edge);
      if (shape.isClicked(e.getX(), e.getY()))
      {
        System.out.println("DEBUG: Edge Detected");
        return edge;
      }
    }
    return null;
  }
  public IdentifierProxy getNearestLabel(final MouseEvent e)
  {
    for (final EdgeProxy edge : getGraph().getEdges())
    {
      for (final Proxy identifier : edge.getLabelBlock().getEventList())
      {
        if (identifier.getClass() == ForeachEventProxy.class)
        {
          throw new UnsupportedOperationException("Foreach block selection not supported");
        }
        else
        {
          final IdentifierProxy identifierProxy = (IdentifierProxy)identifier;
          final ProxyShape shape = getShapeProducer().getShape(identifierProxy);
          if (shape.isClicked(e.getX(), e.getY()))
          {
            return identifierProxy;
          }
        }
      }
    }
    return null;
  }

  private EventProxy findOptions(final ArrayList<EventProxy> possibleEvents)
  {
    if (possibleEvents.size() == 0)
      return null;
    else if (possibleEvents.size() == 1)
      return possibleEvents.get(0);
    else
    {
      final JLabel[] possibilities = new JLabel[possibleEvents.size()];
      final EventProxy[] events = new EventProxy[possibleEvents.size()];
      for (int looper = 0; looper < possibleEvents.size(); looper++)
      {
        final EventProxy event = possibleEvents.get(looper);
        final JLabel toAdd = new JLabel(event.getName());
        if (event.getKind() == EventKind.CONTROLLABLE)
          toAdd.setIcon(IconLoader.ICON_CONTROLLABLE);
        else if (event.getKind() == EventKind.UNCONTROLLABLE)
          toAdd.setIcon(IconLoader.ICON_UNCONTROLLABLE);
        else
          toAdd.setIcon(IconLoader.ICON_PROPOSITION);
        possibilities[looper] = toAdd;
        events[looper] = event;
      }
      final EventChooserDialog dialog = new EventChooserDialog(mContainer.getIDE(), possibilities, events);
      dialog.setVisible(true);
      final EventProxy event = dialog.getSelectedEvent();
      if ((event != null && !dialog.wasCancelled())) {
        for (final EventProxy findEvent : possibleEvents) {
          if (findEvent == event)
            return event;
        }
      }
      return null;
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
        return mSim.getMarkingColorInfo(state, mAutomaton);
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
      // The spring embedder modifies a copy of our graph. When it is running,
      // the items being displayed are not in our compiled graph ...
      final Proxy orig = getOriginal(proxy);
      if (orig == null) {
        // *** BUG? ***
        // Identifiers have no original, this may cause failure to
        // highlight them while spring embedding.
        return super.getRenderingInformation(proxy);
      } else if (orig instanceof SimpleNodeProxy) {
        final StateProxy currentState = mSim.getCurrentStates().get(mAutomaton);
        if (mContainer.getSourceInfoMap().get(currentState).getSourceObject() ==
            orig) {
          proxyIsActive = true;
        }
      }
      else
      {
        final TransitionProxy currentTrans = mSim.getPreviousTransition(mAutomaton);
        if (currentTrans != null) {
          final Proxy currentTransSource =
            mContainer.getSourceInfoMap().get(currentTrans).getSourceObject();
          if (orig instanceof IdentifierProxy) {
            if (currentTransSource == orig) {
              proxyIsActive = true;
            }
          } else if (orig instanceof EdgeProxy) {
            final IdentifierSubject ident =
              (IdentifierSubject) currentTransSource;
            if (ident.getAncestor(EdgeSubject.class) == orig) {
              proxyIsActive = true;
            }
          }
        }
      }
      for (final EventProxy event : mSim.getValidTransitions())
      {
        for (final TransitionProxy trans : mAutomaton.getTransitions())
        {
          if (trans.getEvent() == event && trans.getSource() == mSim.getCurrentStates().get(mAutomaton))
          {
            final Proxy currentTransSource =
              mContainer.getSourceInfoMap().get(trans).getSourceObject();
            if (orig instanceof IdentifierProxy) {
              if (currentTransSource == orig) {
                proxyIsEnabled = true;
              }
            } else if (orig instanceof EdgeProxy) {
              final IdentifierSubject ident =
                (IdentifierSubject) currentTransSource;
              if (ident.getAncestor(EdgeSubject.class) == orig) {
                proxyIsEnabled = true;
              }
            }
          }
        }
      }
      for (final EventProxy event : mSim.getValidTransitions())
      {
        for (final TransitionProxy trans : mAutomaton.getTransitions())
        {
          if (trans.getEvent() == event && trans.getSource() == mSim.getCurrentStates().get(mAutomaton))
          {
            final Proxy currentTransSource =
              mContainer.getSourceInfoMap().get(trans).getSourceObject();
            if (orig instanceof IdentifierProxy) {
              if (currentTransSource == orig && orig == hoveredLabel) {
                proxyIsSelected = true;
              }
            } else if (orig instanceof EdgeProxy) {
              final IdentifierSubject ident =
                (IdentifierSubject) currentTransSource;
              if (ident.getAncestor(EdgeSubject.class) == orig && (ident.getAncestor(EdgeSubject.class) == hoveredEdge)) {
                proxyIsSelected = true;
              }
            }
          }
        }
      }
      return getRawRenderingInformation(orig, proxyIsActive, proxyIsEnabled, proxyIsSelected);
    }



    //#######################################################################
    //# Auxiliary Methods

    private RenderingInformation getRawRenderingInformation(final Proxy orig, final boolean proxyIsActive, final boolean proxyIsEnabled, final boolean proxyIsSelected)
    {
      if (proxyIsActive && proxyIsSelected && proxyIsEnabled)
        return getEverythingRenderingInformation(orig);
      if (proxyIsEnabled && proxyIsSelected)
      {
        return getSelectedEnabledRenderingInformation(orig);
      }
      if (proxyIsActive && !proxyIsEnabled)
      {
        return getActiveRenderingInformation(orig);
      }
      if (proxyIsEnabled && !proxyIsActive)
      {
        return getEnabledRenderingInformation(orig);
      }
      if (proxyIsEnabled && proxyIsActive)
        return getActiveEnabledRenderingInformation(orig);
      return super.getRenderingInformation(orig);
    }


    private RenderingInformation getEverythingRenderingInformation(final Proxy orig)
    {
      return new RenderingInformation
      (false, true,
       EditorColor.SIMULATION_EVERYTHING,
       EditorColor.shadow(EditorColor.SIMULATION_EVERYTHING),
       getPriority(orig));
    }

    private RenderingInformation getSelectedEnabledRenderingInformation(
        final Proxy orig)
    {
      return new RenderingInformation
      (false, true,
          EditorColor.SIMULATION_SELECTED,
          EditorColor.shadow(EditorColor.SIMULATION_SELECTED),
          getPriority(orig));
    }

    private RenderingInformation getActiveRenderingInformation
      (final Proxy proxy)
    {
      return new RenderingInformation
        (false, true,
         EditorColor.SIMULATION_ACTIVE,
         EditorColor.shadow(EditorColor.SIMULATION_ACTIVE),
         getPriority(proxy));
    }

    private RenderingInformation getEnabledRenderingInformation
      (final Proxy proxy)
    {
      return new RenderingInformation
         (false, true,
          EditorColor.SIMULATION_ENABLED,
          EditorColor.shadow(EditorColor.SIMULATION_ENABLED),
          getPriority(proxy));
    }

    private RenderingInformation getActiveEnabledRenderingInformation
      (final Proxy proxy)
    {
      return new RenderingInformation
         (false, true,
          EditorColor.SIMULATION_ENABLED,
          EditorColor.shadow(EditorColor.SIMULATION_ACTIVE),
          getPriority(proxy));
    }

    //########################################################################
    //# Data Members
    private final Map<SimpleNodeProxy,StateProxy> mStateMap;

  }

  //#################################################################################
  //# Data Members
  private final Simulation mSim;
  private final AutomatonProxy mAutomaton;
  private final ModuleContainer mContainer;
  private final AutomatonInternalFrame mParent;
  private EdgeProxy hoveredEdge;
  private IdentifierProxy hoveredLabel;

  //#################################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
