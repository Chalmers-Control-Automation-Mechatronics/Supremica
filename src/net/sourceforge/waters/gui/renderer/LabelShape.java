package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.model.base.Proxy;

public class LabelShape
    extends AbstractProxyShape
{
	public LabelShape(final Proxy p, final int x, final int y, final Font font, String auxilary)
    {
		this(p, x, y, font);
		mAuxilary = auxilary;
    }
    public LabelShape(final Proxy p, final int x, final int y, final Font font)
    {
        super(p);
        mAuxilary = "";
        mFont = font;
        mPoint = new Point(x + 2, y + (font.getSize()));
        mName = getProxy().toString();
        TextLayout text = new TextLayout(mName, mFont,
            new FontRenderContext(null, true, true));
        Rectangle2D rect = text.getBounds();
        rect.setRect(x, y, rect.getWidth() + 4, rect.getHeight() + 4);
        mBounds = new RoundRectangle2D.Double(rect.getX(), rect.getY(),
            rect.getWidth(), rect.getHeight(), ARCRADIUS, ARCRADIUS);
    }

	public RoundRectangle2D getShape()
    {
        return mBounds;
    }
    
    public void draw(Graphics2D g, RenderingInformation status)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setFont(mFont);
        g.setColor(status.getColor());
        g.drawString(mName, (int)mPoint.getX(), (int)mPoint.getY());
        if (status.isFocused())
        {
            g.setColor(status.getShadowColor());
            g.fill(getShape());
        }
    }
	private String mAuxilary;
    private final Point mPoint;
    private final RoundRectangle2D mBounds;
    private final Font mFont;
    private final String mName;
    
    private static double ARCRADIUS = 5;
}
