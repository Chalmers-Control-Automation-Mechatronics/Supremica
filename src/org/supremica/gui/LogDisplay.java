//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.sourceforge.waters.gui.util.IconLoader;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.log.LoggerFilter;
import org.supremica.log.SupremicaLevel;
import org.supremica.properties.BooleanProperty;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


public class LogDisplay
    extends AppenderSkeleton
{

    public static LogDisplay getInstance()
    {
        InterfaceManager.getInstance();
        if (theLogDisplay == null)
        {
            theLogDisplay = new LogDisplay();
            theLogDisplay.connectStreams();
        }
        return theLogDisplay;
    }

    //#######################################################################
    //# Constructors
    private LogDisplay()
    {
        final LoggerFactory factory = LoggerFactory.getInstance();
        mLayout = factory.getLayout();
        setTextPane(new JTextPane());
        theTextPaneScrollPane = new JScrollPane(mTextPane);
        createAttributes();
        createIcons();
        mLabel = "";
        mIsFancy = true;

        popup = new LoggerPopupMenu(LoggerFactory.getInstance().getLoggerFilter());

        // This code used to be in the popup menu -------------
        mTextPane.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {

                // This is needed for the Linux platform
                // where isPopupTrigger is true only on mousePressed.
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(final MouseEvent e)
            {

                // This is for triggering the popup on Windows platforms
                maybeShowPopup(e);
            }

            private void maybeShowPopup(final MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    popup.show(mTextPane, e.getX(), e.getY());
                }
            }
        });
    }


    //#######################################################################
    //# Initialisation
    private void createAttributes()
    {
        mAttributeMap = new HashMap<Level,MutableAttributeSet>(8);
        final MutableAttributeSet attError = new SimpleAttributeSet();
        mAttributeMap.put(Level.ERROR, attError);
        StyleConstants.setFontSize(attError, 14);
        StyleConstants.setForeground(attError, Color.red);
        final MutableAttributeSet attWarn = new SimpleAttributeSet();
        mAttributeMap.put(Level.WARN, attWarn);
        StyleConstants.setFontSize(attWarn, 14);
        StyleConstants.setForeground(attWarn, new Color(255, 128, 0));
        final MutableAttributeSet attInfo = new SimpleAttributeSet();
        mAttributeMap.put(Level.INFO, attInfo);
        mAttributeMap.put(SupremicaLevel.VERBOSE, attInfo);
        StyleConstants.setFontSize(attInfo, 14);
        StyleConstants.setForeground(attInfo, new Color(0, 80, 0));
        final MutableAttributeSet attDebug = new SimpleAttributeSet();
        mAttributeMap.put(Level.DEBUG, attDebug);
        StyleConstants.setFontSize(attDebug, 14);
        StyleConstants.setForeground(attDebug, Color.blue);
        final MutableAttributeSet attFatal = new SimpleAttributeSet();
        mAttributeMap.put(Level.FATAL, attFatal);
        StyleConstants.setFontSize(attFatal, 14);
        StyleConstants.setForeground(attFatal, Color.black);
        final MutableAttributeSet attAll = new SimpleAttributeSet();
        mAttributeMap.put(Level.ALL, attAll);
        StyleConstants.setFontSize(attAll, 14);
        StyleConstants.setForeground(attAll, Color.blue);
        final MutableAttributeSet attOff = new SimpleAttributeSet();
        mAttributeMap.put(Level.OFF, attOff);
        StyleConstants.setFontSize(attOff, 14);
        StyleConstants.setForeground(attOff, Color.red);
    }

    private void connectStreams()
    {
        if (mStdOutReader == null)
        {
            mStdOutReader = new StdOutReader();
        }
        mStdOutReader.setup();
        if (mStdErrReader == null)
        {
            mStdErrReader = new StdErrReader();
        }
        mStdErrReader.setup();
    }

    private void createIcons()
    {
        mIconMap = new HashMap<Level,ImageIcon>(8);
        mIconMap.put(Level.FATAL, IconLoader.ICON_CONSOLE_ERROR);
        mIconMap.put(Level.ERROR, IconLoader.ICON_CONSOLE_ERROR);
        mIconMap.put(Level.WARN, IconLoader.ICON_CONSOLE_WARNING);
        mIconMap.put(SupremicaLevel.VERBOSE, IconLoader.ICON_CONSOLE_INFO);
        mIconMap.put(Level.INFO, IconLoader.ICON_CONSOLE_INFO);
        mIconMap.put(Level.DEBUG, IconLoader.ICON_CONSOLE_DEBUG);
        mIconMap.put(Level.ALL, null);
        mIconMap.put(Level.OFF, null);
    }


    @Override
    public void close()
    {
    }

    public void clear()
    {
        mTextPane.setText("");
    }

    public void cut()
    {
        mTextPane.cut();
    }

    public void copy()
    {
        mTextPane.copy();
    }


    //#######################################################################
    //# Interface org.apache.log4j.Appender
    /**
     * Displays the given logging event in the log display, as soon as
     * possible. If not called from the AWT event dispatching thread, the
     * event may be queued for later logging in a thread-safe fashion.
     */
    @Override
    public void append(final LoggingEvent event)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            appendImmediately(event);
        }
        else
        {
            synchronized (mEventQueue)
            {
                if (mEventQueue.isEmpty())
                {
                    SwingUtilities.invokeLater(mEventQueueReader);
                }
                mEventQueue.add(event);
            }
        }
    }

    /**
     * Displays the given logging event in the log display.
     * This method changes the GUI data structures and therefore
     * must be called from the AWT event dispatching thread.
     */
    private void appendImmediately(final LoggingEvent event)
    {
        final String text = mLayout.format(event);
        final Level level = event.getLevel();
        mTextPane.setCaretPosition(mDocument.getLength());
        if (mIsFancy)
        {
            final ImageIcon icon = mIconMap.get(level);
            if (icon != null)
            {
                mTextPane.setEditable(true);
                mTextPane.insertIcon(icon);
                mTextPane.setEditable(false);
            }
        }
        try
        {
            mDocument.insertString(mDocument.getLength(),
                text,
                mAttributeMap.get(level));
        }
        catch (final BadLocationException exception)
        {
            throw new IllegalStateException(exception);
        }
    }

    public JComponent getComponent()
    {
        return theTextPaneScrollPane;
    }

    public JComponent getComponentWithoutScrollPane()
    {
        return mTextPane;
    }

    public String getLabel()
    {
        return mLabel;
    }


    //#######################################################################
    //# Properties
    public String[] getOptionStrings()
    {
        return new String[]{ LABEL_OPTION, COLOR_OPTION_FATAL,
        COLOR_OPTION_ERROR, COLOR_OPTION_WARN,
        COLOR_OPTION_INFO, COLOR_OPTION_DEBUG,
        COLOR_OPTION_BACKGROUND, FANCY_OPTION,
        FONT_NAME_OPTION, FONT_SIZE_OPTION };
    }

    private Color parseColor(final String v)
    {
        final StringTokenizer st = new StringTokenizer(v, ",");
        final int val[] = { 255, 255, 255, 255 };
        int i = 0;

        while (st.hasMoreTokens())
        {
            val[i] = Integer.parseInt(st.nextToken());

            i++;
        }

        return new Color(val[0], val[1], val[2], val[3]);
    }

    @Override
    public void setLayout(final Layout layout)
    {
        mLayout = layout;
    }

    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    private void setTextPane(final JTextPane textpane)
    {
        mTextPane = textpane;
        mTextPane.setEditable(false);
        mTextPane.setBackground(Color.white);
        mDocument = mTextPane.getStyledDocument();
    }

    private void setColor(final Level p, final String v)
    {
        StyleConstants.setForeground(mAttributeMap.get(p), parseColor(v));
    }

    private void setFontSize(final int size)
    {
        for (final MutableAttributeSet attribs : mAttributeMap.values())
        {
            StyleConstants.setFontSize(attribs, size);
        }
    }

    private void setFontName(final String name)
    {
        for (final MutableAttributeSet attribs : mAttributeMap.values())
        {
            StyleConstants.setFontFamily(attribs, name);
        }
    }

    public void setOption(final String option, final String value)
    {
        if (option.equalsIgnoreCase(LABEL_OPTION))
        {
            mLabel = value;
        }

        if (option.equalsIgnoreCase(COLOR_OPTION_FATAL))
        {
            setColor(Level.FATAL, value);
        }

        if (option.equalsIgnoreCase(COLOR_OPTION_ERROR))
        {
            setColor(Level.ERROR, value);
        }

        if (option.equalsIgnoreCase(COLOR_OPTION_WARN))
        {
            setColor(Level.WARN, value);
        }

        if (option.equalsIgnoreCase(COLOR_OPTION_INFO))
        {
            setColor(Level.INFO, value);
        }

        if (option.equalsIgnoreCase(COLOR_OPTION_DEBUG))
        {
            setColor(Level.DEBUG, value);
        }

        if (option.equalsIgnoreCase(COLOR_OPTION_BACKGROUND))
        {
            mTextPane.setBackground(parseColor(value));
        }

        if (option.equalsIgnoreCase(FANCY_OPTION))
        {
            mIsFancy = OptionConverter.toBoolean(value, mIsFancy);
        }

        if (option.equalsIgnoreCase(FONT_SIZE_OPTION))
        {
            setFontSize(Integer.parseInt(value));
        }

        if (option.equalsIgnoreCase(FONT_NAME_OPTION))
        {
            setFontName(value);
        }

        return;
    }

    @Override
    public boolean requiresLayout()
    {
        return true;
    }

    class LoggerPopupMenu
        extends JPopupMenu
    {
        private static final long serialVersionUID = 1L;
        private LoggerFilter filter = null;
        private JCheckBoxMenuItem fatalItem = null;
        private JCheckBoxMenuItem errorItem = null;
        private JCheckBoxMenuItem debugItem = null;
        private JCheckBoxMenuItem warnItem = null;
        private JCheckBoxMenuItem infoItem = null;

        // except for access, these are copied straight from gui.Supremica
        private void initPopups()
        {
            final JMenuItem cutItem = new JMenuItem("Cut");

            cutItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    clear();
                }
            });
            add(cutItem);

            final JMenuItem copyItem = new JMenuItem("Copy");

            copyItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    copy();
                }
            });
            add(copyItem);
            addSeparator();

            final JMenuItem clearItem = new JMenuItem("Clear");

            clearItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    clear();
                }
            });
            add(clearItem);
            addSeparator();

            final JMenu logProperties = new JMenu("Log Configuration");

            add(logProperties);

            // FATAL, ERROR, WARN, INFO and DEBUG.
            fatalItem = new JCheckBoxMenuItem("Log Fatal");

            fatalItem.setSelected(filter.allowFatal());
            fatalItem.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(final ItemEvent e)
                {
                    setAllowFatal((e.getStateChange() == ItemEvent.SELECTED)
                    ? true
                        : false);
                }
            });
            logProperties.add(fatalItem);

            errorItem = new JCheckBoxMenuItem("Log Error");

            errorItem.setSelected(filter.allowError());
            errorItem.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(final ItemEvent e)
                {
                    setAllowError((e.getStateChange() == ItemEvent.SELECTED)
                    ? true
                        : false);
                }
            });
            logProperties.add(errorItem);

            warnItem = new JCheckBoxMenuItem("Log Warning");

            warnItem.setSelected(filter.allowWarn());
            warnItem.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(final ItemEvent e)
                {
                    setAllowWarn((e.getStateChange() == ItemEvent.SELECTED)
                    ? true
                        : false);
                }
            });
            logProperties.add(warnItem);

            infoItem = new JCheckBoxMenuItem("Log Info");

            infoItem.setSelected(filter.allowInfo());
            infoItem.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(final ItemEvent e)
                {
                    setAllowInfo((e.getStateChange() == ItemEvent.SELECTED)
                    ? true
                        : false);
                }
            });
            logProperties.add(infoItem);

            debugItem = new JCheckBoxMenuItem("Log Debug");

            debugItem.setSelected(filter.allowDebug());
            debugItem.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(final ItemEvent e)
                {
                    setAllowDebug((e.getStateChange() == ItemEvent.SELECTED)
                    ? true
                        : false);
                }
            });
            logProperties.add(debugItem);
        }

        public LoggerPopupMenu(final LoggerFilter filter)
        {
            this.filter = filter;

            initPopups();
        }

        public boolean allowInfo()
        {
            return filter.allowInfo();
        }

        public void setAllowInfo(final boolean allow)
        {
            filter.setAllowInfo(allow);
        }

        public boolean allowDebug()
        {
            return filter.allowDebug();
        }

        public void setAllowDebug(final boolean allow)
        {
            filter.setAllowDebug(allow);
        }

        public boolean allowWarn()
        {
            return filter.allowWarn();
        }

        public void setAllowWarn(final boolean allow)
        {
            filter.setAllowWarn(allow);
        }

        public boolean allowError()
        {
            return filter.allowError();
        }

        public void setAllowError(final boolean allow)
        {
            filter.setAllowError(allow);
        }

        public boolean allowFatal()
        {
            return filter.allowFatal();
        }

        public void setAllowFatal(final boolean allow)
        {
            filter.setAllowFatal(allow);
        }
    }


    //#######################################################################
    //# Inner Class EventQueueReader
    /**
     * This runnable is executed periodically in the AWT event
     * dispatching thread to display any logging events in
     * {@link #mEventQueue}.
     */
    private class EventQueueReader implements Runnable
    {

        //###################################################################
        //# Interface java.lang.Runnable
        /**
         * Displays all logging events in {@link #mEventQueue} to the
         * log display in a thread-safe manner. This method must be
         * executed from the AWT event dispatching thread.
         */
        @Override
        public void run()
        {
            synchronized (mEventQueue)
            {
                for (final LoggingEvent event : mEventQueue)
                {
                    appendImmediately(event);
                }
                mEventQueue.clear();
            }
        }

    }


    //#######################################################################
    //# Inner Class SystemStreamReader
    /**
     * A thread that continuously monitors a system stream ({@link System#err}
     * or {@link System#out} and redirects anything printed to it to the
     * log display.
     */
    private abstract class SystemStreamReader
        extends Thread
        implements SupremicaPropertyChangeListener
    {

        //###################################################################
        //# Constructor
        /**
         * Creates a new system stream reader thread.
         * {@link #setup()} must be called before the new thread
         * can do anything useful.
         */
        private SystemStreamReader(final BooleanProperty property)
        {
            setDaemon(true);
            mSystemStream = getSystemOut();
            mProperty = property;
            mProperty.addPropertyChangeListener(this);
        }

        //###################################################################
        //# Setup
        /**
         * Initialises the pipe and starts the thread.
         */
        private void setup()
        {
            try
            {
                final Pipe pipe = Pipe.open();
                final Pipe.SourceChannel source = pipe.source();
                final Pipe.SinkChannel sink = pipe.sink();
                mPipeIn = Channels.newInputStream(source);
                final OutputStream pout = Channels.newOutputStream(sink);
                mPrintStream = new PrintStream(pout, true);
                reconnect();
                start();
            }
            catch (final IOException exception)
            {
                System.err.println
                    ("Failed to redirect " + getStreamName() + "!");
                exception.printStackTrace(System.err);
            }
        }

        /**
         * Reconnects this reader to its system stream depending on
         * Supremica property settings. This methods connects the
         * reader to the system stream, if the correspoding property
         * is set to true; otherwise it disconnects so output is
         * returned to the console.
         */
        private void reconnect()
        throws IOException
        {
            if (mProperty.isTrue())
            {
                connect();
            }
            else
            {
                disconnect();
            }
        }

        private void connect()
        throws IOException
        {
            setSystemOut(mPrintStream);
        }

        private void disconnect()
        throws IOException
        {
            setSystemOut(mSystemStream);
        }

        //###################################################################
        //# Interface java.lang.Runnable
        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    final String line = readLine();
                    if (line != null)
                    {
                        logLine(line);
                    }
                }
            }
            catch (final IOException exception)
            {
                exception.printStackTrace(System.err);
            }
        }

        //###################################################################
        //# Interface org.supremica.properties.SupremicaPropertyChangeListener
        /**
         * Updates this reader after a change of Supremica properties.
         * This method reconnects the reader to its system stream if the given
         * property matches the Supremica property represented by this reader.
         * @see #reconnect()
         */
        @Override
        public void propertyChanged(final SupremicaPropertyChangeEvent event)
        {
            try
            {
                reconnect();
            }
            catch (final IOException exception)
            {
                System.err.println
                    ("Failed to redirect " + getStreamName() + "!");
                exception.printStackTrace(System.err);
            }
        }

        //###################################################################
        //# Reading
        private String readLine()
        throws IOException
        {
            final StringBuilder buffer = new StringBuilder();
            char ch;
            do
            {
                final int code = mPipeIn.read(); // blocks
                if (code == -1)
                {
                    throw new EOFException("Broken pipe!");
                }
                ch = (char) code;
                if (ch != '\n' && ch != '\r')
                {
                    buffer.append(ch);
                }
            } while (ch != '\n');
            return buffer.length() == 0 ? null : buffer.toString();
        }

        //###################################################################
        //# Abstract Methods
        abstract PrintStream getSystemOut();
        abstract void setSystemOut(PrintStream stream);
        abstract String getStreamName();
        abstract void logLine(String line);

        //###################################################################
        //# Data Members
        private final PrintStream mSystemStream;
        private final BooleanProperty mProperty;

        private InputStream mPipeIn;
        private PrintStream mPrintStream;

    }


    //#######################################################################
    //# Inner Class StdOutReader
    private class StdOutReader extends SystemStreamReader
    {

        //###################################################################
        //# Constructor
        private StdOutReader()
        {
            super(Config.GENERAL_REDIRECT_STDOUT);
            mLogger = LoggerFactory.createLogger(getClass());
        }

        //###################################################################
        //# Overrides for Abstract Baseclass SystemStreamReader
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

        @Override
        void logLine(final String line)
        {
            mLogger.info("Stdout: " + line);
        }

        //###################################################################
        //# Data Members
        private final Logger mLogger;

    }


    //#######################################################################
    //# Inner Class StdErrReader
    private class StdErrReader extends SystemStreamReader
    {

        //###################################################################
        //# Constructor
        private StdErrReader()
        {
            super(Config.GENERAL_REDIRECT_STDERR);
            mLogger = LoggerFactory.createLogger(getClass());
        }

        //###################################################################
        //# Overrides for Abstract Baseclass SystemStreamReader
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

        @Override
        void logLine(final String line)
        {
            mLogger.error("Stderr: " + line);
        }

        //###################################################################
        //# Data Members
        private final Logger mLogger;

    }


    //#######################################################################
    //# Data Members
    private final JScrollPane theTextPaneScrollPane;
    private JTextPane mTextPane;
    private StyledDocument mDocument;
    private final LoggerPopupMenu popup;
    private Layout mLayout;
    private Map<Level,MutableAttributeSet> mAttributeMap;
    private Map<Level,ImageIcon> mIconMap;
    private String mLabel;
    private boolean mIsFancy;

    /**
     * Queue of logging events to be displayed as soon as the AWT event
     * dispatching thread becomes available.
     */
    private final List<LoggingEvent> mEventQueue =
        new LinkedList<LoggingEvent>();
    /**
     * This runnable is executed periodically in the AWT event
     * dispatching thread to display any logging events in
     * {@link #mEventQueue}.
     */
    private final Runnable mEventQueueReader = new EventQueueReader();
    private SystemStreamReader mStdOutReader;
    private SystemStreamReader mStdErrReader;


    //#######################################################################
    //# Class Variables
    private static LogDisplay theLogDisplay = null;


    //#######################################################################
    //# Configuration Option Names
    private static final String LABEL_OPTION = "Label";
    private static final String COLOR_OPTION_FATAL = "Color.Emerg";
    private static final String COLOR_OPTION_ERROR = "Color.Error";
    private static final String COLOR_OPTION_WARN = "Color.Warn";
    private static final String COLOR_OPTION_INFO = "Color.Info";
    private static final String COLOR_OPTION_DEBUG = "Color.Debug";
    private static final String COLOR_OPTION_BACKGROUND = "Color.Background";
    private static final String FANCY_OPTION = "Fancy";
    private static final String FONT_NAME_OPTION = "Font.Name";
    private static final String FONT_SIZE_OPTION = "Font.Size";

}
