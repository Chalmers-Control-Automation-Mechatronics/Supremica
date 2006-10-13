package net.sourceforge.waters.gui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import java.awt.geom.Arc2D;
import java.awt.geom.Arc2D.Double;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collection;
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
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import net.sourceforge.waters.gui.renderer.Handle.HandleType;

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
        g.setColor(status.getColor());
        if (isInitial)
        {
            mHandles.get(0).draw(g, status);
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
                final ColorGeometryProxy geo = decl.getColorGeometry();
                if (geo == null)
                {
                    mColors.add(EditorColor.DEFAULTMARKINGCOLOR);
                    continue;
                }
                if (decl.getName().equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
                {
                    isForbidden = true;
                    continue;
                }
                mColors.addAll(geo.getColorSet());
            }
        }
    }
    
    public SimpleNodeProxyShape(SimpleNodeProxy proxy, ModuleProxy module)
    {
        super(proxy);
        mHandles = new ArrayList<Handle>(1);
        mModule = module;
        Point2D p = getProxy().getPointGeometry().getPoint();
        Rectangle2D rect = new Rectangle2D.Double(p.getX() - RADIUS, p.getY() - RADIUS,
            WIDTH, WIDTH);
        mShape = new Arc2D.Double(rect, 0, 360, Arc2D.OPEN);
        isInitial = getProxy().isInitial();
        if (proxy.getInitialArrowGeometry() != null)
        {
            mArrow = getProxy().getInitialArrowGeometry().getPoint();
        }
        else
        {
            mArrow = new Point(-5, -5); // Why 5?
        }
        if (isInitial)
        {
            mHandles.add(new InitialStateHandle(Math.atan2(mArrow.getY(), mArrow.getX()), p, RADIUS));
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
    
    private List<Handle> mHandles;
    private Collection<Color> mColors = new ArrayList<Color>();
    private final ModuleProxy mModule;
    private final Arc2D mShape;
    private final boolean isInitial;
    private final Point2D mArrow;
    
    public static int RADIUS = 6;
    public static int WIDTH = RADIUS * 2;
    private static Color FILLCOLOR = Color.WHITE;
}
