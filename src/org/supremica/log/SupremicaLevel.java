//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.log
//# CLASS:   SupremicaLevel
//###########################################################################
//# $Id: SupremicaLevel.java,v 1.1 2008-01-29 02:12:15 robi Exp $
//###########################################################################

package org.supremica.log;

import org.apache.log4j.Level;


/**
 * Extension of LOG4J's set of levels to provide another level
 * to represent Supremica'a 'verbose' messages.
 *
 * @author Robi Malik
 */

public class SupremicaLevel extends Level
{

  /**
   * Extend base class constructor for local use.
   */
  private SupremicaLevel(final int level,
			 final String name,
			 final int syslogEquivalent)
  {
    super(level, name, syslogEquivalent);
  }


  /**
   * The VERBOSE level. Used for INFO messages to be printed only
   * when Supremica is in VERBOSE mode.
   */
  public static Level VERBOSE =
    new SupremicaLevel(INFO_INT + 1, "INFO", INFO.getSyslogEquivalent());

}