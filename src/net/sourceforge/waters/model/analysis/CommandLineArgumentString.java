//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentString
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Iterator;


/**
 * A string command line argument passed to a {@link ModelVerifierFactory}.
 * String command line arguments are specified on the command line by their
 * name followed by a string value, e.g.,
 * <CODE>-marking &quot;:acc&quot;</CODE>. The parsed value is stored
 * in the <CODE>CommandLineArgumentString</CODE> object for retrieval.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentString
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument of string type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-marking&quot;</CODE>.
   * @param  description   A textual description of the argument.
   */
  protected CommandLineArgumentString(final String name,
                                      final String description)
  {
    super(name, description);
  }

  /**
   * Creates a command line argument of string type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;marking&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  required      A flag indicating whether this is a required
   *                       command line argument. The command line tool
   *                       will not accept command lines that fail to
   *                       specify all required arguments.
   */
  protected CommandLineArgumentString(final String name,
                                      final String description,
                                      final boolean required)
  {
    super(name, description, required);
  }


  //#######################################################################
  //# Simple Access
  protected String getArgumentTemplate()
  {
    return "<name>";
  }

  protected String getValue()
  {
    return mValue;
  }


  //#######################################################################
  //# Parsing
  protected void parse(final Iterator<String> iter)
  {
    if (iter.hasNext()) {
      mValue = iter.next();
    } else {
      failMissingValue();
    }
  }


  //#########################################################################
  //# Data Members
  private String mValue;

}
