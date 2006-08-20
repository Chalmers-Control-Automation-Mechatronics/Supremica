package net.sourceforge.waters.gui.renderer;

import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;

public abstract class AbstractRendererShape
    implements RendererShape
{
    public void draw(Graphics2D g, RenderingInformation status)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw shadow if focused
        if (status.isFocused())
        {
            g.setColor(status.getShadowColor());
            g.setStroke(SHADOWSTROKE);
            g.draw(getShape());
        }
        
        // Draw shape
        g.setColor(status.getColor());
        g.setStroke(BASICSTROKE);
        g.draw(getShape());
    }
    
    public boolean isClicked(int x, int y)
    {
        return getShape().contains(x, y);
    }
    
    public static void setBasicStroke(Stroke stroke)
    {
        BASICSTROKE = stroke;
    }
    
    /** Single line width, used as default when painting on screen. */
    public static final Stroke SINGLESTROKE = new BasicStroke();
    /** Double line width, used for nodegroup border. */
    public static final Stroke DOUBLESTROKE = new BasicStroke(2);
    /** Thick line used for drawing shadows. */
    public static final Stroke SHADOWSTROKE = new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    /** Used as the basic stroke when printing - "hairline" width. */
    public static final Stroke THINSTROKE = new BasicStroke(0.25f);
    /** The default pen size. Is not {@code final} since it changes when printing. */
    public static Stroke BASICSTROKE = SINGLESTROKE;
}
