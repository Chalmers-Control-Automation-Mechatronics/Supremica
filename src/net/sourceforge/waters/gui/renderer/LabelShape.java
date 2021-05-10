//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public class LabelShape extends AbstractProxyShape
{

  //##########################################################################
  //# Constructors
  LabelShape(final ConditionalProxy cond,
             final double x,
             final double y,
             final Font font)
  {
    this(cond, x, y, new AttributedStringBuilder(cond, font));
  }

  LabelShape(final ForeachProxy foreach,
             final double x,
             final double y,
             final Font font)
  {
    this(foreach, x, y, new AttributedStringBuilder(foreach, font));
  }

  LabelShape(final SimpleExpressionProxy expr,
             final double x,
             final double y,
             final Font font)
  {
    this(expr, x, y, new AttributedStringBuilder(expr, font));
  }

  LabelShape(final SimpleNodeProxy node,
             final double x,
             final double y,
             final Font font)
  {
    this(node.getLabelGeometry(), x, y, new AttributedStringBuilder(node, font));
  }

  LabelShape(final LabelBlockProxy block,
             final double x,
             final double y,
             final String title,
             final Font font)
  {
    this(block, x, y, new AttributedStringBuilder(block, title, font));
  }

  private LabelShape(final Proxy proxy,
                     final double x,
                     final double y,
                     final AttributedStringBuilder builder)
  {
    super(proxy);
    mText = builder.createAttributedString();
    final LineMetrics metrics = builder.getLineMetrics(mText);
    final FontRenderContext context = new FontRenderContext(null, true, true);
    final TextLayout layout = new TextLayout(mText.getIterator(), context);
    final Rectangle2D bounds = layout.getBounds();
    final double width = bounds.getWidth() + 1.0;  // calculated is too short?
    final double height = metrics.getHeight();
    mBounds = new Rectangle2D.Double(x + bounds.getX(), y, width, height);
    mAscent = metrics.getAscent();
    mUnderlineShape =
      builder.createUnderlineShape(proxy, mText, mBounds, metrics);
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  @Override
  public Rectangle2D getShape()
  {
    return mBounds;
  }

  @Override
  public void draw(final Graphics2D graphics,
                   final RenderingInformation status)
  {
    if (status.isFocused()) {
      graphics.setColor(status.getShadowColor());
      graphics.fill(getShape());
    }
    graphics.setColor(status.getColor());
    final int x = (int) Math.round(mBounds.getX());
    final int y = (int) Math.round(mBounds.getY() + mAscent);
    graphics.drawString(mText.getIterator(), x, y);
    if (mUnderlineShape != null) {
      mUnderlineShape.draw(graphics, status);
    }
  }


  //##########################################################################
  //# Inner Class AttributedStringBuilder
  private static class AttributedStringBuilder
  {
    //########################################################################
    //# Constructor
    private AttributedStringBuilder(final ConditionalProxy cond,
                                    final Font font)
    {
      this(font);
      appendEmphasisedText("IF", true, false);
      appendPlainText(" ");
      appendUnderlinedProxy(cond.getGuard());
    }

    private AttributedStringBuilder(final ForeachProxy foreach,
                                    final Font font)
    {
      this(font);
      appendEmphasisedText("FOR", true, false);
      appendPlainText(" " + foreach.getName() + " ");
      appendEmphasisedText("IN", true, false);
      appendPlainText(" ");
      appendUnderlinedProxy(foreach.getRange());
    }

    private AttributedStringBuilder(final SimpleExpressionProxy expr,
                                    final Font font)
    {
      this(font);
      appendUnderlinedProxy(expr);
    }

    private AttributedStringBuilder(final SimpleNodeProxy node,
                                    final Font font)
    {
      this(font);
      appendUnderlinedText(node.getName(), false, false);
    }

    private AttributedStringBuilder(final LabelBlockProxy block,
                                    final String title,
                                    final Font font)
    {
      this(font);
      appendUnderlinedText(title, true, false);
    }

    private AttributedStringBuilder(final Font font)
    {
      mFont = font;
      mStringBuilder = new StringBuilder();
      mAttributeInfoList = new LinkedList<>();
    }

    //########################################################################
    //# String Construction
    private void appendPlainText(final String text)
    {
      appendEmphasisedText(text, false, false);
    }

    private void appendEmphasisedText(final String text,
                                      final boolean bold,
                                      final boolean italic)
    {
      final int start = mStringBuilder.length();
      mStringBuilder.append(text);
      final int end = mStringBuilder.length();
      final AttributeInfo info =
        new AttributeInfo(start, end, bold, italic, false);
      mAttributeInfoList.add(info);
    }

    private void appendUnderlinedProxy(final Proxy proxy)
    {
      final String text = ProxyPrinter.getPrintString(proxy);
      appendUnderlinedText(text, false, false);
    }

    private void appendUnderlinedText(final String text,
                                      final boolean bold,
                                      final boolean italic)
    {
      final int start = mStringBuilder.length();
      mStringBuilder.append(text);
      final int end = mStringBuilder.length();
      final AttributeInfo info =
        new AttributeInfo(start, end, bold, italic, true);
      mAttributeInfoList.add(info);
    }

    private AttributedString createAttributedString()
    {
      final AttributedString string =
        new AttributedString(mStringBuilder.toString());
      for (final AttributeInfo info : mAttributeInfoList) {
        info.apply(string, mFont);
      }
      return string;
    }

    private LineMetrics getLineMetrics(final AttributedString string)
    {
      final AttributedCharacterIterator iter = string.getIterator();
      final FontRenderContext context = new FontRenderContext(null, true, true);
      return mFont.getLineMetrics(iter, 0, iter.getEndIndex(), context);
    }

    private UnderlineShape createUnderlineShape(final Proxy proxy,
                                                final AttributedString string,
                                                final Rectangle2D bounds,
                                                final LineMetrics metrics)
    {
      final FontRenderContext context = new FontRenderContext(null, true, true);
      final TextLayout layout = new TextLayout(string.getIterator(), context);
      final double x = bounds.getX();
      final double y = bounds.getY() +
        metrics.getAscent() + metrics.getUnderlineOffset();
      for (final AttributeInfo info : mAttributeInfoList) {
        final UnderlineShape shape =
          info.createUnderlineShape(proxy, string, layout, x, y);
        if (shape != null) {
          return shape;
        }
      }
      return null;
    }

    //########################################################################
    //# Data Members
    private final Font mFont;
    private final StringBuilder mStringBuilder;
    private final List<AttributeInfo> mAttributeInfoList;
  }


  //##########################################################################
  //# Inner Class AttributeInfo
  private static class AttributeInfo
  {
    //########################################################################
    //# Constructor
    private AttributeInfo(final int start,
                          final int end,
                          final boolean bold,
                          final boolean italic,
                          final boolean underlined)
    {
      mStart = start;
      mEnd = end;
      mBold = bold;
      mItalic = italic;
      mUnderlined = underlined;
    }

    //########################################################################
    //# String Construction
    private void apply(final AttributedString string, Font font)
    {
      if (mBold) {
        font = font.deriveFont(Font.BOLD);
      }
      if (mItalic) {
        font = font.deriveFont(Font.ITALIC);
      }
      final Map<TextAttribute,?> attribs = font.getAttributes();
      string.addAttributes(attribs, mStart, mEnd);
    }

    private UnderlineShape createUnderlineShape(final Proxy proxy,
                                                final AttributedString string,
                                                final TextLayout layout,
                                                final double x,
                                                final double y)
    {
      if (mUnderlined) {
        final Shape shape = layout.getLogicalHighlightShape(mStart, mEnd);
        final Rectangle2D bounds = shape.getBounds2D();
        final double xStart = x + bounds.getX();
        final double width = bounds.getWidth();
        return new UnderlineShape(proxy, xStart, y, width);
      } else {
        return null;
      }
    }

    //########################################################################
    //# Data Members
    private final int mStart;
    private final int mEnd;
    private final boolean mBold;
    private final boolean mItalic;
    private final boolean mUnderlined;
  }


  //##########################################################################
  //# Data Members
  private final AttributedString mText;
  private final Rectangle2D mBounds;
  private final double mAscent;
  private final UnderlineShape mUnderlineShape;


  //##########################################################################
  //# Class Constants
  public static final int DEFAULT_NODE_LABEL_OFFSET_X = 0;
  public static final int DEFAULT_NODE_LABEL_OFFSET_Y = 10;
  public static final Point2D DEFAULT_NODE_LABEL_OFFSET =
    new Point2D.Double(DEFAULT_NODE_LABEL_OFFSET_X,
                       DEFAULT_NODE_LABEL_OFFSET_Y);

}
