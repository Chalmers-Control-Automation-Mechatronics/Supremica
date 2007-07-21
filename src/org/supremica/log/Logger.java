//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.log
//# CLASS:   LoggerFactory
//###########################################################################
//# $Id: Logger.java,v 1.13 2007-07-21 06:28:07 robi Exp $
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
    
    void setLogToConsole(boolean log);
}
