//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.log;

import java.io.PrintWriter;
import java.io.StringWriter;


class SupremicaLogger
    implements Logger
{
    //#######################################################################
    //# Constructor
    SupremicaLogger(final org.apache.log4j.Logger logger)
    {
        mLogger = logger;
    }


    //#######################################################################
    //# Interface org.apache.log4j.Logger
    @Override
    public void debug(final Object message)
    {
        mLogger.debug(message);
    }

    @Override
    public void debug(final Object message, final Throwable t)
    {
        mLogger.debug(message, t);
    }

    /**
     * Print the stack trace to the registered listeners.
     */
    @Override
    public void debug(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.debug(trace[i].toString());
        }
    }

    @Override
    public void error(final Object message)
    {
        mLogger.error(message);
    }

    @Override
    public void error(final Object message, final Throwable t)
    {
        mLogger.error(message + "\n" + t.toString());
        mLogger.debug(getStackTraceAsString(t));
    }

    @Override
    public void error(final Throwable t)
    {
        mLogger.error(t.toString());
        //mLogger.debug(t.getStackTrace());
        mLogger.debug(getStackTraceAsString(t));
    }

    /**
     * Print the stack trace to the registered listeners.
     */
    @Override
    public void error(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.error(trace[i].toString());
        }
    }

    @Override
    public void fatal(final Object message)
    {
        mLogger.fatal(message);
    }

    @Override
    public void fatal(final Object message, final Throwable t)
    {
        mLogger.fatal(message, t);
    }

    /**
     * Print the stack trace to the registered listeners.
     */
    @Override
    public void fatal(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.fatal(trace[i].toString());
        }
    }

    @Override
    public void warn(final Object message)
    {
        mLogger.warn(message);
    }

    @Override
    public void warn(final Object message, final Throwable t)
    {
        mLogger.warn(message, t);
    }

    /**
     * Print the stack trace to the registered listeners.
     */
    @Override
    public void warn(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.warn(trace[i].toString());
        }
    }

    @Override
    public void info(final Object message)
    {
        mLogger.info(message);
    }

    @Override
    public void info(final Object message, final Throwable t)
    {
        mLogger.info(message, t);
    }

    /**
     * Logs the message as an "info"-message only if Supremica is
     * currently in "verbose mode".
     */
    @Override
    public void verbose(final Object message)
    {
        mLogger.log(SupremicaLevel.VERBOSE, message);
    }

    /**
     * Print the stack trace to the registered listeners.
     */
    @Override
    public void info(final StackTraceElement[] trace)
    {
        for (int i = 0; i < trace.length; ++i)
        {
            mLogger.info(trace[i].toString());
        }
    }

    @Override
    public boolean isDebugEnabled()
    {
        return mLogger.isDebugEnabled();
    }

    private String getStackTraceAsString(final Throwable t)
    {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        return stringWriter.toString();
    }


    //#######################################################################
    //# Data Members
    private final org.apache.log4j.Logger mLogger;
}





