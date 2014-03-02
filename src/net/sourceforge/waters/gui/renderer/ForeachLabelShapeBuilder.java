//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   ForeachLabelShapeBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ForeachProxy;

public class ForeachLabelShapeBuilder
{

  //##########################################################################
  //# Constructor
  public ForeachLabelShapeBuilder(final Font defaultFont)
  {
    mEntries = new ArrayList<Entry>();
    mFont = defaultFont;
  }


  //##########################################################################
  //# Access
  public void add(final String name)
  {
    add(null, mFont.getAttributes(), name);
  }

  public void add(final Font font, final String name)
  {
    add(null, font.getAttributes(), name);
  }

  public void add(final Proxy proxy)
  {
    add(proxy, mFont.getAttributes(), proxy.toString());
  }

  public void add(final Proxy proxy, final Map<TextAttribute,?> attributes,
                  final String name)
  {
    mEntries.add(new Entry(proxy, attributes, name));
  }


  //##########################################################################
  //# Invocation
  public ForeachLabelShape create(final ForeachProxy foreach, final int x,
                                  final int y, final Map<Proxy,ProxyShape> map)
  {
    final StringBuilder sb = new StringBuilder();
    for (final Entry e : mEntries) {
      sb.append(e.mName);
    }
    mText = new AttributedString(sb.toString());
    int start = 0;
    int end;
    for (final Entry e : mEntries) {
      end = start + e.mName.length();
      mText.addAttributes(e.mAttributes, start, end);
      if (e.mProxy != null) {
        final UnderlineShape underline =
          createUnderline(e.mProxy, x + 2, y + mFont.getSize(), start, end);
        map.put(e.mProxy, underline);
      }
      start = end;
    }
    return new ForeachLabelShape(foreach, x, y, mFont, mText);
  }


  //##########################################################################
  //# Auxiliary Methods
  private UnderlineShape createUnderline(final Proxy proxy, final double x,
      final double y, final int start, final int end)
  {
    final double maxX = calculateBounds(0, end).getMaxX();
    final double width = calculateBounds(start, end).getWidth();
    final double offset = maxX - width;
    return new UnderlineShape(proxy, x + offset, y, width, mFont);
  }

  private Rectangle2D calculateBounds(final int start, final int end)
  {
    final FontRenderContext context = new FontRenderContext(null, true, true);
    final AttributedCharacterIterator it =
      mText.getIterator(null, start, end);
    final TextLayout layout = new TextLayout(it, context);
    final Rectangle2D rect = layout.getBounds();
    return rect;
  }


  //##########################################################################
  //# Inner Class Entry
  static class Entry
  {
    Entry(final Proxy proxy, final Map<TextAttribute,?> attributes,
          final String name)
    {
      mProxy = proxy;
      mAttributes = attributes;
      mName = name;
    }

    private final Proxy mProxy;
    private final Map<TextAttribute, ?> mAttributes;
    private final String mName;
  }


  //##########################################################################
  //# Data Members
  private final List<Entry> mEntries;
  private final Font mFont;

  private AttributedString mText;

}
