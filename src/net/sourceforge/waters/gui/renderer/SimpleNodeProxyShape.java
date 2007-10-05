package net.sourceforge.waters.gui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
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
import org.supremica.properties.Config;

public class SimpleNodeProxyShape
    extends AbstractProxyShape
{
    private boolean isForbidden = false;
    
    public void draw(Graphics2D g, RenderingInformation status)
    {
        // Draw the filling (depends on marking)
        updateColors();
        // This rectangle is not the same as the one used to create the nodeCircleShape! It gives rounding errors!
        //Rectangle2D rect = nodeCircleShape.getBounds();
        // This one is correct!
        Rectangle2D rect = new Rectangle2D.Double(nodeCircleShape.getX(), nodeCircleShape.getY(),
            DIAMETER, DIAMETER);

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
            // Draw marking
            if (layoutMode.equals(Config.LAYOUT_MODE_LEGALVALUES.ChalmersIDES))
            {
                // CHALMERS IDES MODE---SINGLE TYPE OF MARKING, DOUBLE CIRCLES
                g.setColor(EditorColor.DEFAULTCOLOR);
                g.setStroke(SINGLESTROKE);
                arc = new Arc2D.Double(rect.getX()+2, rect.getY()+2, rect.getWidth()-4, rect.getHeight()-4, 0, 360, Arc2D.OPEN);
                //arc = new Arc2D.Double();
                //arc.setArcByCenter(rect.getX()+rect.getWidth(), rect.getY()+rect.getHeight(), RADIUS-2, 0, 360, Arc2D.OPEN);
                g.draw(arc);
                //g.drawOval((int) rect.getX()+2, (int) rect.getY()+2, (int) (rect.getWidth()-4), (int) (rect.getHeight()-4));
            }
            else
            {
                // DEFAULT MODE
                for (Color c : mColors)
                {
                    arc = new Arc2D.Double(rect, i, degrees, Arc2D.PIE);
                    g.setColor(c);
                    g.fill(arc);
                    i += degrees;
                }
            }
        }
        
        // Draw handles (initial state arrow)
	for (final Handle handle : mHandles) {
	  g.setColor(status.getColor());
	  handle.draw(g, status);
        }

        // The above handle drawing should not be necessary (it's drawn below) but 
        // the initial arrow refuses to be drawn filled in the editor (not in printed output!?)?
        
        // Draw the basic shape (the outline + handles (initial state arrow))
        super.draw(g, status);
        
        // Cross out if forbidden
        if (isForbidden)
        {
            g.setColor(EditorColor.ERRORCOLOR);
            g.setStroke(DOUBLESTROKE);
            g.drawLine((int) rect.getMaxX(), (int) rect.getMaxY(), (int) rect.getMinX(), (int) rect.getMinY());
            g.drawLine((int) rect.getMaxX(), (int) rect.getMinY(), (int) rect.getMinX(), (int) rect.getMaxY());
        }
    }
    
    /**
     * I think this method updates the set of colors used (if this is a marked node).
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
            DIAMETER, DIAMETER);
        
        nodeCircleShape = new Arc2D.Double(rect, 0, 360, Arc2D.OPEN);
        //nodeCircleShape = new Arc2D.Double();
        //nodeCircleShape.setArcByCenter(rect.getX()+rect.getWidth(), rect.getY()+rect.getHeight(), RADIUS, 0, 360, Arc2D.OPEN);
        mShape = new GeneralPath(nodeCircleShape);

        // Create handles
        if (proxy.isInitial()) {
	  final Handle handle = new InitialStateHandle(proxy);
	  mHandles = Collections.singletonList(handle);
        } else {
	  mHandles = Collections.emptyList();
	}
        
        // Append handles to shape
        for (Handle handle: mHandles)
        { 
            mShape.append(handle.getShape(), false);
        }
    }
    
    //public Arc2D getShape()
    public GeneralPath getShape()
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

    //private final Arc2D mShape;
    private final Arc2D nodeCircleShape;
    private final GeneralPath mShape; // To incorporate the initial state arrow    
    
    /** Node radius. */
    public static final int RADIUS;// 6
    static
    {
        if (Config.GUI_EDITOR_LAYOUT_MODE.get().equals(Config.LAYOUT_MODE_LEGALVALUES.ChalmersIDES))
            RADIUS = 10;
        else
            RADIUS = 6;
    };
    public static final int DIAMETER = RADIUS * 2;
    private static final Color FILLCOLOR = Color.WHITE;
    
    private static final Object layoutMode = Config.GUI_EDITOR_LAYOUT_MODE.get();
}
