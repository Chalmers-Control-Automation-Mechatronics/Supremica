//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgument
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Iterator;


/**
 * A command line argument passed to a {@link ModelVerifierFactory}.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgument {

  //#########################################################################
  //# Constructors
  protected CommandLineArgument(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Simple Access
  public String getName()
  {
    return mName;
  }


  //#########################################################################
  //# Parsing
  protected abstract void parse(Iterator<String> iter);

  protected abstract void assign(ModelVerifier verifier);


  //#########################################################################
  //# Exception Handling
  protected IllegalArgumentException getMissingValueException()
  {
    return new IllegalArgumentException
      ("No value specified for command line argument " + getName() + "!");
  }


  //#########################################################################
  //# Data Members
  private final String mName;

}
