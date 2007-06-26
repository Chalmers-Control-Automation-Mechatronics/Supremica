//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorSurface
//###########################################################################
//# $Id: EditorSurface.java,v 1.81 2007-06-26 11:28:14 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;

import net.sourceforge.waters.gui.renderer.AbstractRendererShape;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.Renderable;
import net.sourceforge.waters.gui.renderer.Renderer;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;

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
    
    //##########################################################################
    //# Constructors
    public EditorSurface(GraphProxy graph, ModuleProxy module)
    {
        this(graph, module, new ProxyShapeProducer(graph, module));
    }
    
    public EditorSurface(GraphProxy graph, ModuleProxy module,
        ProxyShapeProducer shapeProducer)
    {
        mGraph = graph;
        mModule = module;
        mShapeProducer = shapeProducer;
    }
    
    
    //##########################################################################
    //#
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
    
    protected void paintGrid(Graphics g)
    {
        g.setColor(EditorColor.BACKGROUNDCOLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(EditorColor.GRIDCOLOR);
        
        // Draw grid iff showGrid is true
        if (Config.GUI_EDITOR_SHOW_GRID.get())
        {
            int x = -(int)getLocation().getX();
            int y = -(int)getLocation().getY();
            
            for (int i = 0; i < getWidth(); i += Config.GUI_EDITOR_GRID_SIZE.get())
            {
                g.drawLine(i, y, i, getHeight());
            }
            
            for (int i = 0; i < getHeight(); i += Config.GUI_EDITOR_GRID_SIZE.get())
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
        //AbstractRendererShape.setBasicStroke(AbstractRendererShape.OLDTHINSTROKE); // Too thin for presentations
        //AbstractRendererShape.setBasicStroke(AbstractRendererShape.SINGLESTROKE); // Just right?
        //AbstractRendererShape.setBasicStroke(AbstractRendererShape.DOUBLESTROKE); // Too thick

        // Clear selection?
        
        // Paint the component on the supplied Graphics instance
        paintComponent(g, true);

        // Reset selection?
        
        // Reset stroke
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
    }
    
    public List<MiscShape> getDrawnObjects()
    {
        return Collections.emptyList();
    }
    
    /**
     * Calculates a dimension indicating the size of all objects currently
     * displayed by the shape producer.
     */
    Dimension calculatePreferredSize()
    {
        final Rectangle area = getShapeProducer().getMinimumBoundingRectangle();
        return new Dimension(area.width, area.height);
    }
    
    public ProxyShapeProducer getShapeProducer()
    {
        return mShapeProducer;
    }
    
    /**
     * Implementation of the Printable interface.
     */
    public int print(Graphics g, PageFormat pageFormat, int page)
    {
        //final double INCH = 72;
        
        Graphics2D g2d;
        
        // Validate the page number, we only print the first page
        if (page == 0)
        {
            g2d = (Graphics2D) g;
            
            // Translate the origin so that (0,0) is in the upper left corner of the printable area
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            /*
            // Print page margin (for debugging purposes)
            {
                Line2D.Double line = new Line2D.Double();
                int i;
                g2d.setColor(Color.ORANGE);
                
                // Draw the vertical lines
                line.setLine(0, 0, 0, pageFormat.getHeight());
                g2d.draw(line);
                line.setLine(pageFormat.getImageableWidth(), 0, pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
                g2d.draw(line);
                
                // Draw the horizontal lines
                line.setLine(0, 0, pageFormat.getImageableWidth(), 0);
                g2d.draw(line);
                line.setLine(0, pageFormat.getImageableHeight(), pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
                g2d.draw(line);
            }
            */
            
            // Get the bounds of the actual drawing
            getShapeProducer().createAllShapes();
            Rectangle area = getShapeProducer().getMinimumBoundingRectangle();
            
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
    
    
    //##########################################################################
    //# Data Members
    private final GraphProxy mGraph;
    private final ModuleProxy mModule;
    private final ProxyShapeProducer mShapeProducer;
    
    public static final int TEXTSHADOWMARGIN = 2;
    
    /** Different status values for objects being dragged. */
    public enum DRAGOVERSTATUS
    {
        /** Is not being draggedOver. */
        NOTDRAG,
        /** Is being draggedOver and can drop held object. */
        CANDROP,
        /** Is being draggedOver but can't drop held object. */
        CANTDROP;
    }
    
}
