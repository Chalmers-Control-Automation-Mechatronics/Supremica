//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentInteger
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Iterator;


/**
 * An integer command line argument passed to a {@link ModelVerifierFactory}.
 * Integer command line arguments are specified on the command line by their
 * name followed by an integer value, e.g.,
 * <CODE>-limit 500000</CODE>. The parsed value is stored
 * in the <CODE>CommandLineArgumentInteger</CODE> object for retrieval.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentInteger
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument of integer type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   */
  protected CommandLineArgumentInteger(final String name,
                                       final String description)
  {
    super(name, description);
  }

  /**
   * Creates a command line argument of integer type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  required      A flag indicating whether this is a required
   *                       command line argument. The command line tool
   *                       will not accept command lines that fail to
   *                       specify all required arguments.
   */
  protected CommandLineArgumentInteger(final String name,
                                       final String description,
                                      final boolean required)
  {
    super(name, description, required);
  }


  //#######################################################################
  //# Simple Access
  protected String getArgumentTemplate()
  {
    return "<n>";
  }

  protected int getValue()
  {
    return mValue;
  }


  //#######################################################################
  //# Parsing
  protected void parse(final Iterator<String> iter)
  {
    if (iter.hasNext()) {
      final String value = iter.next();
      mValue = Integer.parseInt(value);
    } else {
      failMissingValue();
    }
  }


  //#########################################################################
  //# Data Members
  private int mValue;

}
