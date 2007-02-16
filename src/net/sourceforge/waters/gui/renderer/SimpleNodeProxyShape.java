package net.sourceforge.waters.gui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

public class SimpleNodeProxyShape
    extends AbstractProxyShape
{
    private boolean isForbidden = false;
    
    public void draw(Graphics2D g, RenderingInformation status)
    {
        // Draw the filling (depends on marking)
        updateColors();
        Rectangle2D rect = getShape().getBounds();
        Arc2D arc;
        double i = 0;
        double degrees = ((double)360 / (double)mColors.size());
        if (mColors.isEmpty())
        {
            arc = new Arc2D.Double(rect, 0, 360, Arc2D.OPEN);
            g.setColor(FILLCOLOR);
            g.fill(arc);
        }
        else
        {
            for (Color c : mColors)
            {
                arc = new Arc2D.Double(rect, i, degrees, Arc2D.PIE);
                g.setColor(c);
                g.fill(arc);
                i += degrees;
            }
        }
        
        // Draw initial state arrow
	for (final Handle handle : mHandles) {
	  g.setColor(status.getColor());
	  handle.draw(g, status);
        }
        
        // Draw the basic shape (the outline)
        super.draw(g, status);
        
        // Cross out if forbidden
        if (isForbidden)
        //if (false)
        {
            g.setColor(EditorColor.ERRORCOLOR);
            g.setStroke(DOUBLESTROKE);
            g.drawLine((int) rect.getMaxX(), (int) rect.getMaxY(), (int) rect.getMinX(), (int) rect.getMinY());
            g.drawLine((int) rect.getMaxX(), (int) rect.getMinY(), (int) rect.getMinX(), (int) rect.getMaxY());
        }
    }
    
    /**
     * Updates the color set of marked nodes (I think).
     */
    private void updateColors()
    {
        mColors.clear();
        if (mModule != null)
        {
            Map<String, EventDeclProxy> map =
                new HashMap<String, EventDeclProxy>(mModule.getEventDeclList().size());
            final List<Proxy> list =
                getProxy().getPropositions().getEventList();
            if (list.isEmpty())
            {
                return;
            }
            for (EventDeclProxy e : mModule.getEventDeclList())
            {
                map.put(e.getName(), e);
            }
            for (final Proxy prop : list)
            {
                // BUG: ForeachEventSubject not supported!
                final IdentifierProxy p = (IdentifierProxy)prop;
                final EventDeclProxy decl = map.get(p.getName());
                if (decl == null)
                {
                    mColors.add(EditorColor.DEFAULTMARKINGCOLOR);
                    continue;
                }
                if (decl.getName().equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
                {
                    isForbidden = true;
                    continue;
                }
                final ColorGeometryProxy geo = decl.getColorGeometry();
                if (geo == null)
                {
                    mColors.add(EditorColor.DEFAULTMARKINGCOLOR);
                    continue;
                }
                mColors.addAll(geo.getColorSet());
            }
        }
    }
    
    public SimpleNodeProxyShape(SimpleNodeProxy proxy, ModuleProxy module)
    {
        super(proxy);
        mModule = module;
        Point2D p = getProxy().getPointGeometry().getPoint();
        Rectangle2D rect = new Rectangle2D.Double(p.getX() - RADIUS, p.getY() - RADIUS,
            WIDTH, WIDTH);
        mShape = new Arc2D.Double(rect, 0, 360, Arc2D.OPEN);
        if (proxy.isInitial()) {
	  final Handle handle = new InitialStateHandle(proxy);
	  mHandles = Collections.singletonList(handle);
        } else {
	  mHandles = Collections.emptyList();
	}
    }
    
    public Arc2D getShape()
    {
        return mShape;
    }
    
    public SimpleNodeProxy getProxy()
    {
        return (SimpleNodeProxy)super.getProxy();
    }
    
    public List<Handle> getHandles()
    {
        return mHandles;
    }
    
    private final List<Handle> mHandles;
    private Collection<Color> mColors = new ArrayList<Color>();
    private final ModuleProxy mModule;
    private final Arc2D mShape;
    
    public static int RADIUS = 6;
    public static int WIDTH = RADIUS * 2;
    private static Color FILLCOLOR = Color.WHITE;

}
