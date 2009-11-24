package net.sourceforge.waters.gui.simulator;

import javax.swing.JPanel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.EditorSurface.DRAGOVERSTATUS;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.Renderable;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;

public class AutomatonDisplayPane extends JPanel implements Renderable
{

  public AutomatonDisplayPane(final AutomatonProxy automaton)
  {
    super();
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

  /*
  protected void paintComponent(final Graphics g)
  {
    final Renderer renderer = new Renderer();
    renderer.renderGraph(getGraph(), new ArrayList<MiscShape>(), this,
        getShapeProducer(), (Graphics2D)g);
  }
  */
  private GraphProxy getGraph()
  {
    throw new UnsupportedOperationException();
  }

  private ProxyShapeProducer getShapeProducer()
  {
    //return new SubjectShapeProducer (getGraph(), new ModuleContext());
    return null;
  }

}
