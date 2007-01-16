//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorSurface
//###########################################################################
//# $Id: EditorSurface.java,v 1.73 2007-01-16 22:03:32 flordal Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.Rectangle;
import javax.swing.JComponent;
import net.sourceforge.waters.gui.renderer.AbstractRendererShape;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.ProxyShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.Renderable;
import net.sourceforge.waters.gui.renderer.Renderer;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import java.util.List;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import java.util.LinkedList;
import java.util.Collections;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import org.supremica.properties.Config;


/**
 * <p>A component which allows for the display of module data.</p>
 *
 * <p>The EditorSurface is a viewer-only component. It can load components
 * from {@link net.sourceforge.waters.subject.module.ModuleSubject} objects and translate them into its internal
 * storage format, which allows for it to be displayed. To provide
 * interactive editing of an EditorSurface, use a {@link
 * ControlledSurface}.</p>
 *
 * @author Gian Perrone
 */
public class EditorSurface
    extends JComponent
    implements Printable, Renderable
{
    /**
     * Increase bounds for label & guardAction panels.
     */
    public static final int TEXTSHADOWMARGIN = 2;
    protected boolean showGrid = true;
    protected EditorWindowInterface root;
    protected int gridSize = 16;
    protected int dragStartX;
    protected int dragStartY;
    protected int dragNowX;
    protected int dragNowY;
    protected boolean dragSelect = false;
    private final GraphProxy mGraph;
    private final ModuleProxy mModule;
    private final ProxyShapeProducer mShapeProducer;
    protected Rectangle drawnAreaBounds = null;
    
    /** Different status values for objects being dragged. */
    public enum DRAGOVERSTATUS
    {
        /** Is not being draggedOver. */
        NOTDRAG,
        /** Is being draggedOver and can drop data. */
        CANDROP,
        /** Is being draggedOver but can't drop data. */
        CANTDROP;
    }
    
    public RenderingInformation getRenderingInformation(Proxy o)
    {
        return new RenderingInformation(false, false,
            EditorColor.getColor(o, DRAGOVERSTATUS.NOTDRAG, false, false),
            EditorColor.getShadowColor(o, DRAGOVERSTATUS.NOTDRAG, false, false),
            getPriority(o));
    }
    
    protected int getPriority(Proxy o)
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
    
    public GraphProxy getGraph()
    {
        return mGraph;
    }
    
    public ModuleProxy getModule()
    {
        return mModule;
    }
    
    protected GraphProxy getDrawnGraph()
    {
        return getGraph();
    }
    
    public int getGridSize()
    {
       // return gridSize;
        return Config.GUI_EDITOR_GRID_SIZE.get();
    }

    public void setGridSize(int g)
    {
        gridSize = g;
    }
    
    public boolean getShowGrid()
    {
        //return showGrid;
        return Config.GUI_EDITOR_SHOW_GRID.get();
    }
    
    public void setShowGrid(boolean s)
    {
        showGrid = s;
    }
    
    protected void paintGrid(Graphics g)
    {
        g.setColor(EditorColor.BACKGROUNDCOLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(EditorColor.GRIDCOLOR);
        
        // Draw grid iff showGrid is true
        if (getShowGrid())
        {
            int x = -(int)getLocation().getX();
            int y = -(int)getLocation().getY();
            
            for (int i = 0; i < getWidth(); i += getGridSize())
            {
                g.drawLine(i, y, i, getHeight());
            }
            
            for (int i = 0; i < getHeight(); i += getGridSize())
            {
                g.drawLine(x, i, getWidth(), i);
            }
        }
    }
    
    /**
     * Called when printing.
     */
    protected void printComponent(Graphics g)
    {
        // Paint using other stroke
        //AbstractRendererShape.setBasicStroke(AbstractRendererShape.THINSTROKE); // Too thin for presentations
        AbstractRendererShape.setBasicStroke(AbstractRendererShape.SINGLESTROKE); // Just right?
        //AbstractRendererShape.setBasicStroke(AbstractRendererShape.DOUBLESTROKE); // Too thick
        paintComponent(g, true);
        AbstractRendererShape.setBasicStroke(AbstractRendererShape.SINGLESTROKE);
    }
    
    /**
     * Called when painting.
     */
    protected void paintComponent(Graphics g)
    {
        paintComponent(g, false);
    }
    
    /**
     * Paints the surface on {@code g}.
     */
    private void paintComponent(Graphics g, boolean printing)
    {
        // Only paint the grid if we're not printing!
        if (!printing)
        {
            paintGrid(g);
        }
                /*
                // Don't do anything if there is nothing to do!
                if ((nodes == null) || (nodes.size() == 0))
                {
                        return;
                }
                 */
        Renderer renderer = new Renderer();
        renderer.renderGraph(getDrawnGraph(), getDrawnObjects(), this,
            getShapeProducer(), (Graphics2D)g);
        
                /*
                // Test: Print outline of drawn area (just to see that it's OK)
                Rectangle rect = getDrawnAreaBounds();
                g.setColor(Color.PINK);
                g.drawRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
                 */
    }
    
    public List<MiscShape> getDrawnObjects()
    {
        return Collections.emptyList();
    }
    
    public static boolean isSimpleComponentSubject(Object o)
    {
        return (o instanceof SimpleComponentSubject);
    }
    
    public void showDragSelect(Graphics g)
    {
        //Graphics2D g2d = (Graphics2D) this.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor(EditorColor.DRAGSELECTCOLOR);
        g2d.fill(getDragRectangle());
        //g2d.fill(new Rectangle(dragStartX, dragStartY, dragNowX - dragStartX, dragNowY - dragStartY));
    }
    
    // When you're dragging an edge, you only want nodes or nodegroups...
    public NodeProxy getNodeOrNodeGroupAtPosition(int ex, int ey)
    {
        for (NodeProxy node: getGraph().getNodes())
        {
            try
            {
                ProxyShape shape = getShapeProducer().getShape(node);
                if (shape.isClicked(ex, ey))
                {
                    return (NodeProxy)shape.getProxy();
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
        return null;
    }
    
    public Proxy getObjectAtPosition(int ex, int ey)
    {
        for (NodeProxy node : getGraph().getNodes())
        {
            try
            {
                ProxyShape shape = getShapeProducer().getShape(node);
                if (shape != null)
                {
                    if (shape.isClicked(ex, ey))
                    {
                        return shape.getProxy();
                    }
                    if (node instanceof SimpleNodeProxy) {
                      shape = getShapeProducer().getShape(((SimpleNodeProxy)node).getLabelGeometry());
                      if (shape != null)
                      {
                          if ((shape != null) && (shape.isClicked(ex, ey)))
                          {
                              return shape.getProxy();
                          }
                      }
                    }
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
        for (EdgeProxy edge : getGraph().getEdges())
        {
            try
            {
                ProxyShape shape = getShapeProducer().getShape(edge);
                if (shape != null)
                {
                    if (shape.isClicked(ex, ey))
                    {
                        return shape.getProxy();
                    }
                    shape = getShapeProducer().getShape(edge.getLabelBlock());
                    if (shape != null)
                    {
                        if (shape.isClicked(ex, ey))
                        {
                            return shape.getProxy();
                        }
                    }
                    shape = getShapeProducer().getShape(edge.getGuardActionBlock());
                    if (shape != null)
                    {
                        if (shape.isClicked(ex, ey))
                        {
                            return shape.getProxy();
                        }
                    }
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
        try
        {
          ProxyShape shape = getShapeProducer().getShape(mGraph);
          if (shape != null)
          {
            if (shape.isClicked(ex, ey))
            {
              return mGraph.getBlockedEvents();
            }
          }
        }
        catch (VisitorException vis)
        {
            vis.printStackTrace();
        }
        return null;
    }
    
    /**
     * Returns a list of all children of an object.
     */
    public java.util.List<Proxy> getChildren(Proxy p)
    {
        LinkedList<Proxy> children = new LinkedList<Proxy>();
        if (p instanceof SimpleNodeProxy)
        {
            children.add(((SimpleNodeProxy)p).getLabelGeometry());
        }
        if (p instanceof EdgeProxy)
        {
            children.add(((EdgeProxy)p).getLabelBlock());
        }
        
        return children;
    }
    
    public Rectangle getDragRectangle()
    {
        int x;
        int y;
        int w;
        int h;
        // this can probably be done with set from diagonal
        if (dragStartX > dragNowX)
        {
            x = dragNowX;
            w = dragStartX - dragNowX;
        }
        else
        {
            x = dragStartX;
            w = dragNowX - dragStartX;
        }
        
        if (dragStartY > dragNowY)
        {
            y = dragNowY;
            h = dragStartY - dragNowY;
        }
        else
        {
            y = dragStartY;
            h = dragNowY - dragStartY;
        }
        
        return new Rectangle(x,y,w,h);
    }
    
    /**
     * This should adjust the size of the controlled surface to the minimum required
     */
    public void minimizeSize()
    {
        Rectangle area = getDrawnAreaBounds();
        
        int width = (int) area.getWidth();
        int height = (int) area.getHeight();
        
                /* // We want the bounds to be tight, right? Or why was this?
                if (width < 500)
                {
                        width = 500;
                }
                 
                if (height < 500)
                {
                        height = 500;
                }
                 */
        
        //setPreferredSize(new Dimension(width + getGridSize() * 10, height + getGridSize() * 10));
    }
    
    /**
     * Calculates and returns a rectangle that includes everything drawn on the surface.
     */
    public Rectangle getDrawnAreaBounds()
    {
        // If we don't need to recalculate, don't!
        if (drawnAreaBounds != null)
        {
            return drawnAreaBounds;
        }
        
        // How much spacing should there be
        int SPACING = 0;
        /*
        // The extreme values
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
         
        // Local measures
        Rectangle2D bounds;
        double mod = 3;
         
        for (ProxyShape shape : getShapeProducer().getCachedShapes())
        {
            bounds = shape.getShape().getBounds2D();
            if (bounds.getMaxX() + mod > maxX)
            {
                maxX = bounds.getMaxX() + mod;
            }
            if (bounds.getMaxY() + mod > maxY)
            {
                maxY = bounds.getMaxY() + mod;
            }
            if (bounds.getMinX() - mod < minX)
            {
                minX = bounds.getMinX() - mod;
            }
            if (bounds.getMinY() - mod < minY)
            {
                minY = bounds.getMinY() - mod;
            }
        }*/
        
        // Guard Action Blocks
                /*
                for (int i = 0; i < mGuardActionBlocks.size(); i++)
                {
                        EditorGuardActionBlock gABlock = ((EditorGuardActionBlock) mGuardActionBlocks.get(i));
                        x = gABlock.getX(); // This is not the center!
                        mod = gABlock.getWidth() + SPACING;
                        if (x + mod > maxX)
                        {
                                maxX = x + mod;
                        }
                 
                        mod = SPACING;
                        if (x - mod < minX)
                        {
                                minX = x - mod;
                        }
                 
                        y = gABlock.getY(); // This is not the center!
                        mod = gABlock.getHeight() + SPACING;
                        if (y + mod > maxY)
                        {
                                maxY = y + mod;
                        }
                 
                        mod = SPACING;
                        if (y - mod < minY)
                        {
                                minY = y - mod;
                        }
                }*/
        
        //Avoid stupid values
        /*if ((maxX < minX) || (maxY < minY))
        {
            drawnAreaBounds = new Rectangle(0,0,0,0);
        }
        else
        {
            drawnAreaBounds = new Rectangle(0, 0, (int) (maxX),(int) (maxY));
        }*/
        drawnAreaBounds = getShapeProducer().getMinimumBoundingRectangle();
        return drawnAreaBounds;
    }
    
    public ProxyShapeProducer getShapeProducer()
    {
        return mShapeProducer;
    }
    
    // TODO: This should take a ModuleSubject as a parameter
    public EditorSurface(GraphProxy graph, ModuleProxy module)
    {
        this(graph, module, new ProxyShapeProducer(module));
    }
    
    public EditorSurface(GraphProxy graph, ModuleProxy module,
        ProxyShapeProducer shapeProducer)
    {
        mGraph = graph;
        mModule = module;
        mShapeProducer = shapeProducer;
    }
    
    /**
     * Implementation of the Printable interface.
     */
    public int print(Graphics g, PageFormat pageFormat, int page)
    {
        final double INCH = 72;
        
        Graphics2D g2d;
        
        // Validate the page number, we only print the first page
        if (page == 0)
        {
            g2d = (Graphics2D) g;
            // Translate the origin so that (0,0) is in the upper left corner of the printable area
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
                        /*
                        // Print page margin
                        {
                                Line2D.Double line = new Line2D.Double ();
                                int i;
                                g2d.setColor(Color.ORANGE);
                         
                                // Draw the vertical lines
                                line.setLine (0, 0, 0, pageFormat.getHeight());
                                g2d.draw (line);
                                line.setLine (pageFormat.getImageableWidth(), 0, pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
                                g2d.draw (line);
                         
                                // Draw the horizontal lines
                                line.setLine (0, 0, pageFormat.getImageableWidth(), 0);
                                g2d.draw (line);
                                line.setLine (0, pageFormat.getImageableHeight(), pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
                                g2d.draw (line);
                        }
                         */
            
            // Get the bounds of the actual drawing
            Rectangle area = getDrawnAreaBounds();
            
            // This is the place to do rescaling if the figure won't fit on the page!
            double scaleX = pageFormat.getImageableWidth() / (area.getWidth());
            double scaleY = pageFormat.getImageableHeight() / (area.getHeight());
            double scale = scaleX < scaleY ? scaleX : scaleY;
            if (scale < 1)
            {
                System.out.println("Rescaling figure to fit page. Scale: " + scale);
                g2d.scale(scale, scale);
            }
            
            // Put drawing at (0, 0)
            g2d.translate(-area.getX(), -area.getY());
            
            // Put the current figure into the Graphics object!
            print(g);
            
            // OK to print!
            return (PAGE_EXISTS);
        }
        else
        {
            return (NO_SUCH_PAGE);
        }
    }
    
    public void repaint(boolean boundsMaybeChanged)
    {
        if (boundsMaybeChanged)
        {
            //System.out.println("bounds:" + getBounds());
        }
        
        super.repaint();
    }
    
    /*public void repaint()
    {
        repaint(true);
    }*/
    
    public EditorWindowInterface getEditorInterface()
    {
        return root;
    }
}
