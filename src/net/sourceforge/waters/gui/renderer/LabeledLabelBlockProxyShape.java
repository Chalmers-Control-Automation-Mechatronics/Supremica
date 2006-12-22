package net.sourceforge.waters.gui.renderer;

import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.Font;

public class LabeledLabelBlockProxyShape
    extends LabelBlockProxyShape
{
    public LabeledLabelBlockProxyShape(LabelBlockProxy block,
                                       RoundRectangle2D bounds, String name,
                                       Font font)
    {
        super(block, bounds);
        mName = name;
        mFont = font;
    }
    
    public void draw(Graphics2D g, RenderingInformation status)
    {
      super.draw(g, status);
      int x = (int)getShape().getBounds().getMinX();
      int y = (int)getShape().getBounds().getMinY();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      g.setFont(mFont);
      g.setColor(status.getColor());
      g.drawString(mName, x, y);
    }
    
    private String mName;
    private Font mFont;
    
    public static final int DEFAULTARCW = 8;
    public static final int DEFAULTARCH = 8;
    public static final int DEFAULTOFFSETX = 0;
    public static final int DEFAULTOFFSETY = 10;
}
