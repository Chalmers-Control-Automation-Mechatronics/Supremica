package net.sourceforge.waters.gui.renderer;

import java.awt.Rectangle;
import java.util.Collections;
import java.awt.Font;

import java.awt.geom.RoundRectangle2D;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;

public class ProxyShapeProducer
    extends AbstractModuleProxyVisitor
{
    public ProxyShapeProducer(ModuleProxy m)
    {
        mModule = m;
        mMap = Collections.synchronizedMap(new IdentityHashMap<Object, ProxyShape>());
    }
    
    public SimpleNodeProxyShape visitSimpleNodeProxy(SimpleNodeProxy n)
    {
        LabelProxyShape label = (LabelProxyShape)mMap.get(n.getName());
        if (label == null)
        {
            label = new LabelProxyShape(n, DEFAULT);
            mMap.put(n.getName(), label);
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
            int height = 2;
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
                
                LabelShape ls = new LabelShape(p, x, y + height, font);
                mMap.put(p, ls);
                height += ls.getShape().getHeight();
                
                if (width < ls.getShape().getWidth())
                {
                    width = (int)ls.getShape().getWidth();
                }
            }
            height += 2;
            
            RoundRectangle2D mBounds = new RoundRectangle2D.Double(
                x, y, width, height,
                LabelBlockProxyShape.DEFAULTARCW,
                LabelBlockProxyShape.DEFAULTARCH);
            s = new LabelBlockProxyShape(l, mBounds);
            mMap.put(l, s);
        }
        return shape;
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
    
    public ProxyShape getShape(Object o) throws VisitorException
    {
        if (o instanceof Proxy)
        {
            return (ProxyShape)((Proxy)o).acceptVisitor(this);
        }
        return mMap.get(o);
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
    
    protected Map<Object, ProxyShape> getMap()
    {
        return mMap;
    }
    
    private final ModuleProxy mModule;
    private final Map<Object, ProxyShape> mMap;
    
    public static final Font DEFAULT = new Font("Dialog", Font.PLAIN, 12);
    public static final Font UNCONTROLLABLE = DEFAULT.deriveFont(Font.ITALIC);
}
