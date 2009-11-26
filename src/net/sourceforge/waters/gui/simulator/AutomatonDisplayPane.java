package net.sourceforge.waters.gui.simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.EditorSurface.DRAGOVERSTATUS;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.Renderable;
import net.sourceforge.waters.gui.renderer.Renderer;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.gui.ide.ModuleContainer;

public class AutomatonDisplayPane extends JPanel implements Renderable, SimulationObserver
{

  private final Simulation mSim;
  private final AutomatonProxy mAutomaton;
  private final ModuleContainer mContainer;

  public AutomatonDisplayPane(final AutomatonProxy automaton, final ModuleContainer container, final Simulation sim)
  {
    super();
    mContainer = container;
    final ModuleContext context = container.getModuleContext();
    final SimpleComponentProxy component = (SimpleComponentProxy) container.getSourceInfoMap().get(automaton).getSourceObject();
    mGraph = (GraphSubject) component.getGraph();
    mContext = context;
    setBackground(EditorColor.BACKGROUNDCOLOR);
    mShapeProducer = new SubjectShapeProducer(mGraph, mContext);
    final Rectangle2D imageRect = mShapeProducer.getMinimumBoundingRectangle();
    setPreferredSize(new Dimension((int)imageRect.getWidth(), (int)imageRect.getHeight()));
    setBorder(BorderFactory.createLineBorder(Color.black));
    sim.attach(this);
    mSim = sim;
    mAutomaton = automaton;
  }

  public RenderingInformation getRenderingInformation(final Proxy proxy)
  {
    final StateProxy currentState = mSim.getCurrentStates().get(mAutomaton);
    final TransitionProxy currentTrans = mSim.getPreviousTransition(mAutomaton);
    if (proxy.getClass() == SimpleNodeSubject.class)
    {
      if (mContainer.getSourceInfoMap().get(currentState).getSourceObject() == proxy)
        return getRenderingInformation(true, proxy);
    }
    if (proxy.getClass() == SimpleIdentifierSubject.class && currentTrans != null)
    {
      if (mContainer.getSourceInfoMap().get(currentTrans).getSourceObject() == proxy)
        return getRenderingInformation(true, proxy);
    }
    if (proxy.getClass() == EdgeSubject.class && currentTrans != null)
    {
      if (((IdentifierSubject)mContainer.getSourceInfoMap().get(currentTrans).getSourceObject()).getAncestor(EdgeSubject.class) == proxy)
        return getRenderingInformation(true, proxy);
    }
    return getRenderingInformation(false, proxy);
  }

  private RenderingInformation getRenderingInformation(final boolean active, final Proxy proxy)
  {
    return new RenderingInformation
    (false, false,
     EditorColor.getColor(proxy, DRAGOVERSTATUS.NOTDRAG,
                          active, false, active),
     EditorColor.getShadowColor(proxy, DRAGOVERSTATUS.NOTDRAG,
                          active, false, active),
     getPriority(proxy));
  }

  protected int getPriority(final Proxy o)
  {
      int priority = 0;
      if (o instanceof EdgeProxy)
      {
          priority = 1;
      }
      else if (o instanceof NodeProxy)
      {
          priority = 2;
      }
      else if (o instanceof LabelGeometryProxy)
      {
          priority = 3;
      }
      else if (o instanceof LabelBlockProxy)
      {
          priority = 4;
      }
      else if (o instanceof GuardActionBlockProxy)
      {
          priority = 5;
      }
      else if (o instanceof IdentifierProxy)
      {
          priority = 6;
      }
      return priority;
  }


  public void paint(final Graphics g)
  {
    super.paint(g);
    final Graphics2D g2d = (Graphics2D) g;
    final AffineTransform trans = g2d.getTransform();
    final Renderer renderer = new Renderer();
    final Rectangle2D imageRect = mShapeProducer.getMinimumBoundingRectangle();
    final Dimension panelSize = getSize();
    final double scaleX = panelSize.getWidth() / imageRect.getWidth();
    final double scaleY = panelSize.getHeight() / imageRect.getHeight();
    final double min = Math.min(scaleX, scaleY);
    g2d.scale(min, min);
    final List<MiscShape> empty = Collections.emptyList();
    renderer.renderGraph(mGraph, empty, this, mShapeProducer, g2d);
    g2d.setTransform(trans);
  }


  //#################################################################################
  //# Data Members
  private final GraphSubject mGraph;
  private final ModuleContext mContext;
  private final ProxyShapeProducer mShapeProducer;


  //#################################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

  public void simulationChanged(final SimulationChangeEvent event)
  {
    paint(getGraphics());
  }

}
