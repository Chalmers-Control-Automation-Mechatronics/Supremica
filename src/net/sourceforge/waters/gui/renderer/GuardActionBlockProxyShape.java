package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.module.GuardActionBlockProxy;


public class GuardActionBlockProxyShape
    extends AbstractProxyShape
{
    public GuardActionBlockProxyShape(final GuardActionBlockProxy block, final RoundRectangle2D bounds)
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

    public void draw(final Graphics2D g, final RenderingInformation status)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (status.isFocused())
        {
            g.setColor(status.getShadowColor());
            g.setStroke(SHADOWSTROKE);
            g.fill(getShape());
        }
        if (status.isSelected()) {
          g.setColor(status.getColor());
          g.setStroke(BASICSTROKE);
          g.draw(getShape());
        }
    }

    private final RoundRectangle2D mBounds;
    GuardActionBlockProxy mBlock;
    public static final int DEFAULTARCW = 8;
    public static final int DEFAULTARCH = 8;
    public static final int DEFAULTOFFSETX = 0;
    public static final int DEFAULTOFFSETY = 10;
}
