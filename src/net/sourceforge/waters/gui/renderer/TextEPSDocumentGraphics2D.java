//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   TextEPSDocumentGraphics2D
//###########################################################################
//# $Id$
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
