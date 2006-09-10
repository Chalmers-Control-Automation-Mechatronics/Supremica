package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.module.LabelBlockProxy;

public class LabelBlockProxyShape
    extends AbstractProxyShape
{
    public LabelBlockProxyShape(LabelBlockProxy block, RoundRectangle2D bounds)
    {
        super(block);
        mBounds = bounds;
    }
    
    public LabelBlockProxy getProxy()
    {
        return (LabelBlockProxy)super.getProxy();
    }
    
    public RoundRectangle2D getShape()
    {
        return mBounds;
    }
    
    public void draw(Graphics2D g, RenderingInformation status)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (status.isFocused())
        {
            g.setColor(status.getShadowColor());
            g.setStroke(SHADOWSTROKE);
            g.fill(getShape());
        }
    }
    
    private RoundRectangle2D mBounds;
    
    public static final int DEFAULTARCW = 8;
    public static final int DEFAULTARCH = 8;
    public static final int DEFAULTOFFSETX = 0;
    public static final int DEFAULTOFFSETY = 10;
}
