//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
