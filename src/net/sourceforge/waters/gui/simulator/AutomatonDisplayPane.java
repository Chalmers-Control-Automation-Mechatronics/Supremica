package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

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
import net.sourceforge.waters.model.module.GraphProxy;
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
    setBackground(EditorColor.BACKGROUNDCOLOR);
    mGraph = component.getGraph();
    mContext = context;
    final Rectangle2D imageRect = getShapeProducer().getMinimumBoundingRectangle();
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
    System.out.println("DEBUG: Painted");
    final Renderer renderer = new Renderer();
    final Rectangle2D imageRect = getShapeProducer().getMinimumBoundingRectangle();
    final Rectangle2D graphicsRect = g.getClipBounds();
    final double scaleX = graphicsRect.getWidth() / imageRect.getWidth();
    final double scaleY = graphicsRect.getHeight() / imageRect.getHeight();
    final double min = Math.min(scaleX, scaleY);
    final AffineTransform trans = ((Graphics2D)g).getTransform();
    ((Graphics2D)g).scale(min, min);
    renderer.renderGraph(mGraph, new ArrayList<MiscShape>(), this,
        getShapeProducer(), (Graphics2D)g);
    System.out.println("DEBUG: Clip Bounds are " + g.getClipBounds());
    ((Graphics2D)g).setTransform(trans);
  }

  private ProxyShapeProducer getShapeProducer()
  {
    return new SubjectShapeProducer ((GraphSubject) mGraph, mContext);
  }


  //#################################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private final GraphProxy mGraph;
  private final ModuleContext mContext;


}
