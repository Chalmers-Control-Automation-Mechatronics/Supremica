//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.log
//# CLASS:   SupremicaLogger
//###########################################################################
//# $Id: SupremicaLogger.java,v 1.3 2007-07-21 06:28:07 robi Exp $
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

import java.io.StringWriter;
import java.io.PrintWriter;

import org.apache.log4j.Appender;

import org.supremica.properties.Config;


class SupremicaLogger
    implements Logger
{    
    //#######################################################################
    //# Constructors
    SupremicaLogger(final org.apache.log4j.Logger logger)
    {
        mLogger = logger;
    }  
    
    //#######################################################################
    //# Interface org.apache.log4j.Logger
    public void debug(final Object message)
    {
        mLogger.debug(message);
    }
    
    public void debug(final Object message, final Throwable t)
    {
        mLogger.debug(message, t);
    }
    
    /**
     * Print the stack trace to the registered listeners.
     */
    public void debug(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.debug(trace[i].toString());
        }
    }
    
    public void error(final Object message)
    {
        mLogger.error(message);
    }
    
    public void error(final Object message, Throwable t)
    {
        mLogger.error(message + "\n" + t.toString());
        mLogger.debug(getStackTraceAsString(t));
    }
    
    public void error(final Throwable t)
    {
        mLogger.error(t.toString());
        //mLogger.debug(t.getStackTrace());
        mLogger.debug(getStackTraceAsString(t));
    }
    
    /**
     * Print the stack trace to the registered listeners.
     */
    public void error(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.error(trace[i].toString());
        }
    }
    
    public void fatal(final Object message)
    {
        mLogger.fatal(message);
    }
    
    public void fatal(final Object message, Throwable t)
    {
        mLogger.fatal(message, t);
    }
    
    /**
     * Print the stack trace to the registered listeners.
     */
    public void fatal(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.fatal(trace[i].toString());
        }
    }
    
    public void warn(final Object message)
    {
        mLogger.warn(message);
    }
    
    public void warn(final Object message, Throwable t)
    {
        mLogger.warn(message, t);
    }
    
    /**
     * Print the stack trace to the registered listeners.
     */
    public void warn(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.warn(trace[i].toString());
        }
    }
    
    public void info(final Object message)
    {
        mLogger.info(message);
    }
    
    public void info(final Object message, Throwable t)
    {
        mLogger.info(message, t);
    }
    
    /**
     * Logs the message as an "info"-message only if Supremica is
     * currently in "verbose mode".
     */
    public void verbose(final Object message)
    {
        if (Config.VERBOSE_MODE.isTrue())
        {
            info(message);
        }
    }
    
    /**
     * Print the stack trace to the registered listeners.
     */
    public void info(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.info(trace[i].toString());
        }
    }
    
    public boolean isDebugEnabled()
    {
        return mLogger.isDebugEnabled();
    }
    
    public void setLogToConsole(final boolean log)
    {
        final Appender appender = LoggerFactory.getConsoleAppender();
        if (log)
        {
            mLogger.addAppender(appender);
        }
        else
        {
            mLogger.removeAppender(appender);
        }
    }
    
    private String getStackTraceAsString(final Throwable t)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        StringBuffer error = stringWriter.getBuffer();
        return error.toString();
    }
    
    //#######################################################################
    //# Data Members
    private final org.apache.log4j.Logger mLogger;
}
