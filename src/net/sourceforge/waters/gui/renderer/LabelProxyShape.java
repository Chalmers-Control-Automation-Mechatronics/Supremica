package net.sourceforge.waters.gui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

public class LabelProxyShape
	extends AbstractProxyShape
{
	public LabelProxyShape(SimpleNodeProxy node, Font font)
	{
		super(node.getLabelGeometry());
		mFont = font;
		int x = (int)(node.getPointGeometry().getPoint().getX() +
					getProxy().getOffset().getX());
		int y = (int)(node.getPointGeometry().getPoint().getY() +
					getProxy().getOffset().getY());
		mPoint = new Point(x + 2, y + font.getSize());
		mName = node.getName();
		TextLayout text = new TextLayout(mName, mFont,
									 new FontRenderContext(null, true, true));
		Rectangle2D rect = text.getBounds();
		rect.setRect(x, y, rect.getWidth() + 4, rect.getHeight() + 6);
		mBounds = new RoundRectangle2D.Double(rect.getX(), rect.getY(),
					rect.getWidth(), rect.getHeight(), ARCRADIUS, ARCRADIUS);
	}
	
	public RoundRectangle2D getShape()
	{
		return mBounds;
	}
	
	public LabelGeometryProxy getProxy()
	{
		return (LabelGeometryProxy)super.getProxy();
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
	
	private final Font mFont;
	private final String mName;
	private final RoundRectangle2D mBounds;
	private final Point mPoint;
	
	private static double ARCRADIUS = 5;
  public static final int DEFAULTOFFSETX = 0;
  public static final int DEFAULTOFFSETY = 10;
}
