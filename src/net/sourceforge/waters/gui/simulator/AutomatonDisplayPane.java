package net.sourceforge.waters.gui.simulator;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.gui.BackupGraphPanel;
import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonDisplayPane
  extends BackupGraphPanel
  implements SimulationObserver
{

  //##########################################################################
  //# Constructors
  public AutomatonDisplayPane(final AutomatonProxy automaton,
                              final GraphSubject graph,
                              final ModuleContainer container,
                              final Simulation sim)
    throws GeometryAbsentException
  {
    super(graph, container.getModule());
    mSim = sim;
    mAutomaton = automaton;
    mContainer = container;
    final ModuleSubject module = container.getModule();
    final RenderingContext context = new SimulatorRenderingContext();
    final ProxyShapeProducer producer =
      new SubjectShapeProducer(graph, module, context);
    setShapeProducer(producer);
    final int width;
    final int height;
    if (ensureGeometryExists()) {
      // Spring embedder is running, guessing window size ...
      final int numstates = automaton.getStates().size();
      width = height = 136 + 24 * numstates;
    } else {
      final Rectangle2D imageRect = producer.getMinimumBoundingRectangle();
      width = (int) Math.ceil(imageRect.getWidth());
      height = (int) Math.ceil(imageRect.getHeight());
    }
    setPreferredSize(new Dimension(width, height));
    setBackground(EditorColor.BACKGROUNDCOLOR);
    sim.attach(this);
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.gui.simulator.SimulatorObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    repaint();
  }


  //##########################################################################
  //# Repainting
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
    g2d.setColor(Color.RED);
    g2d.setStroke(new BasicStroke(4f));
    g2d.scale(min, min);
    g2d.translate(-imageRect.getX(), -imageRect.getY());
    g2d.drawRect((int)imageRect.getX(), (int)imageRect.getY(), (int)imageRect.getWidth(), (int)imageRect.getHeight());
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
      return mSim.getMarkingColorInfo(state, mAutomaton);
    }

    public RenderingInformation getRenderingInformation(final Proxy proxy)
    {
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
          return getActiveRenderingInformation(orig);
        }
      }
      final TransitionProxy currentTrans = mSim.getPreviousTransition(mAutomaton);
      if (currentTrans != null) {
        final Proxy currentTransSource =
          mContainer.getSourceInfoMap().get(currentTrans).getSourceObject();
        if (orig instanceof IdentifierProxy) {
          if (currentTransSource == orig) {
            return getActiveRenderingInformation(orig);
          }
        } else if (orig instanceof EdgeProxy) {
          final IdentifierSubject ident =
            (IdentifierSubject) currentTransSource;
          if (ident.getAncestor(EdgeSubject.class) == orig) {
            return getActiveRenderingInformation(orig);
          }
        }
      }
      return super.getRenderingInformation(orig);
    }

    //#######################################################################
    //# Auxiliary Methods
    private RenderingInformation getActiveRenderingInformation
      (final Proxy proxy)
    {
      return new RenderingInformation
        (false, true,
         EditorColor.SIMULATION_ACTIVE,
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


  //#################################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
