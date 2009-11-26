package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

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
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.module.GraphSubject;

import org.supremica.gui.ide.ModuleContainer;

public class AutomatonDisplayPane extends JPanel implements Renderable
{

  public AutomatonDisplayPane(final AutomatonProxy automaton, final ModuleContainer container)
  {
    super();
    final ModuleContext context = container.getModuleContext();
    final SimpleComponentProxy component = (SimpleComponentProxy) container.getSourceInfoMap().get(automaton).getSourceObject();
    mGraph = (GraphSubject) component.getGraph();
    mContext = context;
    setBackground(EditorColor.BACKGROUNDCOLOR);
    mShapeProducer = new SubjectShapeProducer(mGraph, mContext);
    final Rectangle2D imageRect = mShapeProducer.getMinimumBoundingRectangle();
    setPreferredSize(new Dimension((int)imageRect.getWidth(), (int)imageRect.getHeight()));
  }

  public RenderingInformation getRenderingInformation(final Proxy proxy)
  {
    final boolean hasfocus = isFocusOwner();
    return new RenderingInformation
      (false, false,
       EditorColor.getColor(proxy, DRAGOVERSTATUS.NOTDRAG,
                            false, false, hasfocus),
       EditorColor.getShadowColor(proxy, DRAGOVERSTATUS.NOTDRAG,
                                  false, false, hasfocus),
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

}
