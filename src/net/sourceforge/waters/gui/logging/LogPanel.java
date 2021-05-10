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

package net.sourceforge.waters.gui.logging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.OptionChangeEvent;
import net.sourceforge.waters.analysis.options.OptionChangeListener;
import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.util.IconAndFontLoader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.ide.IDE;
import org.supremica.properties.Config;


/**
 * <P>The log panel at the bottom of the IDE.</P>
 *
 * <P>This is a scroll pane displaying a text pane. It receives log events
 * from the {@link IDEAppender} and displays them with fancy icons and
 * colours. It can also capture output printed to stdout and stderr.
 * The behaviour can be changed through Supremica's configuration or
 * through a popup menu.</P>
 *
 * @author Knut &Aring;kesson, Robi Malik
 */
public class LogPanel extends JPanel
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a log panel without popup menu.
   */
  public LogPanel()
  {
    this(null);
  }

  /**
   * Creates a log panel with popup menu.
   * @param  manager  The action manager providing the actions for the
   *                  popup menu.
   */
  public LogPanel(final WatersPopupActionManager manager)
  {
    mLevelMap = new HashMap<>();
    addLevel(Level.FATAL, IconAndFontLoader.ICON_CONSOLE_ERROR, Color.RED);
    addLevel(Level.ERROR, IconAndFontLoader.ICON_CONSOLE_ERROR, Color.RED);
    addLevel(Level.WARN, IconAndFontLoader.ICON_CONSOLE_WARNING,
             new Color(255, 128, 0));
    addLevel(Level.INFO, IconAndFontLoader.ICON_CONSOLE_INFO, Color.BLUE);
    addLevel(Level.DEBUG, IconAndFontLoader.ICON_CONSOLE_DEBUG, Color.BLACK);
    addLevel(Level.TRACE, IconAndFontLoader.ICON_CONSOLE_DEBUG, Color.BLACK);
    mEventQueueReader = new EventQueueReader();

    final BorderLayout layout = new BorderLayout();
    setLayout(layout);
    mTextPane = new JTextPane();
    mTextPane.setEditable(false);
    final JScrollPane scroll = new WhiteScrollPane(mTextPane);
    final int lineHeight = Math.max(IconAndFontLoader.getWatersIconSize(),
                                    mTextPane.getFont().getSize());
    final Dimension minimum = new Dimension(0, MINIMUM_LINES * lineHeight);
    scroll.setMinimumSize(minimum);
    final Dimension preferred = new Dimension(0, PREFERRED_LINES * lineHeight);
    scroll.setPreferredSize(preferred);
    add(scroll, BorderLayout.CENTER);

    if (manager != null) {
      final MouseListener listener = new LogPanelMouseListener(manager);
      mTextPane.addMouseListener(listener);
    }
  }


  //#########################################################################
  //# Logging
  /**
   * Initialises the appender to receive log events.
   * This method is called by the {@link IDEAppender} class after the
   * {@link IDE} window is up and running. It creates an {@link Appender}
   * object that that displays log events in this log panel. The
   * {@link IDEAppender} then starts sending the log events that should
   * appear in the GUI.
   * @param  name    The LOG4J2 name for the new appender.
   * @param  layout  The layout used to format messages.
   * @param  ignoreExceptions  How exceptions are handled by LOG4J2.
   * @return The new appender.
   */
  public Appender createAppender(final String name,
                                 final Layout<String> layout,
                                 final boolean ignoreExceptions)
  {
    mLayout = layout;
    final Appender appender =
      new LogPanelAppender(name, layout, ignoreExceptions);
    if (mStdOutReader == null) {
      mStdOutReader = new StdOutReader();
      mStdOutReader.setup();
    }
    if (mStdErrReader == null) {
      mStdErrReader = new StdErrReader();
      mStdErrReader.setup();
    }
    return appender;
  }

  /**
   * Clears the log panel.
   * This can be invoked through the popup menu to clear the log.
   */
  public void clear()
  {
    mTextPane.setText("");
  }

  /**
   * Returns whether or not this log panel is empty.
   * @return <CODE>true</CODE> if the panel contains no text at all,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isEmpty()
  {
    final Document doc = mTextPane.getDocument();
    return doc.getLength() == 0;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Displays the given logging event in the log panel, as soon as
   * possible. If not called from the AWT event dispatching thread, the
   * event may be queued for later logging in a thread-safe fashion.
   */
  public void append(final LogEvent event)
  {
    final Level levelInfo = event.getLevel();
    final LevelInfo info = mLevelMap.get(levelInfo);
    if (info != null) {
      final String text = mLayout.toSerializable(event);
      final LogEventInfo eventInfo = new LogEventInfo(info, text);
      append(eventInfo);
    }
  }

  /**
   * Displays the given logging event in the log panel, as soon as
   * possible. If not called from the AWT event dispatching thread, the
   * event may be queued for later logging in a thread-safe fashion.
   */
  private void append(final LogEventInfo eventInfo)
  {
    if (SwingUtilities.isEventDispatchThread()) {
      appendImmediately(eventInfo);
    } else {
      mEventQueueReader.post(eventInfo);
    }
  }

  /**
   * Displays the given logging event in the log display.
   * This method changes the GUI data structures and therefore
   * must be called from the AWT event dispatching thread.
   */
  private void appendImmediately(final LogEventInfo event)
  {
    try {
      final Document document = mTextPane.getDocument();
      int pos = document.getLength();
      final AttributeSet textAttribs = event.getAttributes();
      if (pos > 0) {
        document.insertString(pos, "\n", textAttribs);
      }
      final Icon icon = event.getIcon();
      if (icon != null) {
        final JLabel label = new JLabel(icon);
        label.setText(" ");
        label.setAlignmentY(0.8f);
        final MutableAttributeSet iconAttribs = new SimpleAttributeSet();
        StyleConstants.setComponent(iconAttribs, label);
        pos = document.getLength();
        document.insertString(pos, " ", iconAttribs);
      }
      pos = document.getLength();
      final String text = event.getText();
      document.insertString(pos, text, textAttribs);
    } catch (final BadLocationException exception) {
      throw new IllegalStateException(exception);
    }
  }

  private void addLevel(final Level level, final Icon icon, final Color color)
  {
    final LevelInfo info = new LevelInfo(level, icon, color);
    mLevelMap.put(level, info);
  }


  //#########################################################################
  //# Inner Class LevelInfo
  /**
   * Auxiliary class to hold formatting information (icon and colour)
   * for different logging severity levels.
   */
  private static class LevelInfo
  {
    //#######################################################################
    //# Constructor
    private LevelInfo(final Level level, final Icon icon, final Color color)
    {
      mLevel = level;
      mIcon = icon;
      mAttributes = new SimpleAttributeSet();
      StyleConstants.setForeground(mAttributes, color);
    }

    //#######################################################################
    //# Simple Access
    @SuppressWarnings("unused")
    private Level getLevel()
    {
      return mLevel;
    }

    private Icon getIcon()
    {
      return mIcon;
    }

    private AttributeSet getAttributes()
    {
      return mAttributes;
    }

    //#######################################################################
    //# Data Members
    private final Level mLevel;
    private final Icon mIcon;
    private final MutableAttributeSet mAttributes;
  }


  //#########################################################################
  //# Inner Class LogEventInfo
  /**
   * Auxiliary log event class. When a {@link LogEvent} is received by the
   * panel, the information is copied into a LogEventInfo object for display
   * by the AWT event dispatching thread. The copying is necessary, because
   * LOG4J2 may overwrite the original {@link LogEvent} when the appenders
   * have finished, and it cannot be used any more when the AWT event
   * dispatching thread is invoked with delay.
   */
  private class LogEventInfo
  {
    //#######################################################################
    //# Constructor
    private LogEventInfo(final LevelInfo levelInfo, final String text)
    {
      mLevelInfo = levelInfo;
      mText = text;
    }

    //#######################################################################
    //# Simple Access
    private Icon getIcon()
    {
      return mLevelInfo.getIcon();
    }

    private AttributeSet getAttributes()
    {
      return mLevelInfo.getAttributes();
    }

    private String getText()
    {
      return mText;
    }

    //#######################################################################
    //# Data Members
    private final LevelInfo mLevelInfo;
    private final String mText;
  }


  //#########################################################################
  //# Inner Class EventQueueReader
  /**
   * This runnable is executed periodically in the AWT event dispatching
   * thread to display any queued logging events.
   */
  private class EventQueueReader implements Runnable
  {
    //#######################################################################
    //# Simple Access
    private synchronized void post(final LogEventInfo event)
    {
      if (mEventQueue.isEmpty()) {
        SwingUtilities.invokeLater(this);
      }
      mEventQueue.add(event);
    }

    //#######################################################################
    //# Interface java.lang.Runnable
    /**
     * Displays all logging events in {@link #mEventQueue} to the log display
     * in a thread-safe manner. This method must be executed from the AWT
     * event dispatching thread.
     */
    @Override
    public synchronized void run()
    {
      for (final LogEventInfo event : mEventQueue) {
        appendImmediately(event);
      }
      mEventQueue.clear();
    }

    //#######################################################################
    //# Data Members
    private final Queue<LogEventInfo> mEventQueue = new ArrayDeque<>();
  }


  //#########################################################################
  //# Inner Class LogPanelAppender
  /**
   * An appender implementation for the log panel.
   * This appender receives log events from the {@link IDEAppender} and
   * relays them for display in the log panel.
   */
  private class LogPanelAppender extends AbstractAppender
  {
    //#######################################################################
    //# Constructor
    private LogPanelAppender(final String name,
                             final Layout<String> layout,
                             final boolean ignoreExceptions)
    {
      super(name, null, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    }

    //#######################################################################
    //# Interface org.apache.log4j.core.Appender
    @Override
    public void append(final LogEvent event)
    {
      LogPanel.this.append(event);
    }
  }


  //#########################################################################
  //# Inner Class SystemStreamReader
  /**
   * A thread that continuously monitors a system stream ({@link System#err}
   * or {@link System#out} and redirects anything printed to it to the log
   * display.
   */
  private abstract class SystemStreamReader extends Thread
    implements OptionChangeListener
  {
    //#######################################################################
    //# Constructor
    /**
     * Creates a new system stream reader thread. {@link #setup()} must be
     * called before the new thread can do anything useful.
     */
    private SystemStreamReader(final BooleanOption option,
                               final LevelInfo info)
    {
      setDaemon(true);
      mSystemStream = getSystemOut();
      mOption = option;
      mOption.addOptionChangeListener(this);
      mLevelInfo = info;
    }

    //#######################################################################
    //# Setup
    /**
     * Initialises the pipe and starts the thread.
     */
    private void setup()
    {
      try {
        final Pipe pipe = Pipe.open();
        final Pipe.SourceChannel source = pipe.source();
        final Pipe.SinkChannel sink = pipe.sink();
        mPipeIn = Channels.newInputStream(source);
        final OutputStream pout = Channels.newOutputStream(sink);
        mPrintStream = new PrintStream(pout, true);
        reconnect();
        start();
      } catch (final IOException exception) {
        final Logger logger = LogManager.getLogger();
        logger.error("Failed to redirect " + getStreamName(), exception);
      }
    }

    /**
     * Reconnects this reader to its system stream depending on Supremica
     * property settings. This methods connects the reader to the system
     * stream, if the corresponding property is set to true; otherwise it
     * disconnects so output is returned to the console.
     */
    private void reconnect() throws IOException
    {
      if (mOption.getBooleanValue()) {
        setSystemOut(mPrintStream);
      } else {
        setSystemOut(mSystemStream);
      }
    }

    //#######################################################################
    //# Interface java.lang.Runnable
    @Override
    public void run()
    {
      try {
        while (true) {
          final String line = readLine();
          if (line != null) {
            final LogEventInfo eventInfo = new LogEventInfo(mLevelInfo, line);
            append(eventInfo);
          }
        }
      } catch (final IOException exception) {
        final Logger logger = LogManager.getLogger();
        logger.error("Error reading from " + getStreamName(), exception);
      }
    }

    //#######################################################################
    //# Interface org.supremica.properties.SupremicaPropertyChangeListener
    /**
     * Updates this reader after a change of Supremica properties. This method
     * reconnects the reader to its system stream if the given property
     * matches the Supremica property represented by this reader.
     * @see #reconnect()
     */
    @Override
    public void optionChanged(final OptionChangeEvent event)
    {
      try {
        reconnect();
      } catch (final IOException exception) {
        final Logger logger = LogManager.getLogger();
        logger.error("Failed to redirect " + getStreamName(), exception);
      }
    }

    //#######################################################################
    //# Reading
    private String readLine() throws IOException
    {
      final StringBuilder buffer = new StringBuilder();
      char ch;
      do {
        final int code = mPipeIn.read(); // blocks
        if (code == -1) {
          throw new EOFException("Broken pipe!");
        }
        ch = (char) code;
        if (ch != '\n' && ch != '\r') {
          buffer.append(ch);
        }
      } while (ch != '\n');
      return buffer.length() == 0 ? null : buffer.toString();
    }

    //#######################################################################
    //# Abstract Methods
    abstract PrintStream getSystemOut();

    abstract void setSystemOut(PrintStream stream);

    abstract String getStreamName();

    //#######################################################################
    //# Data Members
    private final PrintStream mSystemStream;
    private final BooleanOption mOption;
    private final LevelInfo mLevelInfo;

    private InputStream mPipeIn;
    private PrintStream mPrintStream;
  }


  //#########################################################################
  //# Inner Class StdOutReader
  private class StdOutReader extends SystemStreamReader
  {
    //#######################################################################
    //# Constructor
    private StdOutReader()
    {
      super(Config.GENERAL_REDIRECT_STDOUT,
            new LevelInfo(Level.DEBUG, null, Color.BLACK));
    }

    //#######################################################################
    //# Overrides for Abstract base class SystemStreamReader
    @Override
    PrintStream getSystemOut()
    {
      return System.out;
    }

    @Override
    void setSystemOut(final PrintStream stream)
    {
      System.setOut(stream);
    }

    @Override
    String getStreamName()
    {
      return "STDOUT";
    }
  }


  //#########################################################################
  //# Inner Class StdErreader
  private class StdErrReader extends SystemStreamReader
  {
    //#######################################################################
    //# Constructor
    private StdErrReader()
    {
      super(Config.GENERAL_REDIRECT_STDERR,
            new LevelInfo(Level.ERROR, null, Color.RED));
    }

    //#######################################################################
    //# Overrides for Abstract base class SystemStreamReader
    @Override
    PrintStream getSystemOut()
    {
      return System.err;
    }

    @Override
    void setSystemOut(final PrintStream stream)
    {
      System.setErr(stream);
    }

    @Override
    String getStreamName()
    {
      return "STDERR";
    }
  }


  //#########################################################################
  //# Inner Class LogPanelMouseListener
  /**
   * A mouse listener to trigger the popup menu for the log panel.
   */
  private class LogPanelMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Constructor
    private LogPanelMouseListener(final WatersPopupActionManager manager)
    {
      mPopupFactory = new LogPanelPopupFactory(manager);
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    @Override
    public void mousePressed(final MouseEvent event)
    {
      mPopupFactory.maybeShowPopup(mTextPane, event, null);
    }

    @Override
    public void mouseReleased(final MouseEvent event)
    {
      mPopupFactory.maybeShowPopup(mTextPane, event, null);
    }

    //#######################################################################
    //# Data Members
    private final PopupFactory mPopupFactory;
  }


  //#########################################################################
  //# Data Members
  private final Map<Level,LevelInfo> mLevelMap;
  private final EventQueueReader mEventQueueReader;
  private final JTextPane mTextPane;

  private Layout<String> mLayout;
  private SystemStreamReader mStdOutReader;
  private SystemStreamReader mStdErrReader;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1029839755773019922L;

  private static final int MINIMUM_LINES = 1;
  private static final int PREFERRED_LINES = 3;

}
