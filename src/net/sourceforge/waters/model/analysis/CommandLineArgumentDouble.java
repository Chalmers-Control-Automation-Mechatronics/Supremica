//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentDouble
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Iterator;

import net.sourceforge.waters.model.analysis.des.ModelVerifierFactory;


/**
 * A floating point number command line argument passed to a
 * {@link ModelVerifierFactory}. Double command line arguments are specified on
 * the command line by their name followed by a floating point number, e.g.,
 * <CODE>-part 2.5</CODE>. The parsed value is stored in the
 * <CODE>CommandLineArgumentDouble</CODE> object for retrieval.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentDouble
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument of double type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   */
  protected CommandLineArgumentDouble(final String name,
                                      final String description)
  {
    super(name, description);
  }

  /**
   * Creates an optional command line argument of double type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  value         Default value for argument.
   */
  protected CommandLineArgumentDouble(final String name,
                                      final String description,
                                      final double value)
  {
    super(name, description);
    mValue = value;
  }

  /**
   * Creates a command line argument of double type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  required      A flag indicating whether this is a required
   *                       command line argument. The command line tool
   *                       will not accept command lines that fail to
   *                       specify all required arguments.
   */
  protected CommandLineArgumentDouble(final String name,
                                      final String description,
                                      final boolean required)
  {
    super(name, description, required);
  }


  //#######################################################################
  //# Simple Access
  @Override
  protected String getArgumentTemplate()
  {
    return "<n>";
  }

  protected double getValue()
  {
    return mValue;
  }


  //#######################################################################
  //# Parsing
  @Override
  public void parse(final Iterator<String> iter)
  {
    if (iter.hasNext()) {
      final String value = iter.next();
      mValue = Double.parseDouble(value);
      iter.remove();
      setUsed(true);
    } else {
      failMissingValue();
    }
  }


  //#########################################################################
  //# Data Members
  private double mValue;

}
