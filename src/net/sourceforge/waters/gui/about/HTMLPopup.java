//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.gui.about;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * A dialog window that can display arbitrary HTML contents.
 * The font size is scaled according to the IDE configuration.
 * A scrollbar is included if necessary.
 * This is used by the {@link AboutPanel} to display the license information.
 *
 * @author Robi Malik
 */
public class HTMLPopup
  extends JDialog
{

  //#########################################################################
  //# Constructor
  public HTMLPopup(final String title, final URL url, final Frame owner)
    throws IOException
  {
    super(owner, title);
    setBackground(Color.WHITE);
    final HTMLDocument doc = createContents(url);
    final JEditorPane panel = new JEditorPane();
    panel.setContentType("text/html");
    panel.setOpaque(true);
    panel.setEditable(false);
    panel.setDocument(doc);
    final int scaledWidth =
      Math.round(TEXT_WIDTH * IconAndFontLoader.GLOBAL_SCALE_FACTOR);
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int screenWidth = (int) screenSize.getWidth();
    final int textWidth = Math.min(scaledWidth, screenWidth);
    panel.setSize(textWidth, Integer.MAX_VALUE);
    final int screenHeight = (int) screenSize.getHeight();
    final int textHeight = panel.getPreferredSize().height;
    final int scrollHeight = Math.min(screenHeight * 7 / 8, textHeight);
    final Dimension scrollSize = new Dimension(textWidth, scrollHeight);
    panel.setPreferredSize(scrollSize);
    final JScrollPane scroll = new JScrollPane(panel);
    add(scroll);
    pack();
    setLocationRelativeTo(owner);
    setVisible(true);
  }


  //#########################################################################
  //# Auxiliary Methods
  private HTMLDocument createContents(final URL url)
    throws IOException
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("<HTML><BODY STYLE=\"font-size: ");
    builder.append(IconAndFontLoader.HTML_FONT_SIZE);
    builder.append("px; font-family: serif;\">");
    final InputStream stream = url.openStream();
    try {
      final InputStreamReader reader = new InputStreamReader(stream);
      final char[] buffer = new char[BUFFER_SIZE];
      int numChars = reader.read(buffer);
      while (numChars > 0) {
        for (int i = 0; i < numChars; i++) {
          builder.append(buffer[i]);
        }
        numChars = reader.read(buffer);
      }
    } finally {
      stream.close();
    }
    builder.append("</BODY></HTML>");

    final Reader reader = new StringReader(builder.toString());
    final HTMLEditorKit htmlKit = new HTMLEditorKit();
    final HTMLDocument doc = (HTMLDocument) htmlKit.createDefaultDocument();
    try {
      htmlKit.read(reader, doc, 0);
    } catch (final BadLocationException exception) {
      throw new WatersRuntimeException(exception);
    }
    final MutableAttributeSet attribs = new SimpleAttributeSet();
    StyleConstants.setSpaceAbove(attribs, 0);
    StyleConstants.setSpaceBelow(attribs, 4);
    doc.setParagraphAttributes(0, doc.getLength(), attribs, false);
    return doc;
  }


  //#########################################################################
  //# Class Constants
  private static final int TEXT_WIDTH = 640;
  private static final int BUFFER_SIZE = 1024;

  private static final long serialVersionUID = -5901475634903492023L;

}
