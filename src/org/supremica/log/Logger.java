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

package org.supremica.log;

/**
 * A wrapper interface for the {@link org.apache.log4j.Logger} class.
 * In addition to providing access to logging functionality through an
 * interface instead of a class, this interface also provides some more
 * convenient formatting methods for logging of exceptions, and a verbose
 * mode.
 *
 * (Robi thinks that this interface should be removed because it forces all
 * users of logging to link to package org.supremica.log, which depends on
 * all of Supremica. Logging functionality should be accessible through
 * LOG4J directly, and all reformatting issues should be handled by the
 * appenders.)
 *
 * @author Knut &Aring;kesson
 */

public interface Logger
{
    void debug(Object message);

    void debug(StackTraceElement[] stackTrace);

    void debug(Object message, Throwable t);

    void error(Object message);

    void error(StackTraceElement[] stackTrace);

    void error(Object message, Throwable t);

    void error(Throwable t);

    void fatal(Object message);

    void fatal(StackTraceElement[] stackTrace);

    void fatal(Object message, Throwable t);

    void warn(Object message);

    void warn(StackTraceElement[] stackTrace);

    void warn(Object message, Throwable t);

    void info(Object message);

    void info(StackTraceElement[] stackTrace);

    void info(Object message, Throwable t);

    /**
     * Logs the message as an "info"-message only if currently in "verbose mode".
     */
    void verbose(Object message);

    boolean isDebugEnabled();

}
