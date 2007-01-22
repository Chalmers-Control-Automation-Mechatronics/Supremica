package net.sourceforge.waters.gui.renderer;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.awt.Font;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;

public class ProxyShapeProducer
    extends AbstractModuleProxyVisitor
{
    public ProxyShapeProducer(ModuleProxy m)
    {
        mModule = m;
        mMap = Collections.synchronizedMap(new IdentityHashMap<Proxy, ProxyShape>());
    }
    
    public SimpleNodeProxyShape visitSimpleNodeProxy(SimpleNodeProxy n)
    {
        LabelProxyShape label = (LabelProxyShape)mMap.get(n.getLabelGeometry());
        if (label == null)
        {
          label = new LabelProxyShape(n, DEFAULT);
          mMap.put(n.getLabelGeometry(), label);
        }
        SimpleNodeProxyShape s = (SimpleNodeProxyShape)mMap.get(n);
        if (s == null)
        {
          s = new SimpleNodeProxyShape(n, mModule);
          mMap.put(n, s);
        }
        return s;
    }
    
    public EdgeProxyShape visitEdgeProxy(EdgeProxy e)
    {
        EdgeProxyShape shape = (EdgeProxyShape)mMap.get(e);
        if (shape == null)
        {
            if (e.getStartPoint().getPoint().equals(e.getEndPoint().getPoint()))
            {
                shape = new EdgeProxyShape.Tear(e);
            }
            else
            {
                shape = new EdgeProxyShape.QuadCurve(e);
            }
            mMap.put(e, shape);
        }
        LabelBlockProxy l = e.getLabelBlock();
        LabelBlockProxyShape s = (LabelBlockProxyShape)mMap.get(l);
        if (s == null)
        {
            EdgeProxyShape edge = (EdgeProxyShape)mMap.get(e);
            int height = 1;
            int width = 0;
            int x;
            int y;
            if (l.getGeometry() != null)
            {
                x = (int)l.getGeometry().getOffset().getX();
                y = (int)l.getGeometry().getOffset().getY();
            }
            else
            {
                x = LabelBlockProxyShape.DEFAULTOFFSETX;
                y = LabelBlockProxyShape.DEFAULTOFFSETY;
            }
            x += edge.getTurningPoint().getX();
            y += edge.getTurningPoint().getY();
            for (Proxy p : l.getEventList())
            {
                // Use different font for different event kinds.
                Font font = DEFAULT;
                if (p instanceof SimpleIdentifierSubject)
                {
                    SimpleIdentifierSubject identifier = (SimpleIdentifierSubject) p;
                    for (EventDeclProxy event: mModule.getEventDeclList())
                    {
                        if (event.getName().equals(identifier.getName()))
                        {
                            if (event.getKind() == EventKind.UNCONTROLLABLE)
                            {
                                font = UNCONTROLLABLE;
                            }
                            break;
                        }
                    }
                }
                
                LabelShape ls = new LabelShape(p, x + 1, y + height, font);
                mMap.put(p, ls);
                height += ls.getShape().getHeight();
                
                if (width < ls.getShape().getWidth())
                {
                    width = (int)ls.getShape().getWidth();
                }
            }
            height += 3;
            width += 3;
            RoundRectangle2D mBounds = new RoundRectangle2D.Double(
                x, y, width, height,
                LabelBlockProxyShape.DEFAULTARCW,
                LabelBlockProxyShape.DEFAULTARCH);
            s = new LabelBlockProxyShape(l, mBounds);
            mMap.put(l, s);
        }
        
        GuardActionBlockProxy GA = e.getGuardActionBlock();
        GuardActionBlockProxyShape GAShape = (GuardActionBlockProxyShape) mMap.get(GA);
        if(GAShape==null && GA != null) 
        {
            double x, y;
            //get the edge
            EdgeProxyShape edgeProxyShape = (EdgeProxyShape) mMap.get(e);
            
            //find out offset
            if(GA.getGeometry() == null)
            {         
                LabelGeometrySubject offset =
                    new LabelGeometrySubject
                    (new Point(GuardActionBlockProxyShape.DEFAULTOFFSETX,
                    (int) (l.getGeometry().getOffset().getY() + s.getShape().getHeight())));
                ((EdgeSubject) e).getGuardActionBlock().setGeometry(offset);
            }
            x = GA.getGeometry().getOffset().getX();
            y = GA.getGeometry().getOffset().getY();
            x += edgeProxyShape.getTurningPoint().getX();
            y += edgeProxyShape.getTurningPoint().getY();
            
            //create content
            int width = 0;
            int height = 2;
            Font font = DEFAULT;
            
            //guard
            List<SimpleExpressionProxy> guards = GA.getGuards();
            if(!guards.isEmpty())
            {
                SimpleExpressionProxy guard = guards.get(0); //there should only be one guard expression.
                LabelShape ls = new LabelShape(guard , (int) x, (int) y + height, font, "guard");
                mMap.put(guard, ls);
                height += ls.getShape().getHeight();
                if (width < ls.getShape().getWidth())
                {
                    width = (int)ls.getShape().getWidth();
                }
                height += 2;
            }
            
            //actions
            List<BinaryExpressionProxy> actions = GA.getActions();
            for(BinaryExpressionProxy action : actions)
            {
                LabelShape ls = new LabelShape(action , (int) x, (int) y + height, font, "action");
                mMap.put(action, ls);
                height += ls.getShape().getHeight();
                if (width < ls.getShape().getWidth())
                {
                    width = (int)ls.getShape().getWidth();
                }
            }
            
            //create shape
            RoundRectangle2D mBounds = new RoundRectangle2D.Double(
                x, y, width, height,
                GuardActionBlockProxyShape.DEFAULTARCW,
                GuardActionBlockProxyShape.DEFAULTARCH);
            GAShape = new GuardActionBlockProxyShape(GA, mBounds);
            mMap.put(GA, GAShape);
        }
        return shape;
    }
    
    public LabeledLabelBlockProxyShape visitGraphProxy(GraphProxy g)
    {
      if (g.getBlockedEvents() == null) {
        return null;
      }
      LabelBlockProxy l = g.getBlockedEvents();
      LabeledLabelBlockProxyShape s = (LabeledLabelBlockProxyShape)mMap.get(l);
      if (s == null)
      {
          int height = 1;
          int width = 0;
          int x = 0;
          int y = 0;
          if (l.getGeometry() != null)
          {
              x = (int)l.getGeometry().getOffset().getX();
              y = (int)l.getGeometry().getOffset().getY();
          }
          else
          {
              x = LabelBlockProxyShape.DEFAULTOFFSETX;
              y = LabelBlockProxyShape.DEFAULTOFFSETY;
          }
          Font font = BOLD;
          String blocked = "BLOCKED:";
          TextLayout text = new TextLayout(blocked, font,
                                           new FontRenderContext(null, true, true));
          width = (int)text.getBounds().getWidth();
          for (Proxy p : l.getEventList())
          {
              // Use different font for different event kinds.
              font = DEFAULT;
              if (p instanceof SimpleIdentifierSubject)
              {
                  SimpleIdentifierSubject identifier = (SimpleIdentifierSubject) p;
                  for (EventDeclProxy event: mModule.getEventDeclList())
                  {
                      if (event.getName().equals(identifier.getName()))
                      {
                          if (event.getKind() == EventKind.UNCONTROLLABLE)
                          {
                              font = UNCONTROLLABLE;
                          }
                          break;
                      }
                  }
              }
              
              LabelShape ls = new LabelShape(p, x + 1, y + height, font);
              mMap.put(p, ls);
              height += ls.getShape().getHeight();
              
              if (width < ls.getShape().getWidth())
              {
                  width = (int)ls.getShape().getWidth();
              }
          }
          height += 4;
          width += 3;
          RoundRectangle2D mBounds = new RoundRectangle2D.Double(
              x, y, width, height,
              LabelBlockProxyShape.DEFAULTARCW,
              LabelBlockProxyShape.DEFAULTARCH);
          s = new LabeledLabelBlockProxyShape(l, mBounds, blocked, DEFAULT);
          mMap.put(l, s);
      }
      return s;
    }
    
    public GroupNodeProxyShape visitGroupNodeProxy(GroupNodeProxy g)
    {
        GroupNodeProxyShape s = (GroupNodeProxyShape)mMap.get(g);
        if (s == null)
        {
            s = new GroupNodeProxyShape(g);
            mMap.put(g, s);
        }
        return s;
    }
    
    public ProxyShape getShape(Proxy p) throws VisitorException
    {
      if (p != null) {
        return (ProxyShape)p.acceptVisitor(this);
      } else {
        return null;
      }
    }
    
    public ProxyShape visitProxy(Proxy p) throws VisitorException
    {
        ProxyShape s = mMap.get(p);
        if (s == null)
        {
            throw new VisitorException(p + " is not in the map");
        }
        return s;
    }
    
    public Rectangle getMinimumBoundingRectangle()
    {
        Collection<ProxyShape> shapes = mMap.values();
        Rectangle rect = new Rectangle(0,0,0,0);
        synchronized(mMap)
        {
            for (ProxyShape shape : shapes)
            {
                rect.add(shape.getShape().getBounds2D());
            }
        }
        return rect;
    }
    
    protected Map<Proxy, ProxyShape> getMap()
    {
        return mMap;
    }
    
    private final ModuleProxy mModule;
    private final Map<Proxy, ProxyShape> mMap;
    
    public static final Font DEFAULT = new Font("Dialog", Font.PLAIN, 12);
    public static final Font BOLD = DEFAULT.deriveFont(Font.BOLD);
    public static final Font UNCONTROLLABLE = DEFAULT.deriveFont(Font.ITALIC);
}
