//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.gui
//# CLASS:   SupremicaLoggerFactory
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

package org.supremica.gui;

import java.io.PrintWriter;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.supremica.log.LoggerFactory;
import org.supremica.log.SupremicaLevel;
import org.supremica.properties.BooleanProperty;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


public class SupremicaLoggerFactory
    extends LoggerFactory
    implements SupremicaPropertyChangeListener
{

    //#######################################################################
    //# Initialisation
	public static SupremicaLoggerFactory initialiseSupremicaLoggerFactory()
	{
		final SupremicaLoggerFactory factory = new SupremicaLoggerFactory();
		factory.initialiseAppenders();
		return factory;
	}


    //#######################################################################
    //# Constructors
    private SupremicaLoggerFactory()
    {
    }
    
    protected void initialiseAppenders()
    {
		super.initialiseAppenders();
        final Layout layout = getLayout();
        final PrintWriter writer = new PrintWriter(System.out);
        final Filter filter = new VerboseFilter();
        final Appender cappender = new WriterAppender(layout, writer);
		final Appender dappender = LogDisplay.getInstance();
        cappender.addFilter(filter);
        dappender.addFilter(filter);
        mConsoleAppender = cappender;

        final Logger root = Logger.getRootLogger();
        if (Config.LOG_TO_CONSOLE.isTrue())
        {
            root.addAppender(mConsoleAppender);
        }
        if (Config.LOG_TO_GUI.isTrue())
        {
            root.addAppender(dappender);
        }

        Config.LOG_TO_CONSOLE.addPropertyChangeListener(this);
        Config.LOG_TO_GUI.addPropertyChangeListener(this);
    }
    

    //#######################################################################
    //# Factory Methods
    public Appender getConsoleAppender()
    {
        return mConsoleAppender;
    }
    
    
    //#######################################################################
    //# Interface org.supremica.properties.SupremicaPropertyChangeListener
    /**
     * Updates the appenders of the root logger after a change to
     * Supremica properties. This method adds or removes appenders
     * to the root logger, thus changing the behaviour of all loggers
     * that do not implement specific behaviour.
     * @param  event     The event fired in response to the property change.
     */
    public void propertyChanged(final SupremicaPropertyChangeEvent event)
    {
        final BooleanProperty property = (BooleanProperty) event.getSource();
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
        final Logger root = Logger.getRootLogger();
        if (property.isTrue())
        {
            root.addAppender(appender);
        }
        else
        {
            root.removeAppender(appender);
        }
    }
    
    
    //#######################################################################
    //# Inner Class VerboseFilter
	private class VerboseFilter extends Filter
	{
		//###################################################################
		//# Overrides for Abstract Base Class org.apache.log4j.spi.Filter
		public int decide(final LoggingEvent event)
		{
			if (event.getLevel() == SupremicaLevel.VERBOSE) {
				if (Config.VERBOSE_MODE.isTrue()) {
					return Filter.ACCEPT;
				} else {
					return Filter.DENY;
				}
			} else {
				final Filter filter = getLoggerFilter();
				return filter.decide(event);
			}
		}

	}


    //#######################################################################
    //# Data Members
    private Appender mConsoleAppender;
    
}
