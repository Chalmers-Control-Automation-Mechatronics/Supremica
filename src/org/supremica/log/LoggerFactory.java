//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.log
//# CLASS:   LoggerFactory
//###########################################################################
//# $Id: LoggerFactory.java,v 1.17 2007-05-28 07:07:08 robi Exp $
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

package org.supremica.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.varia.NullAppender;

import org.supremica.properties.Config;


public class LoggerFactory
{
    
    //#######################################################################
    //# Constructors
    private LoggerFactory()
    {
    }
    
    
    //#######################################################################
    //# Factory Methods
    public static LoggerFilter getLoggerFilter()
    {
        return mFilter;
    }
    
    public static Logger createLogger(final Class clazz)
    {
        return createLogger(clazz.getName());
    }
    
    public static Logger createLogger(final String name)
    {
        final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(name);
        return new SupremicaLogger(logger);
    }
    
    public static Appender getConsoleAppender()
    {
        return mConsoleAppender;
    }
    
    /**
     * Updates the appenders of the root logger after a change to
     * Supremica properties. This method adds or removes appenders
     * to the root logger, thus changing the behaviour of all loggers
     * that do not implement specific behaviour.
     * @param  property  The property that was changed,
     *                   either {@link Config#LOG_TO_CONSOLE} or
     *                   {@link Config#LOG_TO_GUI}.
     */
    public static void updateProperty(final AppenderProperty property)
    {
        final Appender appender;
        if (property == Config.LOG_TO_CONSOLE)
        {
            appender = mConsoleAppender;
        }
        else if (property == Config.LOG_TO_GUI)
        {
            appender = LogDisplay.getInstance();
        }
        else
        {
            throw new IllegalArgumentException
                ("Unsupported property: " + property.getKey() + "!");
        }
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
        if (property.isTrue())
        {
            root.addAppender(appender);
        }
        else
        {
            root.removeAppender(appender);
        }
    }
    
    public static void logToNull()
	{
        final Appender appender = new NullAppender();
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
		root.addAppender(appender);
	}

    public static void logToFile(final File file)
		throws FileNotFoundException
	{
		final String name = file.toString();
		final OutputStream fstream = new FileOutputStream(file, true);
		final PrintStream pstream = new PrintStream(fstream, true);
		logToStream(pstream, name);
	}

	public static void logToStream(final PrintStream stream)
	{
		logToStream(stream, null);
	}

	public static void logToStream(final PrintStream stream,
								   final String name)
	{
        final PrintWriter writer = new PrintWriter(stream);
        final Appender appender = new WriterAppender(mLayout, writer);
		if (name != null) {
			appender.setName(name);
		}
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
		root.addAppender(appender);
	}

    public static void cancelLogToFile(final File file)
	{
		final String name = file.toString();
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
		final Appender appender = root.getAppender(name);
		root.removeAppender(appender);
		appender.close();
	}


    //#######################################################################
    //# Static Initialisation
    static
    {
        final PatternLayout layout = new PatternLayout("%-5p %m%n");
        final PrintWriter writer = new PrintWriter(System.out);
        final Appender cappender = new WriterAppender(layout, writer);
        mConsoleAppender = cappender;
        final LoggerFilter filter = new LoggerFilter();
        cappender.addFilter(filter);
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
        if (Config.LOG_TO_CONSOLE.isTrue())
        {
            root.addAppender(cappender);
        }
        if (Config.LOG_TO_GUI.isTrue())
        {
            final Appender dappender = LogDisplay.getInstance();
            root.addAppender(dappender);
        }
		mLayout = layout;
        mFilter = filter;        
    }
    
    
    //#######################################################################
    //# Data Members
    private static final PatternLayout mLayout;
    private static final LoggerFilter mFilter;
    private static final Appender mConsoleAppender;
    
}
