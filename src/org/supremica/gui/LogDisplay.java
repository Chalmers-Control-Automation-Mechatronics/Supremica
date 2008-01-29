//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.gui
//# CLASS:   LogDisplay
//###########################################################################
//# $Id: LogDisplay.java,v 1.5 2008-01-29 02:12:15 robi Exp $
//###########################################################################

/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

package org.supremica.gui;

import java.awt.Color;
import java.awt.event.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.text.*;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.OptionConverter;

import org.supremica.gui.*;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.log.LoggerFilter;
import org.supremica.log.SupremicaLevel;
import org.supremica.properties.BooleanProperty;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;
import org.supremica.util.VPopupMenu;


public class LogDisplay
    extends AppenderSkeleton
{
    
    public static LogDisplay getInstance()
    {
        final InterfaceManager interfaceManager =
            InterfaceManager.getInstance();
        final LoggerFactory factory = LoggerFactory.getInstance();
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
            public void mousePressed(MouseEvent e)
            {
                
                // This is needed for the Linux platform
                // where isPopupTrigger is true only on mousePressed.
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e)
            {
                
                // This is for triggering the popup on Windows platforms
                maybeShowPopup(e);
            }
            
            private void maybeShowPopup(MouseEvent e)
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
        MutableAttributeSet attError = new SimpleAttributeSet();
        mAttributeMap.put(Level.ERROR, attError);
        StyleConstants.setFontSize(attError, 14);
        StyleConstants.setForeground(attError, Color.red);
        MutableAttributeSet attWarn = new SimpleAttributeSet();
        mAttributeMap.put(Level.WARN, attWarn);
        StyleConstants.setFontSize(attWarn, 14);
        StyleConstants.setForeground(attWarn, new Color(255, 128, 0));
        MutableAttributeSet attInfo = new SimpleAttributeSet();
        mAttributeMap.put(Level.INFO, attInfo);
        mAttributeMap.put(SupremicaLevel.VERBOSE, attInfo);
        StyleConstants.setFontSize(attInfo, 14);
        StyleConstants.setForeground(attInfo, new Color(0, 80, 0));
        MutableAttributeSet attDebug = new SimpleAttributeSet();
        mAttributeMap.put(Level.DEBUG, attDebug);
        StyleConstants.setFontSize(attDebug, 14);
        StyleConstants.setForeground(attDebug, Color.blue);
        MutableAttributeSet attFatal = new SimpleAttributeSet();
        mAttributeMap.put(Level.FATAL, attFatal);
        StyleConstants.setFontSize(attFatal, 14);
        StyleConstants.setForeground(attFatal, Color.black);
        MutableAttributeSet attAll = new SimpleAttributeSet();
        mAttributeMap.put(Level.ALL, attAll);
        StyleConstants.setFontSize(attAll, 14);
        StyleConstants.setForeground(attAll, Color.blue);
        MutableAttributeSet attOff = new SimpleAttributeSet();
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
        mIconMap.put(Level.FATAL, getIcon("/icons/BlackFlag.gif"));
        mIconMap.put(Level.ERROR, getIcon("/icons/RedFlag.gif"));
        mIconMap.put(Level.WARN, getIcon("/icons/OrangeFlag.gif"));
        mIconMap.put(SupremicaLevel.VERBOSE, getIcon("/icons/GreenFlag.gif"));
        mIconMap.put(Level.INFO, getIcon("/icons/GreenFlag.gif"));
        mIconMap.put(Level.DEBUG, getIcon("/icons/BlueFlag.gif"));
        mIconMap.put(Level.ALL, getIcon("/icons/BlackFlag.gif"));
        mIconMap.put(Level.OFF, getIcon("/icons/BlackFlag.gif"));
    }
    
    
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
    
    private ImageIcon getIcon(final String name)
    {
        final URL url = Supremica.class.getResource(name);
        return url == null ? null : new ImageIcon(url);
    }
    
    
    //#######################################################################
    //# Interface org.apache.log4j.Appender
    /**
     * Displays the given logging event in the log display, as soon as
     * possible. If not called from the AWT event dispatching thread, the
     * event may be queued for later logging in a thread-safe fashion.
     */
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
    
    private Color parseColor(String v)
    {
        StringTokenizer st = new StringTokenizer(v, ",");
        int val[] = { 255, 255, 255, 255 };
        int i = 0;
        
        while (st.hasMoreTokens())
        {
            val[i] = Integer.parseInt(st.nextToken());
            
            i++;
        }
        
        return new Color(val[0], val[1], val[2], val[3]);
    }
    
    public void setLayout(Layout layout)
    {
        mLayout = layout;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    private void setTextPane(JTextPane textpane)
    {
        mTextPane = textpane;
        mTextPane.setEditable(false);
        mTextPane.setBackground(Color.white);
        mDocument = mTextPane.getStyledDocument();
    }
    
    private void setColor(Level p, String v)
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
    
    public void setOption(String option, String value)
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
    
    public boolean requiresLayout()
    {
        return true;
    }
    
    class LoggerPopupMenu
        extends VPopupMenu
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
            JMenuItem cutItem = new JMenuItem("Cut");
            
            cutItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    clear();
                }
            });
            add(cutItem);
            
            JMenuItem copyItem = new JMenuItem("Copy");
            
            copyItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    copy();
                }
            });
            add(copyItem);
            addSeparator();
            
            JMenuItem clearItem = new JMenuItem("Clear");
            
            clearItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    clear();
                }
            });
            add(clearItem);
            addSeparator();
            
            JMenu logProperties = new JMenu("Log Configuration");
            
            add(logProperties);
            
            // FATAL, ERROR, WARN, INFO and DEBUG.
            fatalItem = new JCheckBoxMenuItem("Log Fatal");
            
            fatalItem.setSelected(filter.allowFatal());
            fatalItem.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
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
                public void itemStateChanged(ItemEvent e)
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
                public void itemStateChanged(ItemEvent e)
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
                public void itemStateChanged(ItemEvent e)
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
                public void itemStateChanged(ItemEvent e)
                {
                    setAllowDebug((e.getStateChange() == ItemEvent.SELECTED)
                    ? true
                        : false);
                }
            });
            logProperties.add(debugItem);
        }
        
        public LoggerPopupMenu(LoggerFilter filter)
        {
            this.filter = filter;
            
            initPopups();
        }
        
        public boolean allowInfo()
        {
            return filter.allowInfo();
        }
        
        public void setAllowInfo(boolean allow)
        {
            filter.setAllowInfo(allow);
        }
        
        public boolean allowDebug()
        {
            return filter.allowDebug();
        }
        
        public void setAllowDebug(boolean allow)
        {
            filter.setAllowDebug(allow);
        }
        
        public boolean allowWarn()
        {
            return filter.allowWarn();
        }
        
        public void setAllowWarn(boolean allow)
        {
            filter.setAllowWarn(allow);
        }
        
        public boolean allowError()
        {
            return filter.allowError();
        }
        
        public void setAllowError(boolean allow)
        {
            filter.setAllowError(allow);
        }
        
        public boolean allowFatal()
        {
            return filter.allowFatal();
        }
        
        public void setAllowFatal(boolean allow)
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
            final StringBuffer buffer = new StringBuffer();
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
        PrintStream getSystemOut()
        {
            return System.out;
        }
        
        void setSystemOut(final PrintStream stream)
        {
            System.setOut(stream);
        }
        
        String getStreamName()
        {
            return "STDOUT";
        }
        
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
        PrintStream getSystemOut()
        {
            return System.err;
        }
        
        void setSystemOut(final PrintStream stream)
        {
            System.setErr(stream);
        }
        
        String getStreamName()
        {
            return "STDERR";
        }
        
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
    private JScrollPane theTextPaneScrollPane;
    private JTextPane mTextPane;
    private StyledDocument mDocument;
    private LoggerPopupMenu popup;
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
