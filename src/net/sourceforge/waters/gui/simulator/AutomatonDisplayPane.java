package net.sourceforge.waters.gui.simulator;

import java.awt.Graphics;
import java.awt.Graphics2D;
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
    mGraph = component.getGraph();
    mContext = context;
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


  protected void paintComponent(final Graphics g)
  {
    final Renderer renderer = new Renderer();
    renderer.renderGraph(mGraph, new ArrayList<MiscShape>(), this,
        getShapeProducer(), (Graphics2D)g);
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
