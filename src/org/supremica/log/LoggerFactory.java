//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.log
//# CLASS:   LoggerFactory
//###########################################################################
//# $Id$
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
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.varia.NullAppender;


public class LoggerFactory
{

    //#######################################################################
    //# Initialisation
	public static LoggerFactory initialiseSimpleLoggerFactory()
	{
		final LoggerFactory factory = new LoggerFactory();
		install(factory);
		return factory;
	}


    //#######################################################################
    //# Constructors
    protected LoggerFactory()
    {
        mLayout = new PatternLayout("%-5p %m%n");
        mLoggerFilter = new LoggerFilter();
    }

    protected void initialiseAppenders()
	{
		install(this);
	}

    
    //#######################################################################
    //# Factory Methods
    public LoggerFilter getLoggerFilter()
    {
        return mLoggerFilter;
    }
    
    public Logger createLoggerFor(final Class clazz)
    {
        return createLoggerFor(clazz.getName());
    }
    
    public Logger createLoggerFor(final String name)
    {
        final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(name);
        return new SupremicaLogger(logger);
    }
    
    public Layout getLayout()
    {
        return mLayout;
    }

    
    //#######################################################################
    //# Redirection
    public void logToNull()
    {
        final Appender appender = new NullAppender();
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
        root.addAppender(appender);
    }
    
    public void logToFile(final File file)
    throws FileNotFoundException
    {
        final String name = file.toString();
        final OutputStream fstream = new FileOutputStream(file, true);
        final PrintStream pstream = new PrintStream(fstream, true);
        logToStream(pstream, name);
    }
    
    public void logToStream(final PrintStream stream)
    {
        logToStream(stream, null);
    }
    
    public void logToStream(final PrintStream stream, final String name)
    {
        final PrintWriter writer = new PrintWriter(stream);
        final Appender appender = new WriterAppender(mLayout, writer);
        if (name != null)
        {
            appender.setName(name);
        }
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
        root.addAppender(appender);
    }
    
    public void cancelLogToFile(final File file)
    {
        final String name = file.toString();
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
        final Appender appender = root.getAppender(name);
        root.removeAppender(appender);
        appender.close();
    }
    
    
    //#######################################################################
    //# Static Access
    public static LoggerFactory getInstance()
    {
        if (theInstance == null) {
			theInstance = initialiseSimpleLoggerFactory();
		}
        return theInstance;
    }
    
    public static Logger createLogger(final Class clazz)
    {
        return getInstance().createLoggerFor(clazz);
    }
    
    public static Logger createLogger(final String name)
    {
        return getInstance().createLoggerFor(name);
    }


    //#######################################################################
    //# Initialisation
	private static void install(final LoggerFactory factory)
	{
		if (theInstance == null) {
			theInstance = factory;
		} else {
			throw new IllegalStateException
				("Trying to install a second logger factory!");
		}
	}


    //#######################################################################
    //# Data Members
    private final Layout mLayout;
    private final LoggerFilter mLoggerFilter;
    
    
    //#######################################################################
    //# Singleton Pattern
    private static LoggerFactory theInstance = null;
}
