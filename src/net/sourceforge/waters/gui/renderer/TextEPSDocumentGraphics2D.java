//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.AttributedCharacterIterator;

import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;


/**
 * An <CODE>EPSDocumentGraphics2D</CODE> which renders text using PostScript
 * text commands instead of primitive shapes. Limited formatting is supported.
 *
 * @author Tom Levy
 */

public class TextEPSDocumentGraphics2D extends EPSDocumentGraphics2D
{

  //##########################################################################
  //# Constructor
  public TextEPSDocumentGraphics2D()
  {
    super(false);
  }


  //##########################################################################
  //# Overrides for org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D
  @Override
  public void drawString(final String text, final float x, final float y)
  {
    try {
      establishColor(getColor());
      useFont(getFont());
      gen.saveGraphicsState();
      gen.writeln(gen.mapCommand("newpath"));
      moveto(x, y);
      gen.writeln("1 -1 " + gen.mapCommand("scale"));
      // Hm ... gen replaces special characters with ??? ...
      gen.writeln("(" + text + ") " + gen.mapCommand("show"));
      gen.restoreGraphicsState();
    } catch (final IOException e) {
      handleIOException(e);
    }
  }

  @Override
  public void drawString(final AttributedCharacterIterator iter,
                         final float x, final float y)
  {
    try {
      gen.saveGraphicsState();
      gen.writeln(gen.mapCommand("newpath"));
      moveto(x, y);
      gen.writeln("1 -1 " + gen.mapCommand("scale"));
      int i = iter.getBeginIndex();
      while (i < iter.getEndIndex()) {
        iter.setIndex(i);
        Color color = (Color) iter.getAttribute(TextAttribute.FOREGROUND);
        if (color == null) {
          color = getColor();
        }
        establishColor(color);
        useFont(Font.getFont(iter.getAttributes()));
        gen.write("(");
        while (i < iter.getRunLimit()) {
          final char c = iter.setIndex(i);
          gen.write(Character.toString(c));
          i++;
        }
        gen.writeln(") " + gen.mapCommand("show"));
      }
      gen.restoreGraphicsState();
    } catch (final IOException e) {
      handleIOException(e);
    }
  }


  //##########################################################################
  //# Auxiliary Methods
  private void moveto(final float x, final float y)
    throws IOException
  {
    final Point2D point = new Point2D.Double(x, y);
    getTransform().transform(point, point);
    final String xStr = gen.formatDouble(point.getX());
    final String yStr = gen.formatDouble(point.getY());
    gen.writeln(xStr + " " + yStr + " " + gen.mapCommand("moveto"));
  }

  private void useFont(final Font font)
    throws IOException
  {
    final StringBuilder name = new StringBuilder("/Helvetica");
    if (font.isBold() || font.isItalic()) {
      name.append("-");
    }
    if (font.isBold()) {
      name.append("Bold");
    }
    if (font.isItalic()) {
      name.append("Oblique");
    }
    gen.useFont(name.toString(), font.getSize());
  }

}
