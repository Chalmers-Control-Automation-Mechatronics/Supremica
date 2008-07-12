package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;

public class GuardActionBlockProxyShape
    extends AbstractProxyShape
{
    public GuardActionBlockProxyShape(GuardActionBlockProxy block, RoundRectangle2D bounds)
    {
        super(block);
        mBlock = block;
        mBounds = bounds;
    }
    
    public GuardActionBlockProxy getProxy()
    {
        return (GuardActionBlockProxy)super.getProxy();
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
	/*
        g.setColor(EditorColor.GUARDCOLOR);
        g.setStroke(BASICSTROKE);
        int offset = g.getFontMetrics().getHeight();
        g.drawString(mBlock.getGuards().toString(),
                     (int) mBounds.getX(), (int) mBounds.getY() + offset);
	*/
    }
    
    private RoundRectangle2D mBounds;
    GuardActionBlockProxy mBlock;
    public static final int DEFAULTARCW = 8;
    public static final int DEFAULTARCH = 8;
    public static final int DEFAULTOFFSETX = 0;
    public static final int DEFAULTOFFSETY = 10;
}
