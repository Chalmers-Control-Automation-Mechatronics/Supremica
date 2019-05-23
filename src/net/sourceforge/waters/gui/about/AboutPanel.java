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

package net.sourceforge.waters.gui.about;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.WatersRuntimeException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.properties.Config;


/**
 * A panel to display version information.
 * This class displays the Waters/Supremica version and configuration
 * information in a HTML formatted multi-line text panel. It is used
 * in the {@link AboutPopup} and in the {@link WelcomeScreen}.
 *
 * @author Robi Malik
 */
public class AboutPanel
  extends JEditorPane
  implements HyperlinkListener
{

  //#########################################################################
  //# Static Methods
  public static void performStudentVersionCheck()
  {
    if (Config.GENERAL_STUDENT_VERSION.isTrue()) {
      try {
        System.loadLibrary("waters");
      } catch (final UnsatisfiedLinkError error) {
        final Version version = Version.getInstance();
        final StringBuilder builder = new StringBuilder();
        builder.append("<HTML><BODY><P STYLE=\"font-size: ");
        builder.append(IconAndFontLoader.HTML_FONT_SIZE);
        builder.append("px; width: ");
        builder.append((int) Math.ceil(320 * IconAndFontLoader.GLOBAL_SCALE_FACTOR));
        builder.append("px;\">");
        version.appendUnsupportedOSExplanation(builder);
        builder.append("</P></BODY></HTML>");
        JOptionPane.showMessageDialog(null, builder.toString(),
                                      "Failed to load library",
                                      JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
    }
  }


  //#########################################################################
  //# Constructor
  public AboutPanel()
  {
    final HTMLDocument doc = createContents();
    setContentType("text/html");
    setDocument(doc);
    setBackground(Color.WHITE);
    setOpaque(true);
    setEditable(false);
    addHyperlinkListener(this);
  }


  //#########################################################################
  //# Interface javax.swing.event.HyperlinkListener
  @Override
  public void hyperlinkUpdate(final HyperlinkEvent event)
  {
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      final Element element = event.getSourceElement();
      final Document doc = element.getDocument();
      final int start = element.getStartOffset();
      final int end = element.getEndOffset();
      String title = "License";
      try {
        title = doc.getText(start, end - start);
      } catch (final BadLocationException exception) {
        // should not happen but never mind ...
      }
      final String name = event.getDescription();
      final URL url = Version.class.getResource(name);
      final Frame owner = getOwningFrame();
      try {
        @SuppressWarnings("unused")
        final HTMLPopup popup = new HTMLPopup(title, url, owner);
      } catch (final IOException exception) {
        final Logger logger = LogManager.getLogger();
        logger.error("Could not find licence file " + url);
      }
    }
  }


  //#########################################################################
  //# Update
  void update()
  {
    final HTMLDocument doc = createContents();
    setDocument(doc);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Frame getOwningFrame()
  {
    Window ancestor = SwingUtilities.getWindowAncestor(this);
    while (!(ancestor instanceof Frame)) {
      ancestor = ancestor.getOwner();
    }
    return (Frame) ancestor;
  }

  private HTMLDocument createContents()
  {
    final Version version = Version.getInstance();
    final StringBuilder builder = new StringBuilder();
    builder.append("<HTML><BODY STYLE=\"font-size: ");
    builder.append(IconAndFontLoader.HTML_FONT_SIZE);
    builder.append("px; font-family: serif;\">");
    builder.append("<H1 STYLE=\"text-align: center; color: #00008d; font-style: italic;\">");
    builder.append(version.getTitle());
    builder.append("</H1>");
    builder.append("<P STYLE=\"text-align: center;\"><STRONG>");
    builder.append("<A HREF=\"release_notes.html\">What's new in this version</A>");
    builder.append("</STRONG></P>");
    builder.append("<P>Waters/Supremica is a joint project between the Department ");
    builder.append("of Signals and Systems, Chalmers University of Technology, ");
    builder.append("Sweden and the Department of Computer Science, University ");
    builder.append("of Waikato, New Zealand.</P>");
    builder.append("<P>Authors: Knut &Aring;kesson, Goran \u010Cengi\u0107, ");
    builder.append("Martin Fabian, Hugo Flordal, Carly Hona, Tom Levy, Robi Malik, ");
    builder.append("Markus Sk&ouml;ldstam, Arash Vahidi, and many others.</P>");
    builder.append("<P>Waters is released under the ");
    builder.append("<A HREF=\"gpl2.html\">GNU General Public License, version&nbsp;2</A>.<BR>");
    builder.append("Supremica is released using the ");
    builder.append("<A HREF=\"suplic.html\">Supremica Software License Agreement</A>.</P>");
    builder.append("<P>This is ");
    builder.append(version.getTitle());
    builder.append("<BR>");
    builder.append("Built ");
    builder.append(version.getPrintableBuildTime());
    builder.append("<BR>");
    final String osType = version.getOSType();
    if (osType == null) {
      builder.append("Dynamic libraries <span style=\"color: red;\">unavailable</span><BR>");
    } else {
      builder.append("Dynamic libraries compiled for ");
      builder.append(osType);
      if (version.checkOSType()) {
        try {
          System.loadLibrary("waters");
        } catch (final UnsatisfiedLinkError error) {
          builder.append(" - <span style=\"color: red;\">failed to load</span>");
        }
      } else {
        builder.append(" - <span style=\"color: red;\">incompatible</span>");
      }
      builder.append("<BR>");
    }
    builder.append("Running in Java ");
    builder.append(version.getJavaVersionText());
    builder.append("<BR>");
    if (Config.DOT_USE.get() && !Config.DOT_EXECUTE_COMMAND.get().equals("")) {
      final String dotVersion = getDotVersion();
      if (dotVersion == null) {
        builder.append("GraphViz <span style=\"color: red;\">not found</span><BR>");
      } else {
        builder.append("Using GraphViz/dot version ");
        builder.append(dotVersion);
        builder.append("<BR>");
      }
    }
    builder.append("Maximum available memory: ");
    builder.append(Runtime.getRuntime().maxMemory() / 0x100000L);
    builder.append(" MiB</P>");
    builder.append("</BODY></HTML>");

    final Reader reader = new StringReader(builder.toString());
    final HTMLEditorKit htmlKit = new HTMLEditorKit();
    final HTMLDocument doc = (HTMLDocument) htmlKit.createDefaultDocument();
    try {
      htmlKit.read(reader, doc, 0);
    } catch (IOException | BadLocationException exception) {
      throw new WatersRuntimeException(exception);
    }
    final MutableAttributeSet attribs = new SimpleAttributeSet();
    StyleConstants.setSpaceAbove(attribs, 0);
    StyleConstants.setSpaceBelow(attribs, 4);
    doc.setParagraphAttributes(0, doc.getLength(), attribs, false);
    return doc;
  }

  private String getDotVersion()
  {
    final DotVersionTask task = new DotVersionTask();
    final Thread thread = new Thread(task);
    thread.start();
    try {
      thread.join(500);
    } catch (final InterruptedException exception) {
      // Interrupted? Timeout? Never mind ...
    }
    task.kill();
    return task.getVersionInfo();
  }


  //#########################################################################
  //# Inner Class DotVersionTask
  /**
   * Background task to run the command 'dot -version' and retrieve a
   * version number. This is run in a separate thread to provide better
   * robustness in cases where the external command fails to terminate.
   */
  private static class DotVersionTask implements Runnable
  {

    //#######################################################################
    //# Interface java.lang.Runnable
    @Override
    public void run()
    {
      try {
        final String command = Config.DOT_EXECUTE_COMMAND.get();
        final ProcessBuilder builder = new ProcessBuilder(command, "-version");
        builder.redirectErrorStream(true);
        mProcess = builder.start();
        // Some versions of dot read stdin even with the -version option.
        // By closing the stream, we stop dot from waiting for input:
        mProcess.getOutputStream().close();
        final InputStream stream = mProcess.getInputStream();
        final BufferedReader reader =
          new BufferedReader(new InputStreamReader(stream));
        final String line = reader.readLine();
        reader.close();
        if (line != null) {
          final Pattern pattern =
            Pattern.compile("^dot - graphviz version ([0-9\\.]+)");
          final Matcher matcher = pattern.matcher(line);
          if (matcher.find()) {
            mVersionInfo = matcher.group(1);
            final Pattern subPattern =
              Pattern.compile("2\\.([0-9]+)(\\.[0-9]+)?");
            final Matcher subMatcher = subPattern.matcher(mVersionInfo);
            int minorVersion = 99;
            if (subMatcher.matches()) {
              final String group = subMatcher.group(1);
              if (group.length() <= 2) {
                minorVersion = Integer.parseInt(subMatcher.group(1));
              }
            }
            if (minorVersion > 40) {
              mVersionInfo +=
                " - <span style=\"color: red;\">incompatible</span>";
            }
          }
        }
      } catch (final IOException exception) {
        // mVersionInfo remains null
      }
    }

    //#######################################################################
    //# Retrieving Result
    String getVersionInfo()
    {
      return mVersionInfo;
    }

    void kill()
    {
      if (mProcess != null) {
        mProcess.destroy();
      }
    }

    //#######################################################################
    //# Data Members
    private Process mProcess = null;
    private String mVersionInfo = null;

  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 5614454534701458734L;

}
