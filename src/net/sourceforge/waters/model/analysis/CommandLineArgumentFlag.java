//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentFlag
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Iterator;


/**
 * A flag command line argument passed to a {@link ModelVerifierFactory}.
 * Flags are optional arguments that can be either present or absent.
 * The status (present or absent) can be queried using the {@link
 * #getValue()} method, but note that configuration of the compiler and the
 * model verifier will only be triggered if the argument is actually
 * present.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentFlag
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a flag command line argument.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-verbose&quot;</CODE>.
   * @param  description   A textual description of the argument.
   */
  protected CommandLineArgumentFlag(final String name,
                                    final String description)
  {
    super(name, description);
    mIsSet = false;
  }


  //#######################################################################
  //# Simple Access
  protected boolean getValue()
  {
    return mIsSet;
  }


  //#######################################################################
  //# Parsing
  protected void parse(final Iterator<String> iter)
  {
    mIsSet = true;
  }


  //#########################################################################
  //# Data Members
  private boolean mIsSet;

}
