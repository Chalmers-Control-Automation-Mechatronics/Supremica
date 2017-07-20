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
